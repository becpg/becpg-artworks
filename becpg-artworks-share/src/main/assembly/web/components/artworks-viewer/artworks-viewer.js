(function() {


	ArtworksViewer = function(htmlId) {

		this.id = htmlId;
		var me = this;

		document.addEventListener('DOMContentLoaded', function(event) {
			me.onReady();
		});


		window.addEventListener("beforeunload", function(e) {
			me.destroy();
		});

		return this;
	};



	ArtworksViewer.prototype =
	{

		shouldSave: false,

		options:
		{
			nodeRef: "",
			contentURL: "",
			compareContentURL: null,
			mode: "",
			fileName: ""
		},

		msg: {},

		setOptions: function ArtworksViewer_setOptions(obj) {
			Object.assign(this.options, obj);
			return this;
		},

		setMessages: function ArtworksViewer_setMessages(obj) {
			Object.assign(this.msg, obj);
			return this;
		},

		onReady: function ArtworksViewer_onReady() {
			var me = this;

			this.viewer = WebViewer.Iframe({
				path: URL_CONTEXT + '/res/components/artworks-viewer/pdftron/lib',
				licenseKey: window.atob(me.options.encryptedLicenseKey),
				documentId: me.options.nodeRef,
				fullAPI: true,
				ui: 'legacy',
				initialDoc: me.options.compareContentURL == null ? PROXY_URI + me.options.contentURL : null,
				annotationUser: USERNAME_DISPLAYNAME,
				enableMeasurement: true,
				disabledElements: (me.options.mode.includes("sign") || me.options.compareContentURL != null) ? ['layoutButtons', 'pageTransitionButtons', 'toolsButton', 'annotationPopup', 'panToolButton', 'linkButton', 'toolsOverlayCloseButton'] : [],
				isReadOnly: me.options.compareContentURL != null
				//isAdminUser: '${user.isAdmin ? string}',
			}, document.getElementById(me.id + '-viewer'))
				.then(async instance => {

					const { documentViewer, annotationManager, PDFNet } = instance.Core;

					documentViewer.addEventListener('documentLoaded', async () => {

						const { VerificationOptions } = instance.UI;

						VerificationOptions.addTrustedCertificates([me.options.issuerCertificateURL]);
					
  					});
										
					instance.UI.setLanguage(JS_LOCALE);
					instance.UI.enableElements(['bookmarksPanel', 'bookmarksPanelButton', 'richTextPopup']);
					documentViewer.addEventListener('pageComplete', () => {
						instance.UI.closeElements(['loadingModal']);
					});
					
					const saveButton = document.getElementById(me.id + '-saveButton');

					// Save
					saveButton.onclick = async e => {
						const doc = documentViewer.getDocument();
						const xfdfString = await annotationManager.exportAnnotations();
						const data = await doc.getFileData({
							// saves the document with annotations in it
							xfdfString,
							flags: instance.Core.SaveOptions.INCREMENTAL
						});
						const arr = new Uint8Array(data);
						const blob = new Blob([arr], { type: 'application/pdf' });


						var progressListener = function DNDUpload_progressListener(e) {
							saveButton.classList.add("loading");
						};

						var failureListener = function DNDUpload_failureListener(e) {
							me.shouldSave = true;
							saveButton.classList.add("should-save");
							saveButton.classList.remove("loading");
							alert(e);
						};

						var request = new XMLHttpRequest();
						request.upload.addEventListener("progress", progressListener, false);
						request.upload.addEventListener("error", failureListener, false);

						var url = PROXY_URI + "api/upload";

						if (CSRF_POLICY.enabled) {

							function getCookie(name) {
								var nameEQ = name + "=";
								var ca = document.cookie.split(';');
								for (var i = 0; i < ca.length; i++) {
									var c = ca[i];
									while (c.charAt(0) == ' ') c = c.substring(1, c.length);
									if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
								}
								return null;
							}

								var token = getCookie(CSRF_POLICY.properties["token"]);

								if (token) {
									token = token.replace(/"/g, '');
								}

								url += "?" + CSRF_POLICY.properties["token"] + "=" + token;
							
						}

						const formData = new FormData;
						formData.append("filedata", blob);
						if (me.options.mimetype == "application/pdf") {
							formData.append("filename", me.options.fileName);
							formData.append("updateNodeRef", me.options.nodeRef);
							formData.append("majorVersion", "false");
							formData.append("description", me.msg["label.newVersion.message"]);
							formData.append("updatenameandmimetype", "false")
						} else {
							me.createNewFile = true;
							var fileName = me.options.fileName;
							fileName += ".pdf";
							formData.append("filename", fileName);
							formData.append("destination", me.options.parent);
						}
						
						formData.append("overwrite", "false");
						
						if (me.options.mode == "sign") {
							formData.append("checkin", "false");
							formData.append('properties', '[{"sign:status":"ReadyToSign"}]');
						}

						request.open("POST", url, true);
						request.send(formData);
						request.responseType = "text";
						request.onreadystatechange = () => {
							if (this.status === 401) {
								var redirect = this.getResponseHeader["Location"];
								if (redirect) {
									window.location.href = window.location.protocol + "//" + window.location.host + redirect;
									return;
								}
								else {
									window.location.reload();
									return;
								}
							} else if (request.status == 200) {
								me.shouldSave = false;
								
							    var response = JSON.parse(request.responseText);
		
								if (response && me.options.returnUrl.includes("nodeRef=")) {
									me.options.returnUrl = me.options.returnUrl.split("nodeRef=")[0] + "nodeRef=" + response.nodeRef;
								}
								
								if (me.options.returnUrl) {
									window.location.href = window.location.protocol + "//" + window.location.host + me.options.returnUrl;
								}
							}
						};
					};
						

					if (me.options.mode == "sign") {
						
						 const tool = instance.Core.documentViewer.getTool('AnnotationCreateSignature');
 						 tool.setSigningMode(instance.Core.Tools.SignatureCreateTool.SigningModes.ANNOTATION);

						instance.UI.disableElements(['toolbarGroup-View']);
						instance.UI.disableElements(['toolbarGroup-Annotate']);
						instance.UI.disableElements(['toolbarGroup-Shapes']);
						instance.UI.disableElements(['toolbarGroup-Insert']);
						instance.UI.disableElements(['toolbarGroup-Measure']);
						instance.UI.disableElements(['toolbarGroup-Edit']);
						instance.UI.disableElements(['toolbarGroup-Forms']);
						
						instance.UI.disableElements(['ribbons']);
						instance.UI.disableElements(['freeHandToolGroupButton']);
						instance.UI.disableElements(['freeHandHighlightToolGroupButton']);
						instance.UI.disableElements(['shapeToolGroupButton']);
						instance.UI.disableElements(['ellipseToolGroupButton']);
						instance.UI.disableElements(['polygonToolGroupButton']);
						instance.UI.disableElements(['polygonCloudToolGroupButton']);
						instance.UI.disableElements(['lineToolGroupButton']);
						instance.UI.disableElements(['polyLineToolGroupButton']);
						instance.UI.disableElements(['arrowToolGroupButton']);
						instance.UI.disableElements(['redoButton']);
						instance.UI.disableElements(['eraserToolButton']);
						instance.UI.disableElements(['rubberStampToolGroupButton']);
						instance.UI.disableElements(['stampToolGroupButton']);
						instance.UI.disableElements(['fileAttachmentToolGroupButton']);
						instance.UI.disableElements(['calloutToolGroupButton']);
						instance.UI.disableElements(['distanceToolGroupButton']);
						instance.UI.disableElements(['perimeterToolGroupButton']);
						instance.UI.disableElements(['areaToolGroupButton']);
						instance.UI.disableElements(['ellipseAreaToolGroupButton']);
						instance.UI.disableElements(['rectangleAreaToolGroupButton']);
						instance.UI.disableElements(['cloudyRectangleAreaToolGroupButton']);
						instance.UI.disableElements(['countToolGroupButton']);
						instance.UI.disableElements(['dateFreeTextToolButton']);
						instance.UI.disableElements(['dotStampToolButton']);
						instance.UI.disableElements(['checkStampToolButton']);
						instance.UI.disableElements(['crossStampToolButton']);
						instance.UI.disableElements(['freeTextToolGroupButton']);
						instance.UI.disableElements(['highlightToolGroupButton']);
						instance.UI.disableElements(['underlineToolGroupButton']);
						instance.UI.disableElements(['strikeoutToolGroupButton']);
						instance.UI.disableElements(['squigglyToolGroupButton']);
						instance.UI.disableElements(['stickyToolGroupButton']);
						instance.UI.disableElements(['freeTextToolButton']);
						instance.UI.disableElements(['rubberStampToolButton']);
						instance.UI.disableElements(['calendarToolButton']);
						instance.UI.disableElements(['stylePanelToggle']);

						if (me.options.signatureStatus == "Signed") {
							saveButton.style.display = "none";
							instance.UI.openElements(['tabPanel', 'signaturePanel-tabPanel' ]);
							instance.UI.disableElements(['tools-header']);
							instance.UI.disableElements(['toolbarGroup-FillAndSign']);
							instance.UI.disableElements(['layersPanel-tabPanel']);
							instance.UI.disableElements(['thumbnailsPanel-tabPanel']);
							instance.UI.disableElements(['bookmarksPanel-tabPanel']);
							instance.UI.disableElements(['outlinesPanel-tabPanel']);
							instance.UI.disableElements(['fileAttachmentPanel-tabPanel']);
						}
						
						const signatureTool = documentViewer.getTool('AnnotationCreateSignature');

						const createSignHereElement = instance.Core.Annotations.SignatureWidgetAnnotation.prototype.createSignHereElement;

						instance.Core.Annotations.SignatureWidgetAnnotation.prototype.createSignHereElement = function() {

							if (me.options.signatureStatus == "ReadyToSign") {
								return null;
							}
							
							// signHereElement is the default one with dark blue background
							const signHereElement = createSignHereElement.apply(this, arguments);

							signHereElement.style.background = 'red';
							return signHereElement;
						}
							
						signatureTool.addEventListener('locationSelected', () => {
							signatureTool.addSignature();
							instance.UI.closeElements(['signatureModal']);
						});

						documentViewer.addEventListener('annotationsLoaded', () => {
							annotationManager.addEventListener('annotationChanged', (annotations, action) => {
								
								const signatureWidgetAnnots = annotationManager.getAnnotationsList().filter(
									annot => annot instanceof instance.Core.Annotations.SignatureWidgetAnnotation
								);
								
								var allSigned = true;
								
								signatureWidgetAnnots.forEach(annot => {
									var userId = me.options.userId.replace(/\./g, "_");
									if (annot.getAssociatedSignatureAnnotation() == null && (annot.fieldName == userId || annot.fieldName == (userId + "-signature"))) {
										allSigned = false;
									}
								});
								
								if (allSigned && action == "delete") {
									signatureWidgetAnnots.forEach(signatureWidgetAnnot => {
										annotations.forEach(annotation => {
											if (signatureWidgetAnnot.getAssociatedSignatureAnnotation() === annotation) {
												allSigned = false;
											}
										});
									});
								}
								
								if (allSigned) {
									me.shouldSave = true;
									saveButton.parentElement.parentElement.classList.remove("yui-button-disabled");
									saveButton.classList.add("should-save");
								} else {
									me.shouldSave = false;
									saveButton.parentElement.parentElement.classList.add("yui-button-disabled");
									saveButton.classList.remove("should-save");
								}
								
							});
						});
					
					} else {
						instance.UI.setHeaderItems(header => {
							header.push({
								type: 'actionButton',
								img: '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="18px" height="22px" viewBox="0 0 22 22" version="1.1"> <g id="surface1"> <path style=" stroke:none;fill-rule:nonzero;fill-opacity:1;" d="M 20.167969 4.125 C 20.167969 2.472656 18.914062 1.089844 17.269531 0.929688 C 15.625 0.765625 14.128906 1.882812 13.808594 3.503906 C 13.492188 5.125 14.457031 6.722656 16.042969 7.195312 L 16.042969 7.332031 C 16.042969 8.851562 14.808594 10.082031 13.292969 10.082031 L 9.625 10.082031 C 8.636719 10.09375 7.671875 10.414062 6.875 11 L 6.875 7.195312 C 8.402344 6.742188 9.363281 5.234375 9.132812 3.65625 C 8.902344 2.082031 7.550781 0.914062 5.957031 0.914062 C 4.367188 0.914062 3.011719 2.082031 2.785156 3.65625 C 2.554688 5.234375 3.515625 6.742188 5.042969 7.195312 L 5.042969 14.804688 C 3.515625 15.257812 2.554688 16.765625 2.785156 18.34375 C 3.011719 19.917969 4.367188 21.085938 5.957031 21.085938 C 7.550781 21.085938 8.902344 19.917969 9.132812 18.34375 C 9.363281 16.765625 8.402344 15.257812 6.875 14.804688 L 6.875 14.667969 C 6.875 13.148438 8.105469 11.917969 9.625 11.917969 L 13.292969 11.917969 C 15.824219 11.917969 17.875 9.863281 17.875 7.332031 L 17.875 7.195312 C 19.234375 6.789062 20.164062 5.542969 20.167969 4.125 Z M 4.582031 4.125 C 4.582031 3.367188 5.199219 2.75 5.957031 2.75 C 6.71875 2.75 7.332031 3.367188 7.332031 4.125 C 7.332031 4.882812 6.71875 5.5 5.957031 5.5 C 5.199219 5.5 4.582031 4.882812 4.582031 4.125 Z M 7.332031 17.875 C 7.332031 18.632812 6.71875 19.25 5.957031 19.25 C 5.199219 19.25 4.582031 18.632812 4.582031 17.875 C 4.582031 17.117188 5.199219 16.5 5.957031 16.5 C 6.328125 16.488281 6.683594 16.628906 6.945312 16.886719 C 7.207031 17.148438 7.347656 17.507812 7.332031 17.875 Z M 16.957031 5.5 C 16.589844 5.511719 16.234375 5.371094 15.972656 5.113281 C 15.710938 4.851562 15.570312 4.492188 15.582031 4.125 C 15.582031 3.367188 16.199219 2.75 16.957031 2.75 C 17.71875 2.75 18.332031 3.367188 18.332031 4.125 C 18.347656 4.492188 18.207031 4.851562 17.945312 5.113281 C 17.683594 5.371094 17.328125 5.511719 16.957031 5.5 Z M 16.957031 5.5 "/> </g> </svg>',
								onClick: () => {
									const subDocument = document.getElementById('webviewer-1').contentDocument;
									var versionMenu = subDocument.getElementById("versionMenu");
									var versionButton = subDocument.querySelector("[data-element='versionButton']");
									if (versionMenu.classList.contains("closed")) {
										versionMenu.classList.remove("closed");
										versionButton.classList.add("active");
									} else {
										versionMenu.classList.add("closed");
										versionButton.classList.remove("active");
									}
								},
								dataElement: "versionButton"
							});
						});
						
						documentViewer.addEventListener('documentLoaded', () => {
							const subDocument = document.getElementById('webviewer-1').contentDocument;
							var versionMenu = subDocument.getElementById("versionMenu");
							if (!versionMenu) {
							    // Create the iframe
							    var iframe = subDocument.createElement("iframe");
							    var currentUrl = window.location.href;
							    var url = new URL(currentUrl);
							    url.searchParams.set("mode", "version"); // Add or update the 'mode' query parameter
							    iframe.src = url.toString();
							    iframe.style.width = "100%";
							    iframe.style.height = "400px";
							    iframe.style.border = "none";
							
							    // Create versionMenu and insert the iframe
							    versionMenu = subDocument.createElement("div");
							    versionMenu.id = 'versionMenu';
							    versionMenu.setAttribute("aria-label", "Versions");
							    versionMenu.style.left = "auto";
							    versionMenu.style.right = "6px";
							    versionMenu.style.top = "40px";
							    versionMenu.style.display = "inline-block";
							    versionMenu.classList.add("Overlay");
							    versionMenu.classList.add("FlyoutMenu");
							    versionMenu.classList.add("closed");
							    versionMenu.setAttribute("data-element", "versionMenu");
							
							    versionMenu.appendChild(iframe);
							
							    var appElement = subDocument.getElementsByClassName("App")[0];
							    appElement.appendChild(versionMenu);
							}
						});
						if (me.options.compareContentURL != null) {
							
							async function loadDocumentWithAnnotations(url, PDFNet) {
								const tempDoc = await PDFNet.PDFDoc.createFromURL(url);
								await tempDoc.lock();
								
								// Extract XFDF annotations
								const fdfDoc = await tempDoc.fdfExtract(1);
								let xfdf = null;
								
								if (fdfDoc) {
									xfdf = await fdfDoc.saveAsXFDFAsString();
								}
								
								await tempDoc.unlock();
								
								return { doc: tempDoc, xfdf: xfdf };
							}
							
							documentViewer.addEventListener('documentLoaded', () => {
									instance.UI.setLayoutMode(instance.UI.LayoutMode.FacingContinuous);
								});
								
								await PDFNet.initialize();
								
								// Load both documents with their annotations
								const [result1, result2] = await Promise.all([
									loadDocumentWithAnnotations(PROXY_URI + me.options.contentURL, PDFNet),
									loadDocumentWithAnnotations(PROXY_URI + me.options.compareContentURL, PDFNet)
								]);
								
								const newDoc = await PDFNet.PDFDoc.create();
								await newDoc.lock();
								
								// Create comparison document
								if (me.options.mode == "overlay") {
									const getPageArray = async (doc) => {
										const arr = [];
										const itr = await doc.getPageIterator(1);
										for (itr; await itr.hasNext(); itr.next()) {
											const page = await itr.current();
											arr.push(page);
										}
										return arr;
									}
									
									const [doc1Pages, doc2Pages] = await Promise.all([
										getPageArray(result1.doc),
										getPageArray(result2.doc)
									]);
									
									// we'll loop over the doc with the most pages
									const biggestLength = Math.max(doc1Pages.length, doc2Pages.length);
									// we need to do the pages in order, so lets create a Promise chain
									const chain = Promise.resolve();
									for (let i = 0; i < biggestLength; i++) {
										chain.then(async () => {
											let page1 = doc1Pages[i];
											let page2 = doc2Pages[i];
											// handle the case where one document has more pages than the other
											if (!page1) {
												page1 = new PDFNet.Page(0); // create a blank page
											}
											if (!page2) {
												page2 = new PDFNet.Page(0); // create a blank page
											}
											return newDoc.appendVisualDiff(page1, page2, null)
										})
									}
									await chain; // wait for our chain to resolve
								} else {
									await newDoc.appendTextDiffDoc(result1.doc, result2.doc);
								}
								
								await newDoc.unlock();
								
								// Load the comparison document
								await instance.UI.loadDocument(newDoc);
								
								// Wait for document to be fully loaded before importing annotations
								await new Promise(resolve => {
									const onLoaded = () => {
										documentViewer.removeEventListener('documentLoaded', onLoaded);
										resolve();
									};
									documentViewer.addEventListener('documentLoaded', onLoaded);
								});
								
								// Import annotations from both original documents into the comparison view
								try {
									if (result1.xfdf) {
										// Parse the XFDF string into XML
									   const parser = new DOMParser();
									   const xmlDoc = parser.parseFromString(result1.xfdf, "application/xml");

									   // Select all annotations: freetext and text
									   const annotations = xmlDoc.querySelectorAll("annots > *");

									   annotations.forEach((annot) => {
									       const pageAttr = annot.getAttribute("page");
									       if (pageAttr !== null) {
									           const newPage = parseInt(pageAttr, 10) * 2;
									           annot.setAttribute("page", newPage);
									       }
									   });

									   // Serialize XML back to string
									   const serializer = new XMLSerializer();
									   const newXfdf = serializer.serializeToString(xmlDoc);

									   // Import modified annotations
									   await annotationManager.importAnnotations(newXfdf);
									   console.log('Imported annotations from document 1');		
									}
									if (result2.xfdf) {
										// Parse the XFDF string into XML
									   const parser = new DOMParser();
									   const xmlDoc = parser.parseFromString(result2.xfdf, "application/xml");

									   // Select all annotations: freetext and text
									   const annotations = xmlDoc.querySelectorAll("annots > *");

									   annotations.forEach((annot) => {
									       const pageAttr = annot.getAttribute("page");
									       if (pageAttr !== null) {
									           const newPage = parseInt(pageAttr, 10) * 2 + 1;
									           annot.setAttribute("page", newPage);
									       }
									   });

									   // Serialize XML back to string
									   const serializer = new XMLSerializer();
									   const newXfdf = serializer.serializeToString(xmlDoc);

									   // Import modified annotations
									   await annotationManager.importAnnotations(newXfdf);
									   console.log('Imported annotations from document 2');		
									}
								} catch (error) {
									console.error('Error importing annotations:', error);
								}							
						} else {
							instance.UI.setHeaderItems(header => {
								header.push({
									type: 'actionButton',
									img: '<svg version="1.0" xmlns="http://www.w3.org/2000/svg" width="18px" height="22px" viewBox="0 0 100.000000 100.000000" preserveAspectRatio="xMidYMid meet"> <g transform="translate(0.000000,100.000000) scale(0.100000,-0.100000)" fill="#000000" stroke="none"> <path d="M313 975 c-219 -59 -339 -244 -306 -471 33 -231 249 -433 507 -474 109 -17 246 11 296 62 43 43 46 84 10 185 -32 94 -38 154 -16 172 21 18 43 3 76 -50 63 -104 133 -40 116 107 -13 110 -75 225 -173 321 -133 130 -347 192 -510 148z m72 -140 c54 -53 13 -145 -64 -145 -30 0 -70 40 -77 77 -5 27 -1 37 24 63 39 38 83 40 117 5z m276 -14 c26 -32 20 -88 -12 -113 -51 -42 -139 -4 -139 60 0 81 101 116 151 53z m-432 -201 c31 -16 46 -65 31 -100 -15 -37 -37 -50 -84 -50 -66 0 -98 79 -55 134 22 28 72 36 108 16z m130 -226 c40 -33 43 -83 6 -119 -34 -35 -78 -33 -117 5 -25 26 -29 36 -24 62 7 36 22 57 51 68 34 14 52 11 84 -16z"/> </g> </svg>',
									onClick: () => {
										const subDocument = document.getElementById('webviewer-1').contentDocument;
										var colorsMenu = subDocument.getElementById("colorsMenu");
										var colorsButton = subDocument.querySelector("[data-element='colorsButton']");
	
										if (colorsMenu.classList.contains("closed")) {
											colorsMenu.classList.remove("closed");
											colorsButton.classList.add("active");
										} else {
											colorsMenu.classList.add("closed");
											colorsButton.classList.remove("active");
										}
									},
									dataElement: "colorsButton"
								});
							});
							instance.UI.enableFeatures([instance.UI.Feature.Measurement]);
							instance.UI.enableFeatures([instance.UI.Feature.ContentEdit]);
							instance.UI.enableElements(['layersPanel', 'layersPanelButton']);
							var  isUpdating = false;
							documentViewer.addEventListener('annotationsLoaded', () => {
								annotationManager.addEventListener('annotationChanged', (annotations, action) => {
									me.shouldSave = true;
									saveButton.parentElement.parentElement.classList.remove("yui-button-disabled");
									saveButton.classList.add("should-save");
							      // Sync annotations with server depending on the action
							      if (action === 'add' && !isUpdating) {
							        addAnnotations(annotations);
							      } else if (action === 'modify' && !isUpdating) {
							        modifyAnnotations(annotations);
							      } else if (action === 'delete' && !isUpdating) {
							        deleteAnnotations(annotations);
							      }
							    });
							});
							
							async function addAnnotations(annotations) {
							    for(const annotation of annotations) {
							        const xfdf = await annotationManager.exportAnnotations({ annotList: [annotation] })
							        var message = {
					             			 type : "ADD",
					             			 user : me.options.userId,
					             			 xfdf : xfdf,
					             			 id : annotation.Id
					             	 };
					        		  me.ws.send( JSON.stringify(message) );
							    }
							}
							
							async function modifyAnnotations(annotations) {
							    for(const annotation of annotations) {
							        const xfdf = await annotationManager.exportAnnotations({ annotList: [annotation] })
							        var message = {
					             			 type : "MODIFY",
					             			 user : me.options.userId,
					             			 xfdf : xfdf,
					             			 id : annotation.Id
					             	 };
					        		  me.ws.send( JSON.stringify(message) );
							    }
							}
							
							function deleteAnnotations(annotations) {
							    for(const annotation of annotations) {
							        var message = {
					             			 type : "DELETE",
					             			 user : me.options.userId,
					             			 id : annotation.Id
					             	 };
					        		  me.ws.send( JSON.stringify(message) );
							    }
							}
							
							const subDocument = document.getElementById('webviewer-1').contentDocument;
							
							const { openElements, closeElements } = instance.UI;
	
							let colorSeparationLoaded = false;
							documentViewer.addEventListener('documentLoaded', () => {
								instance.UI.setLayoutMode(instance.UI.LayoutMode.FacingContinuous);
								var colorsMenu = subDocument.getElementById("colorsMenu");
								if (!colorsMenu) {
									colorsMenu = subDocument.createElement("div");
									colorsMenu.id = 'colorsMenu';
									colorsMenu.setAttribute("aria-label", "Colors");
									colorsMenu.style.left = "auto";
									colorsMenu.style.right = "6px";
									colorsMenu.style.top = "40px";
									colorsMenu.style.display = "inline-block";
									colorsMenu.classList.add("Overlay");
									colorsMenu.classList.add("FlyoutMenu");
									colorsMenu.classList.add("closed");
									colorsMenu.setAttribute("data-element","colorsMenu");
									var appElement = subDocument.getElementsByClassName("App")[0];
									appElement.appendChild(colorsMenu);
								}
								var colorsButton = document.getElementById('webviewer-1').contentDocument.querySelector("[data-element='colorsButton']");
								var closeColorsMenu = function(e) {
									if (e.target != colorsButton && !colorsMenu.contains(e.target)) {
										colorsMenu.classList.add("closed");
										colorsButton.classList.remove("active");
									}
								};
								
								document.addEventListener('click', closeColorsMenu);
								document.getElementById('webviewer-1').contentDocument.addEventListener('click', closeColorsMenu);
								
								const doc = documentViewer.getDocument();
								colorSeparationLoaded = false;
								// Enable color separation
								doc.enableColorSeparations(true);
								// wait till the individual "colors" in the top left corner load first
								openElements(['loadingModal']);
	
								// Listen to each color in a PDF document
								doc.addEventListener('colorSeparationAdded', color => {
									colorSeparationLoaded = true;
									const input = subDocument.createElement('input');
									input.id = color.name;
									input.type = 'checkbox';
									input.checked = color.enabled;
									input.style.marginTop = `10px`;
									input.style.marginRight = `5px`;
									input.style.marginLeft = `10px`;
									input.onchange = e => {
										// show 'loadingModal', hide it in the 'pageComplete' event
										openElements(['loadingModal']);
										// Show/hide a color
										doc.enableSeparation(color.name, e.target.checked);
										// Redraw the canvas
										documentViewer.refreshAll();
										documentViewer.updateView();
									};
	
									const label = subDocument.createElement('label');
									label.id = `${color.name} label`;
									label.htmlFor = color.name;
									label.style.color = `rgb(${color.rgb.join(',')})`;
									label.style.marginRight = `10px`;
									label.innerHTML = color.name;
	
									const lineBreak = subDocument.createElement('br');
	
									colorsMenu.appendChild(input);
									colorsMenu.appendChild(label);
									colorsMenu.appendChild(lineBreak);
									closeElements(['loadingModal']);
								});
								
								var protocolPrefix = (window.location.protocol === 'https:') ? 'wss:' : 'ws:';
								
								me.ws = new WebSocket(protocolPrefix + '//' + location.host + URL_CONTEXT +  "/annotws/"+me.options.nodeRef.replace(":/",""));
								 me.ws.onmessage = async function (evt) {
							        try {
										isUpdating = true;
							            const message = JSON.parse(evt.data);
							            switch (message.type) {
							                case "ADD":
							                case "MODIFY":
							                    await annotationManager.importAnnotations(message.xfdf);
							                    console.log(`Annotation ${message.id} ${message.type.toLowerCase()}d.`);
							                    break;
							
							                case "DELETE":
							                    const annotations = annotationManager.getAnnotationsList();
							                    const annotationToDelete = annotations.find(a => a.Id === message.id);
							                    if (annotationToDelete) {
													const command = `<delete><id>${message.id}</id></delete>`;
	      											annotationManager.importAnnotationCommand(command);
							                        console.log(`Annotation ${message.id} deleted.`);
							                    } else {
							                        console.warn(`Annotation ${message.id} not found.`);
							                    }
							                    break;
							
							                case "SYNC":
							                    for (const [id, xfdf] of Object.entries(message.annotations)) {
							                        await annotationManager.importAnnotations(xfdf);
							                        console.log(`Synced annotation ${id}`);
							                    }
							                    break;
							
							                default:
							                    console.warn("Unknown message type received:", message);
							            }
							        } catch (error) {
							            console.error("Error processing WebSocket message:", error);
							        } finally {
										isUpdating = false;
									}
							    };
							});
	
							documentViewer.addEventListener('pageComplete', () => {
								// wait for the first 'colorSeparationAdded' event before closing the loading modal
								// we don't want to hide the 'loadingModal' for the first 'pageComplete' event for the initial load
								if (colorSeparationLoaded) {
									closeElements(['loadingModal']);
								}
							});
						}
						
					}
					
					
					

				});


		},
		
		destroy: function ArtworksViewer_destroy() {
			if (this.shouldSave) {
				var text = this.msg["label.exit.message"]
				return text;
			}
		}


	};
})();

