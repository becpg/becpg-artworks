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


					if (me.options.mode == "sign") {
						const signatureTool = documentViewer.getTool('AnnotationCreateSignature');

						Annotations.SignatureWidgetAnnotation.prototype.createSignHereElement = function() {
							const div = document.createElement('div');
							div.style.width = '100%';
							div.style.height = '100%';
							div.style.cursor = 'pointer';

							const inlineSvg = '<svg version="1.1" id="Capa_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" viewBox="0 0 25.588 25.588" style="enable-background:new 0 0 25.588 25.588; width: 100%; height: 100%; transform: translateX(-35%);" xml:space="preserve"><g><path style="fill:#030104;" d="M18.724,9.903l3.855,1.416l-3.206,8.729c-0.3,0.821-1.927,3.39-3.06,3.914l-0.275,0.75c-0.07,0.19-0.25,0.309-0.441,0.309c-0.054,0-0.108-0.01-0.162-0.029c-0.243-0.09-0.369-0.359-0.279-0.604l0.26-0.709c-0.575-1.117-0.146-4.361,0.106-5.047L18.724,9.903z M24.303,0.667c-1.06-0.388-2.301,0.414-2.656,1.383l-2.322,6.326l3.854,1.414l2.319-6.325C25.79,2.673,25.365,1.056,24.303,0.667z M17.328,9.576c0.108,0.04,0.219,0.059,0.327,0.059c0.382,0,0.741-0.234,0.882-0.614l2.45-6.608c0.181-0.487-0.068-1.028-0.555-1.208c-0.491-0.178-1.028,0.068-1.209,0.555l-2.45,6.608C16.592,8.855,16.841,9.396,17.328,9.576z M13.384,21.967c-0.253-0.239-0.568-0.537-1.078-0.764c-0.42-0.187-0.829-0.196-1.128-0.203c-0.031,0-0.067-0.001-0.103-0.002c-0.187-0.512-0.566-0.834-1.135-0.96c-0.753-0.159-1.354,0.196-1.771,0.47c0.037-0.21,0.098-0.46,0.143-0.64c0.144-0.58,0.292-1.18,0.182-1.742c-0.087-0.444-0.462-0.774-0.914-0.806c-1.165-0.065-2.117,0.562-2.956,1.129c-0.881,0.595-1.446,0.95-2.008,0.749c-0.686-0.244-0.755-2.101-0.425-3.755c0.295-1.49,0.844-4.264,2.251-5.524c0.474-0.424,1.16-0.883,1.724-0.66c0.663,0.26,1.211,1.352,1.333,2.653c0.051,0.549,0.53,0.952,1.089,0.902c0.55-0.051,0.954-0.539,0.902-1.089c-0.198-2.12-1.192-3.778-2.593-4.329C6.058,7.07,4.724,6.982,3.107,8.429c-1.759,1.575-2.409,4.246-2.88,6.625c-0.236,1.188-0.811,5.13,1.717,6.029c1.54,0.549,2.791-0.298,3.796-0.976c0.184-0.124,0.365-0.246,0.541-0.355c-0.167,0.725-0.271,1.501,0.167,2.155c0.653,0.982,1.576,1.089,2.742,0.321c0.045-0.029,0.097-0.063,0.146-0.097c0.108,0.226,0.299,0.475,0.646,0.645c0.42,0.206,0.84,0.216,1.146,0.224c0.131,0.003,0.31,0.007,0.364,0.031c0.188,0.083,0.299,0.185,0.515,0.389c0.162,0.153,0.333,0.312,0.55,0.476c0.18,0.135,0.39,0.199,0.598,0.199c0.304,0,0.605-0.139,0.801-0.4c0.331-0.442,0.241-1.069-0.201-1.4C13.61,22.183,13.495,22.072,13.384,21.967z"/></g></svg>';
							div.innerHTML = inlineSvg;

							return div;
						}



						signatureTool.addEventListener('locationSelected', () => {
							signatureTool.setSignature(base64Image);
							signatureTool.addSignature();
							instance.UI.closeElements(['signatureModal']);
						});

						documentViewer.addEventListener('annotationsLoaded', () => {
							const signatureWidgetAnnots = annotationManager.getAnnotationsList().filter(
								annot => annot instanceof Annotations.SignatureWidgetAnnotation
							);

							signatureWidgetAnnots.forEach(annot => {
								annot.isSignedDigitally().then(isSigned => {
									if (isSigned) {
										// if this signature field is signed initially
									} else {
										annot.createSignHereElement();
									}
								});
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


						const saveButton = document.getElementById(me.id + '-saveButton');

						documentViewer.addEventListener('annotationsLoaded', () => {
							annotationManager.addEventListener('annotationChanged', (annotations, action) => {
								me.shouldSave = true;
								saveButton.parentElement.parentElement.classList.remove("yui-button-disabled");
								saveButton.classList.add("should-save");
							});
						});



						// Save
						saveButton.onclick = async e => {
							const doc = documentViewer.getDocument();
							const xfdfString = await annotationManager.exportAnnotations();
							const data = await doc.getFileData({
								// saves the document with annotations in it
								xfdfString
							});
							const arr = new Uint8Array(data);
							const blob = new Blob([arr], { type: 'application/pdf' });


							var progressListener = function DNDUpload_progressListener(e) {
								saveButton.classList.add("loading");
							};


							var successListener = function DNDUpload_successListener(e) {
								me.shouldSave = false;
								window.location.reload(true);
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

									url += "?" + CSRF_POLICY.properties["token"] + "=" + encodeURIComponent(token);
								
							}

							const formData = new FormData;
							formData.append("filedata", blob);
							formData.append("filename", me.options.fileName);
							formData.append("majorVersion", "false");
							formData.append("overwrite", "false");
							formData.append("updatenameandmimetype", "false")
							formData.append("updateNodeRef", me.options.nodeRef);
							formData.append("description", this.msg["label.newVersion.message"]);

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

