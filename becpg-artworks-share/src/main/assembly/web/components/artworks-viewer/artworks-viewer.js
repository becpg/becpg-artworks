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

			this.viewer = WebViewer({
				path: URL_CONTEXT + '/res/components/artworks-viewer/pdftron/lib',
				licenseKey: window.atob(me.options.encryptedLicenseKey),
				documentId: me.options.nodeRef,
				fullAPI: true,
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

						instance.UI.disableFeatures([
							instance.UI.Feature.ContentEdit,
							instance.UI.Feature.Measurement,
							instance.UI.Feature.Redaction,
							instance.UI.Feature.TextSelection,
							instance.UI.Feature.FilePicker,
							instance.UI.Feature.NotesPanel
						]);

						instance.UI.disableElements([
							'toolbarGroup-View',
							'toolbarGroup-Annotate',
							'toolbarGroup-Shapes',
							'toolbarGroup-Insert',
							'toolbarGroup-Measure',
							'toolbarGroup-Edit',
							'toolbarGroup-Forms',
							'ribbons',
							'freeHandToolGroupButton',
							'freeHandHighlightToolGroupButton',
							'shapeToolGroupButton',
							'ellipseToolGroupButton',
							'polygonToolGroupButton',
							'polygonCloudToolGroupButton',
							'lineToolGroupButton',
							'polyLineToolGroupButton',
							'arrowToolGroupButton',
							'redoButton',
							'eraserToolButton',
							'rubberStampToolGroupButton',
							'stampToolGroupButton',
							'fileAttachmentToolGroupButton',
							'calloutToolGroupButton',
							'distanceToolGroupButton',
							'perimeterToolGroupButton',
							'areaToolGroupButton',
							'ellipseAreaToolGroupButton',
							'rectangleAreaToolGroupButton',
							'cloudyRectangleAreaToolGroupButton',
							'countToolGroupButton',
							'dateFreeTextToolButton',
							'dotStampToolButton',
							'checkStampToolButton',
							'crossStampToolButton',
							'freeTextToolGroupButton',
							'highlightToolGroupButton',
							'underlineToolGroupButton',
							'strikeoutToolGroupButton',
							'squigglyToolGroupButton',
							'stickyToolGroupButton',
							'freeTextToolButton',
							'rubberStampToolButton',
							'calendarToolButton',
							'stylePanelToggle',
							'textPopup',
							'contextMenuPopup',
							'fileAttachmentButton',
							'stampToolButton'
						]);

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
						
						const headerItems = [];
						const header = instance.UI.getModularHeader('default-top-header');
						const newItem = {		
						  type: 'toggleButton',
						  dataElement: 'versionButton',
						  img: '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="18px" height="22px" viewBox="0 0 22 22" version="1.1"> <g id="surface1"> <path style=" stroke:none;fill-rule:nonzero;fill-opacity:1;" d="M 20.167969 4.125 C 20.167969 2.472656 18.914062 1.089844 17.269531 0.929688 C 15.625 0.765625 14.128906 1.882812 13.808594 3.503906 C 13.492188 5.125 14.457031 6.722656 16.042969 7.195312 L 16.042969 7.332031 C 16.042969 8.851562 14.808594 10.082031 13.292969 10.082031 L 9.625 10.082031 C 8.636719 10.09375 7.671875 10.414062 6.875 11 L 6.875 7.195312 C 8.402344 6.742188 9.363281 5.234375 9.132812 3.65625 C 8.902344 2.082031 7.550781 0.914062 5.957031 0.914062 C 4.367188 0.914062 3.011719 2.082031 2.785156 3.65625 C 2.554688 5.234375 3.515625 6.742188 5.042969 7.195312 L 5.042969 14.804688 C 3.515625 15.257812 2.554688 16.765625 2.785156 18.34375 C 3.011719 19.917969 4.367188 21.085938 5.957031 21.085938 C 7.550781 21.085938 8.902344 19.917969 9.132812 18.34375 C 9.363281 16.765625 8.402344 15.257812 6.875 14.804688 L 6.875 14.667969 C 6.875 13.148438 8.105469 11.917969 9.625 11.917969 L 13.292969 11.917969 C 15.824219 11.917969 17.875 9.863281 17.875 7.332031 L 17.875 7.195312 C 19.234375 6.789062 20.164062 5.542969 20.167969 4.125 Z M 4.582031 4.125 C 4.582031 3.367188 5.199219 2.75 5.957031 2.75 C 6.71875 2.75 7.332031 3.367188 7.332031 4.125 C 7.332031 4.882812 6.71875 5.5 5.957031 5.5 C 5.199219 5.5 4.582031 4.882812 4.582031 4.125 Z M 7.332031 17.875 C 7.332031 18.632812 6.71875 19.25 5.957031 19.25 C 5.199219 19.25 4.582031 18.632812 4.582031 17.875 C 4.582031 17.117188 5.199219 16.5 5.957031 16.5 C 6.328125 16.488281 6.683594 16.628906 6.945312 16.886719 C 7.207031 17.148438 7.347656 17.507812 7.332031 17.875 Z M 16.957031 5.5 C 16.589844 5.511719 16.234375 5.371094 15.972656 5.113281 C 15.710938 4.851562 15.570312 4.492188 15.582031 4.125 C 15.582031 3.367188 16.199219 2.75 16.957031 2.75 C 17.71875 2.75 18.332031 3.367188 18.332031 4.125 C 18.347656 4.492188 18.207031 4.851562 17.945312 5.113281 C 17.683594 5.371094 17.328125 5.511719 16.957031 5.5 Z M 16.957031 5.5 "/> </g> </svg>',
						  title: 'Versions',
						  onClick: (e) => {
							e.stopPropagation();
							var webViewerEl = document.querySelector('apryse-webviewer');
							var colorsButton = webViewerEl && webViewerEl.shadowRoot ? webViewerEl.shadowRoot.querySelector("[data-element='colorsButton']") : null;
							var colorsMenu = document.getElementById("colorsMenu");
							if (colorsButton) {
								colorsButton.classList.remove("active");
							}
							if (colorsMenu) {
								colorsMenu.style.display = "none";
							}
							
							var alignButton = webViewerEl && webViewerEl.shadowRoot ? webViewerEl.shadowRoot.querySelector("[data-element='alignButton']") : null;
							var alignMenu = document.getElementById("alignMenu");
							if (alignButton) {
								alignButton.classList.remove("active");
							}
							if (alignMenu) {
								alignMenu.style.display = "none";
							}

							var versionMenu = document.getElementById("versionMenu");

							var versionButton = webViewerEl && webViewerEl.shadowRoot ? webViewerEl.shadowRoot.querySelector("[data-element='versionButton']") : null;
							if (versionButton) {
								if (!versionButton.classList.contains("active")) {
									var buttonRect = versionButton.getBoundingClientRect();
									versionMenu.style.top = (buttonRect.bottom + 5) + "px";  // 5px gap below button
									versionMenu.style.right = (window.innerWidth - buttonRect.right) + "px";

									versionButton.classList.add("active");
									versionMenu.style.display = "block";  // Show it
								} else {
									versionButton.classList.remove("active");
									versionMenu.style.display = "none";
								}
							}
						  }
					  }
						
						headerItems.push(newItem);
						
						if (me.options.compareContentURL != null && me.options.mode == "overlay") {
							const newItemAlign = {
							  type: 'toggleButton',
							  dataElement: 'alignButton',
							  img: '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="18px" height="22px" viewBox="0 0 22 22" version="1.1"> <g id="surface1"> <path style=" stroke:none;fill-rule:nonzero;fill-opacity:1;fill:currentColor;" d="M 4 2 L 14 2 C 15.1 2 16 2.9 16 4 L 16 14 C 16 15.1 15.1 16 14 16 L 4 16 C 2.9 16 2 15.1 2 14 L 2 4 C 2 2.9 2.9 2 4 2 Z M 18 6 L 20 6 C 21.1 6 22 6.9 22 8 L 22 18 C 22 19.1 21.1 20 20 20 L 10 20 C 8.9 20 8 19.1 8 18 L 8 16 M 4 4 L 4 14 L 14 14 L 14 4 Z" /> </g> </svg>',
							  title: me.msg["label.alignOverlay"] || 'Align Overlay',
							  onClick: (e) => {
								e.stopPropagation();
								var webViewerEl = document.querySelector('apryse-webviewer');
								var colorsButton = webViewerEl && webViewerEl.shadowRoot ? webViewerEl.shadowRoot.querySelector("[data-element='colorsButton']") : null;
								var colorsMenu = document.getElementById("colorsMenu");
								if (colorsButton) colorsButton.classList.remove("active");
								if (colorsMenu) colorsMenu.style.display = "none";
								
								var versionButton = webViewerEl && webViewerEl.shadowRoot ? webViewerEl.shadowRoot.querySelector("[data-element='versionButton']") : null;
								var versionMenu = document.getElementById("versionMenu");
								if (versionButton) versionButton.classList.remove("active");
								if (versionMenu) versionMenu.style.display = "none";

								var alignMenu = document.getElementById("alignMenu");
								var alignButton = webViewerEl && webViewerEl.shadowRoot ? webViewerEl.shadowRoot.querySelector("[data-element='alignButton']") : null;

								if (alignButton && !alignButton.classList.contains("active")) {
									alignButton.classList.add("active");
									var buttonRect = alignButton.getBoundingClientRect();
									alignMenu.style.top = (buttonRect.bottom + 5) + "px";
									alignMenu.style.right = (window.innerWidth - buttonRect.right) + "px";
									alignMenu.style.display = "block";
								} else if (alignButton) {
									alignButton.classList.remove("active");
									alignMenu.style.display = "none";
								}
							  }
							};
							headerItems.push(newItemAlign);
						}
						
						if (me.options.compareContentURL == null) {
							const newItemColor = {
							  type: 'toggleButton',
							  dataElement: 'colorsButton',
							  img: '<svg version="1.0" xmlns="http://www.w3.org/2000/svg" width="18px" height="22px" viewBox="0 0 100.000000 100.000000" preserveAspectRatio="xMidYMid meet"> <g transform="translate(0.000000,100.000000) scale(0.100000,-0.100000)" fill="#000000" stroke="none"> <path d="M313 975 c-219 -59 -339 -244 -306 -471 33 -231 249 -433 507 -474 109 -17 246 11 296 62 43 43 46 84 10 185 -32 94 -38 154 -16 172 21 18 43 3 76 -50 63 -104 133 -40 116 107 -13 110 -75 225 -173 321 -133 130 -347 192 -510 148z m72 -140 c54 -53 13 -145 -64 -145 -30 0 -70 40 -77 77 -5 27 -1 37 24 63 39 38 83 40 117 5z m276 -14 c26 -32 20 -88 -12 -113 -51 -42 -139 -4 -139 60 0 81 101 116 151 53z m-432 -201 c31 -16 46 -65 31 -100 -15 -37 -37 -50 -84 -50 -66 0 -98 79 -55 134 22 28 72 36 108 16z m130 -226 c40 -33 43 -83 6 -119 -34 -35 -78 -33 -117 5 -25 26 -29 36 -24 62 7 36 22 57 51 68 34 14 52 11 84 -16z"/> </g> </svg>',
							  title: 'Colors',
							  onClick: (e) => {
								e.stopPropagation();
								var webViewerEl = document.querySelector('apryse-webviewer');
								var versionMenu = document.getElementById("versionMenu");
								var versionButton = webViewerEl && webViewerEl.shadowRoot ? webViewerEl.shadowRoot.querySelector("[data-element='versionButton']") : null;
								if (versionButton) {
									versionButton.classList.remove("active");
								}
								if (versionMenu) {
									versionMenu.style.display = "none";
								}
								
								var alignButton = webViewerEl && webViewerEl.shadowRoot ? webViewerEl.shadowRoot.querySelector("[data-element='alignButton']") : null;
								var alignMenu = document.getElementById("alignMenu");
								if (alignButton) {
									alignButton.classList.remove("active");
								}
								if (alignMenu) {
									alignMenu.style.display = "none";
								}

								var colorsMenu = document.getElementById("colorsMenu");
								var colorsButton = webViewerEl && webViewerEl.shadowRoot ? webViewerEl.shadowRoot.querySelector("[data-element='colorsButton']") : null;

								if (colorsButton) {
									if (!colorsButton.classList.contains("active")) {
										colorsButton.classList.add("active");
										var buttonRect = colorsButton.getBoundingClientRect();
										colorsMenu.style.top = (buttonRect.bottom + 5) + "px";  // 5px gap below button
										colorsMenu.style.right = (window.innerWidth - buttonRect.right) + "px";
										colorsMenu.style.display = "block";
									} else {
										colorsButton.classList.remove("active");
										colorsMenu.style.display = "none";
									}
								}
							  }
							};
							headerItems.push(newItemColor);
						}
						
						header.setItems([...header.getItems(), ...headerItems]);
						
						documentViewer.addEventListener('documentLoaded', () => {
							var versionMenu = document.getElementById("versionMenu");
							if (!versionMenu) {
							    // Create the iframe
								var iframe = document.createElement("iframe");
								var currentUrl = window.location.href;
								var url = new URL(currentUrl);
								url.searchParams.set("mode", "version");
								iframe.src = url.toString();
								iframe.style.width = "100%";
								iframe.style.height = "400px";
								iframe.style.border = "none";

								versionMenu = document.createElement("div");
								versionMenu.id = 'versionMenu';
								versionMenu.setAttribute("aria-label", "Versions");
								versionMenu.classList.add("Overlay", "FlyoutMenu", "closed");
								versionMenu.setAttribute("data-element", "versionMenu");

								versionMenu.style.position = "fixed";
								versionMenu.style.display = "none";
								versionMenu.style.backgroundColor = "white";
								versionMenu.style.border = "1px solid #ccc";
								versionMenu.style.right = "6px";
								versionMenu.style.top = "50px";
								versionMenu.style.boxShadow = "0 2px 8px rgba(0,0,0,0.15)";
								versionMenu.style.zIndex = "1000";
								versionMenu.style.width = "400px";  // Set a width

								versionMenu.appendChild(iframe);
								document.body.appendChild(versionMenu);
							}
							
							var closeVersionMenu = function(e) {
								var webViewerEl = document.querySelector('apryse-webviewer');
								var versionButton = webViewerEl && webViewerEl.shadowRoot ? webViewerEl.shadowRoot.querySelector("[data-element='versionButton']") : null;
								if (versionButton && e.target != versionButton && !versionMenu.contains(e.target)) {
									versionButton.classList.remove("active");
									versionMenu.style.display = "none";
								}
							};
															
							document.addEventListener('click', closeVersionMenu);
						});
						if (me.options.compareContentURL != null) {
							
							let offsetX = 0;
							let offsetY = 0;
							let rotation = 0;
							const originalBoxes = [];
							
							const getPageArray = async (doc) => {
								const arr = [];
								const itr = await doc.getPageIterator(1);
								for (itr; await itr.hasNext(); itr.next()) {
									const page = await itr.current();
									arr.push(page);
								}
								return arr;
							}
							
							async function recreateDiff() {
								instance.UI.openElements(['loadingModal']);
								
								const newDoc = await PDFNet.PDFDoc.create();
								await newDoc.lock();
								
								const [doc1Pages, doc2Pages] = await Promise.all([
									getPageArray(result1.doc),
									getPageArray(result2.doc)
								]);
								
								const biggestLength = Math.max(doc1Pages.length, doc2Pages.length);
								let chain = Promise.resolve();
								for (let i = 0; i < biggestLength; i++) {
									const index = i;
									chain = chain.then(async () => {
										let page1 = doc1Pages[index];
										let page2 = doc2Pages[index];
										if (!page1) {
											page1 = new PDFNet.Page(0);
										}
										if (!page2) {
											page2 = new PDFNet.Page(0);
										} else if (originalBoxes[index]) {
											const orig = originalBoxes[index];
											
											// Reset and apply offset
											const cropRect = await PDFNet.Rect.init(
												orig.cropBox.x1 + offsetX, 
												orig.cropBox.y1 + offsetY, 
												orig.cropBox.x2 + offsetX, 
												orig.cropBox.y2 + offsetY
											);
											await page2.setCropBox(cropRect);
											
											const mediaRect = await PDFNet.Rect.init(
												orig.mediaBox.x1 + offsetX, 
												orig.mediaBox.y1 + offsetY, 
												orig.mediaBox.x2 + offsetX, 
												orig.mediaBox.y2 + offsetY
											);
											await page2.setMediaBox(mediaRect);
											
											// Apply rotation
											const rotEnum = (orig.rotation + Math.floor((rotation % 360) / 90)) % 4;
											await page2.setRotation(rotEnum);
										}
										return newDoc.appendVisualDiff(page1, page2, null);
									});
								}
								await chain;
								await newDoc.unlock();
								
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
									   const parser = new DOMParser();
									   const xmlDoc = parser.parseFromString(result1.xfdf, "application/xml");
									   const annotations = xmlDoc.querySelectorAll("annots > *");
									   annotations.forEach((annot) => {
									       const pageAttr = annot.getAttribute("page");
									       if (pageAttr !== null) {
									           const newPage = parseInt(pageAttr, 10) * 2;
									           annot.setAttribute("page", newPage);
									       }
									   });
									   const serializer = new XMLSerializer();
									   const newXfdf = serializer.serializeToString(xmlDoc);
									   await annotationManager.importAnnotations(newXfdf);
									}
									if (result2.xfdf) {
									   const parser = new DOMParser();
									   const xmlDoc = parser.parseFromString(result2.xfdf, "application/xml");
									   const annotations = xmlDoc.querySelectorAll("annots > *");
									   annotations.forEach((annot) => {
									       const pageAttr = annot.getAttribute("page");
									       if (pageAttr !== null) {
									           const newPage = parseInt(pageAttr, 10) * 2 + 1;
									           annot.setAttribute("page", newPage);
									       }
									   });
									   const serializer = new XMLSerializer();
									   const newXfdf = serializer.serializeToString(xmlDoc);
									   await annotationManager.importAnnotations(newXfdf);
									}
								} catch (error) {
									console.error('Error importing annotations:', error);
								}
								
								instance.UI.closeElements(['loadingModal']);
							}
							
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
								
								if (me.options.mode == "overlay") {
									const doc2PagesForOriginals = await getPageArray(result2.doc);
									for (let i = 0; i < doc2PagesForOriginals.length; i++) {
										const page = doc2PagesForOriginals[i];
										const cropBox = await page.getCropBox();
										const cropCoords = await cropBox.get();
										const mediaBox = await page.getMediaBox();
										const mediaCoords = await mediaBox.get();
										const rot = await page.getRotation();
										originalBoxes.push({
											cropBox: { x1: cropCoords.x1, x2: cropCoords.x2, y1: cropCoords.y1, y2: cropCoords.y2 },
											mediaBox: { x1: mediaCoords.x1, x2: mediaCoords.x2, y1: mediaCoords.y1, y2: mediaCoords.y2 },
											rotation: rot
										});
									}
									
									var alignMenu = document.getElementById("alignMenu");
									if (!alignMenu) {
										alignMenu = document.createElement("div");
										alignMenu.id = 'alignMenu';
										alignMenu.setAttribute("aria-label", me.msg["label.alignOverlay"] || "Align Overlay");
										alignMenu.style.position = "fixed";
										alignMenu.style.left = "auto";
										alignMenu.style.display = "none";
										alignMenu.style.backgroundColor = "white";
										alignMenu.style.border = "1px solid #ccc";
										alignMenu.style.padding = "10px";
										alignMenu.style.boxShadow = "0 2px 8px rgba(0,0,0,0.15)";
										alignMenu.style.zIndex = "1000";
										alignMenu.style.width = "180px";
										alignMenu.style.fontFamily = "Lato, sans-serif";
										alignMenu.style.fontWeight = "400";
										alignMenu.style.fontSize = "14px";
										alignMenu.style.color = "#485056";
										alignMenu.classList.add("Overlay", "FlyoutMenu", "closed");
										alignMenu.setAttribute("data-element", "alignMenu");
										
										alignMenu.innerHTML = `
											<div style="font-weight: bold; margin-bottom: 8px; text-align: center; font-family: inherit;">${me.msg["label.shiftOverlay"] || "Shift Overlay"}</div>
											<div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 5px; margin-bottom: 12px; justify-items: center; align-items: center; font-family: inherit;">
												<div></div>
												<button id="align-up" title="${me.msg["label.moveUp"] || "Move Up"}" style="width: 36px; height: 36px; border: 1px solid #ccc; background: white; cursor: pointer; display: flex; align-items: center; justify-content: center; border-radius: 4px; font-family: inherit;">
													<svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none"><line x1="12" y1="19" x2="12" y2="5"></line><polyline points="5 12 12 5 19 12"></polyline></svg>
												</button>
												<div></div>
												<button id="align-left" title="${me.msg["label.moveLeft"] || "Move Left"}" style="width: 36px; height: 36px; border: 1px solid #ccc; background: white; cursor: pointer; display: flex; align-items: center; justify-content: center; border-radius: 4px; font-family: inherit;">
													<svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none"><line x1="19" y1="12" x2="5" y2="12"></line><polyline points="12 19 5 12 12 5"></polyline></svg>
												</button>
												<div style="font-size: 11px; text-align: center; color: #666; font-family: inherit;" id="align-nudge-val">0,0</div>
												<button id="align-right" title="${me.msg["label.moveRight"] || "Move Right"}" style="width: 36px; height: 36px; border: 1px solid #ccc; background: white; cursor: pointer; display: flex; align-items: center; justify-content: center; border-radius: 4px; font-family: inherit;">
													<svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
												</button>
												<div></div>
												<button id="align-down" title="${me.msg["label.moveDown"] || "Move Down"}" style="width: 36px; height: 36px; border: 1px solid #ccc; background: white; cursor: pointer; display: flex; align-items: center; justify-content: center; border-radius: 4px; font-family: inherit;">
													<svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none"><line x1="12" y1="5" x2="12" y2="19"></line><polyline points="19 12 12 19 5 12"></polyline></svg>
												</button>
												<div></div>
											</div>
											<div style="font-weight: bold; margin-bottom: 8px; text-align: center; font-family: inherit;">${me.msg["label.rotateOverlay"] || "Rotate Overlay"}</div>
											<div style="display: flex; justify-content: space-around; margin-bottom: 12px; font-family: inherit;">
												<button id="align-rot-ccw" title="${me.msg["label.rotateCounterclockwise"] || "Rotate Counterclockwise"}" style="width: 44px; height: 36px; border: 1px solid #ccc; background: white; cursor: pointer; display: flex; align-items: center; justify-content: center; border-radius: 4px; font-family: inherit;">
													<svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none"><polyline points="1 4 1 10 7 10"></polyline><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path></svg>
												</button>
												<div style="font-size: 11px; align-self: center; color: #666; font-family: inherit;" id="align-rot-val">0°</div>
												<button id="align-rot-cw" title="${me.msg["label.rotateClockwise"] || "Rotate Clockwise"}" style="width: 44px; height: 36px; border: 1px solid #ccc; background: white; cursor: pointer; display: flex; align-items: center; justify-content: center; border-radius: 4px; font-family: inherit;">
													<svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none"><polyline points="23 4 23 10 17 10"></polyline><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path></svg>
												</button>
											</div>
											<button id="align-reset" style="width: 100%; height: 36px; border: 1px solid #ccc; background: white; cursor: pointer; border-radius: 4px; display: flex; align-items: center; justify-content: center; gap: 5px; font-family: inherit; font-size: inherit; font-weight: inherit; color: inherit;">
												<svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none"><path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"></path></svg> ${me.msg["label.reset"] || "Reset"}
											</button>
										`;
										document.body.appendChild(alignMenu);
										
										const nudgeStep = 2;
										const updateLabels = () => {
											document.getElementById("align-nudge-val").innerText = offsetX + "," + offsetY;
											document.getElementById("align-rot-val").innerText = rotation + "°";
										};
										
										document.getElementById("align-up").onclick = async () => {
											offsetY -= nudgeStep;
											updateLabels();
											await recreateDiff();
										};
										document.getElementById("align-down").onclick = async () => {
											offsetY += nudgeStep;
											updateLabels();
											await recreateDiff();
										};
										document.getElementById("align-left").onclick = async () => {
											offsetX += nudgeStep;
											updateLabels();
											await recreateDiff();
										};
										document.getElementById("align-right").onclick = async () => {
											offsetX -= nudgeStep;
											updateLabels();
											await recreateDiff();
										};
										document.getElementById("align-rot-ccw").onclick = async () => {
											rotation = (rotation - 90 + 360) % 360;
											updateLabels();
											await recreateDiff();
										};
										document.getElementById("align-rot-cw").onclick = async () => {
											rotation = (rotation + 90) % 360;
											updateLabels();
											await recreateDiff();
										};
										document.getElementById("align-reset").onclick = async () => {
											offsetX = 0;
											offsetY = 0;
											rotation = 0;
											updateLabels();
											await recreateDiff();
										};
									}
									
									var closeAlignMenu = function(e) {
										var webViewerEl = document.querySelector('apryse-webviewer');
										var alignButton = webViewerEl && webViewerEl.shadowRoot ? webViewerEl.shadowRoot.querySelector("[data-element='alignButton']") : null;
										if (alignButton && e.target != alignButton && !alignMenu.contains(e.target)) {
											alignButton.classList.remove("active");
											alignMenu.style.display = "none";
										}
									};
									document.addEventListener('click', closeAlignMenu);
								}
								
								// Create comparison document
								if (me.options.mode == "overlay") {
									await recreateDiff();
								} else {
									const newDoc = await PDFNet.PDFDoc.create();
									await newDoc.lock();
									await newDoc.appendTextDiffDoc(result1.doc, result2.doc);
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
								}
						} else {

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
							
							const { openElements, closeElements } = instance.UI;
	
							let colorSeparationLoaded = false;
							documentViewer.addEventListener('documentLoaded', () => {
								instance.UI.setLayoutMode(instance.UI.LayoutMode.FacingContinuous);
								var colorsMenu = document.getElementById("colorsMenu");
								if (!colorsMenu) {
									colorsMenu = document.createElement("div");
									colorsMenu.id = 'colorsMenu';
									colorsMenu.setAttribute("aria-label", "Colors");
									colorsMenu.style.position = "fixed";
									colorsMenu.style.left = "auto";
									colorsMenu.style.display = "none";
									colorsMenu.style.backgroundColor = "white";
									colorsMenu.style.display = "inline-block";
									colorsMenu.classList.add("Overlay");
									colorsMenu.classList.add("FlyoutMenu");
									colorsMenu.classList.add("closed");
									colorsMenu.setAttribute("data-element","colorsMenu");
									document.body.appendChild(colorsMenu);
								}
								var closeColorsMenu = function(e) {
									var webViewerEl = document.querySelector('apryse-webviewer');
									var colorsButton = webViewerEl && webViewerEl.shadowRoot ? webViewerEl.shadowRoot.querySelector("[data-element='colorsButton']") : null;
									if (colorsButton && e.target != colorsButton && !colorsMenu.contains(e.target)) {
										colorsButton.classList.remove("active");
										colorsMenu.style.display = "none";
									}
								};
								
								document.addEventListener('click', closeColorsMenu);
								
								const doc = documentViewer.getDocument();
								colorSeparationLoaded = false;
								// Enable color separation
								doc.enableColorSeparations();
								// wait till the individual "colors" in the top left corner load first
								openElements(['loadingModal']);
	
								// Listen to each color in a PDF document
								doc.addEventListener('colorSeparationAdded', color => {
									colorSeparationLoaded = true;
									const input = document.createElement('input');
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
	
									const label = document.createElement('label');
									label.id = `${color.name} label`;
									label.htmlFor = color.name;
									label.style.color = `rgb(${color.rgb.join(',')})`;
									label.style.marginRight = `10px`;
									label.innerHTML = color.name;
	
									const lineBreak = document.createElement('br');
	
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

