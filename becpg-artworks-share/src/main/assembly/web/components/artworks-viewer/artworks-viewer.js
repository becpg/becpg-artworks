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
					
					const { VerificationOptions } = instance.UI;
					
					var bytes = []; // char codes

					for (var i = 0; i < me.options.certificate.length; ++i) {
						var code = me.options.certificate.charCodeAt(i);
						bytes = bytes.concat([code & 0xff, code / 256 >>> 0]);
					}

					VerificationOptions.addTrustedCertificates([bytes, me.options.certificate]);
					
					instance.UI.setLanguage(JS_LOCALE);
					instance.UI.enableElements(['bookmarksPanel', 'bookmarksPanelButton', 'richTextPopup']);
					const { Tools, documentViewer, PDFNet, annotationManager } = instance.Core;
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

						if (me.options.signatureStatus == "Signed") {
							saveButton.style.display = "none";
							instance.UI.openElements(['leftPanel', 'signaturePanel' ]);
							instance.UI.disableElements(['toolsHeader']);
							instance.UI.disableElements(['bookmarksPanelButton']);
							instance.UI.disableElements(['outlinesPanelButton']);
							instance.UI.disableElements(['thumbnailsPanelButton']);
						}
						
						const signatureTool = documentViewer.getTool('AnnotationCreateSignature');

						const createSignHereElement = instance.Annotations.SignatureWidgetAnnotation.prototype.createSignHereElement;

						instance.Annotations.SignatureWidgetAnnotation.prototype.createSignHereElement = function() {

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
									annot => annot instanceof instance.Annotations.SignatureWidgetAnnotation
								);
								
								var allSigned = true;
								
								signatureWidgetAnnots.forEach(annot => {
									if (annot.annot == null && (annot.Va.Ed == me.options.userId || annot.Va.Ed == (me.options.userId + "-signature"))) {
										allSigned = false;
									}
								});
								
								if (allSigned && action == "delete") {
									signatureWidgetAnnots.forEach(signatureWidgetAnnot => {
										annotations.forEach(annotation => {
											if (signatureWidgetAnnot.annot === annotation) {
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
					
					} else if (me.options.compareContentURL != null) {
						
						documentViewer.addEventListener('documentLoaded', () => {
							instance.UI.setLayoutMode(instance.UI.LayoutMode.FacingContinuous);
						});

						await PDFNet.initialize();

						const newDoc = await PDFNet.PDFDoc.create();
						await newDoc.lock();

						const doc1 = await PDFNet.PDFDoc.createFromURL(PROXY_URI + me.options.contentURL);
						const doc2 = await PDFNet.PDFDoc.createFromURL(PROXY_URI + me.options.compareContentURL);


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
								getPageArray(doc1),
								getPageArray(doc2)
							]);

							// we'll loop over the doc with the most pages
							const biggestLength = Math.max(doc1Pages.length, doc2Pages.length)

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

							await newDoc.appendTextDiffDoc(doc1, doc2);
						}

						await newDoc.unlock();

						instance.UI.loadDocument(newDoc);
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
						instance.UI.enableElements(['layersPanel', 'layersPanelButton']);

						documentViewer.addEventListener('annotationsLoaded', () => {
							annotationManager.addEventListener('annotationChanged', (annotations, action) => {
								me.shouldSave = true;
								saveButton.parentElement.parentElement.classList.remove("yui-button-disabled");
								saveButton.classList.add("should-save");
							});
						});
						
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
						});

						documentViewer.addEventListener('pageComplete', () => {
							// wait for the first 'colorSeparationAdded' event before closing the loading modal
							// we don't want to hide the 'loadingModal' for the first 'pageComplete' event for the initial load
							if (colorSeparationLoaded) {
								closeElements(['loadingModal']);
							}
						});
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

