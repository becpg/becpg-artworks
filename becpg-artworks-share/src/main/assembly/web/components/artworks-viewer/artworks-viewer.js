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
				documentId: me.options.nodeRef,
				fullAPI: true,
				initialDoc: me.options.compareContentURL == null ? PROXY_URI + me.options.contentURL : null,
				annotationUser: USERNAME_DISPLAYNAME,
				disabledElements: me.options.compareContentURL != null ? ['layoutButtons', 'pageTransitionButtons', 'toolsButton', 'annotationPopup', 'panToolButton', 'linkButton', 'toolsOverlayCloseButton'] : [],
				isReadOnly: me.options.compareContentURL != null
				//isAdminUser: '${user.isAdmin ? string}',
			}, document.getElementById(me.id + '-viewer'))
				.then(async instance => {

					instance.UI.setLanguage(JS_LOCALE);


					const { Tools, documentViewer, PDFNet, annotationManager } = instance.Core;

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


						var successListener = function DNDUpload_successListener(e) {
							me.shouldSave = false;
							if (me.options.returnUrl) {
								window.location.href = window.location.protocol + "//" + window.location.host + me.options.returnUrl;
							}
						};


						var failureListener = function DNDUpload_failureListener(e) {
							me.shouldSave = true;
							saveButton.classList.add("should-save");
							saveButton.classList.remove("loading");
							alert(e);
						};

						var request = new XMLHttpRequest();
						request.upload.addEventListener("progress", progressListener, false);
						request.upload.addEventListener("load", successListener, false);
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
						formData.append("filename", me.options.fileName);
						formData.append("majorVersion", "false");
						formData.append("overwrite", "false");
						formData.append("updatenameandmimetype", "false")
						formData.append("updateNodeRef", me.options.nodeRef);
						formData.append("description", me.msg["label.newVersion.message"]);

						request.open("POST", url, true);
						request.send(formData);
						request.onreadystatechange = function() {
							if (this.status === 401) {
								var redirect = this.getResponseHeader["Location"];
								if (redirect) {
									window.location.href = window.location.protocol + "//" + window.location.host + redirect;
									return;
								}
								else {
									window.location.reload(true);
									return;
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

						const signatureTool = documentViewer.getTool('AnnotationCreateSignature');

						const createSignHereElement = instance.Annotations.SignatureWidgetAnnotation.prototype.createSignHereElement;

						instance.Annotations.SignatureWidgetAnnotation.prototype.createSignHereElement = function() {
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
									if (annot.annot == null && (annot.Wa.jd == me.options.userId || annot.Wa.jd == (me.options.userId + "-signature"))) {
										allSigned = false;
									}
								});
								
								if (allSigned && action == "delete") {
									signatureWidgetAnnots.forEach(signatureWidgetAnnot => {
										annotations.forEach(annotation => {
											if (signatureWidgetAnnot.annot === annotation && (signatureWidgetAnnot.Wa.jd == me.options.userId || signatureWidgetAnnot.Wa.jd == (me.options.userId + "-signature"))) {
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
						documentViewer.addEventListener('annotationsLoaded', () => {
							annotationManager.addEventListener('annotationChanged', (annotations, action) => {
								me.shouldSave = true;
								saveButton.parentElement.parentElement.classList.remove("yui-button-disabled");
								saveButton.classList.add("should-save");
							});
						});
					}

					// wait until the document has been loaded
					documentViewer.addEventListener('documentLoaded', () => {
						instance.UI.setLayoutMode(instance.UI.LayoutMode.FacingContinuous);
					});
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

