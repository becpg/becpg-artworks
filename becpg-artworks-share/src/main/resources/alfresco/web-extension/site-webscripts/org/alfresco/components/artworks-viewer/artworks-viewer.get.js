<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">


function getVersions(nodeRef) {
	var result = remote.connect("alfresco").get('/api/version?nodeRef=' + nodeRef);
	if (result.status != 200) {
		AlfrescoUtil.error(result.status, 'Could not load versions' + nodeRef);
	}
	result = JSON.parse(result);
	return result;
}


function main() {
	AlfrescoUtil.param('nodeRef', null);
	AlfrescoUtil.param('compareTo', null);
	AlfrescoUtil.param('mode', null);
	AlfrescoUtil.param('returnUrl', null);
	AlfrescoUtil.param('taskId', null);
	model.linkButtons = [];

	if (model.nodeRef) {

		var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, null);
		if (documentDetails) {
			model.document = documentDetails;
			model.item = documentDetails.item;
			model.node = documentDetails.item.node;

			model.displayName = (model.item.displayName != null) ? model.item.displayName : model.item.fileName;
			model.downloadName = encodeURIComponent(model.displayName);


			model.contentURL = documentDetails.item.node.contentURL;
			
			if (model.taskId) {
				model.returnUrl = "/share/page/task-edit?taskId=" + model.taskId;
			} else if (!model.returnUrl) {
				model.returnUrl = "/share/page/context/mine/document-details?nodeRef=" + model.nodeRef;
			}
			
			if (model.mode == "sign") {
				if (model.node.properties["sign:status"] == "Signed") {
					model.mode = "signedView";
				}
			}
			
			if (model.mode != "sign") {
				model.linkButtons.push({
					id: "back-button",
					href : model.returnUrl,
					label: (user && user.isGuest) ? msg.get("button.login") : msg.get("button.back"),
					cssClass: "brand-bgcolor-2"
				});
			}
		}

	    model.versions = [];
		var versions = getVersions(model.nodeRef);
		if (versions) {
			 model.versions = versions;
		}

	}
	
	if (model.compareTo) {
		var documentDetails = AlfrescoUtil.getNodeDetails(model.compareTo, null);
		if (documentDetails) {
			model.compareContentURL = documentDetails.item.node.contentURL;
			model.compareItem = documentDetails.item;
			model.compareNode = documentDetails.item.node;

			model.compareDisplayName = (model.item.displayName != null) ? model.item.displayName : model.item.fileName;
		}

	}
	
	  // Widget instantiation metadata...
   var widget = {
      id : "ArtworksViewer", 
      name : "ArtworksViewer",
      options : {
         nodeRef :  (page.url.args.nodeRef != null) ? page.url.args.nodeRef : "",
         mode :  (model.mode != null) ? model.mode : "annotation",
         compareContentURL : model.compareContentURL!=null ? model.compareContentURL : null,
		 contentURL : model.contentURL!=null ? model.contentURL : null,
	     fileName : model.displayName,
		 userId : user.id,
		 returnUrl : model.returnUrl,
		 encryptedLicenseKey : model.document.metadata.custom.artworks.licenseKey,
		 certificate : model.document.metadata.custom.artworks.certificate,
		 mimetype : model.document.item.node.mimetype,
		 parent : model.document.item.parent.nodeRef
      }
   };
   model.widgets = [widget];
		
	
}

main();
