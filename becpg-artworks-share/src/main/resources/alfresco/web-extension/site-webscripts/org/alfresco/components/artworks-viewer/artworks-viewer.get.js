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
		
		model.sourceNodeRef = model.nodeRef;
		
		var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, null);
		if (documentDetails) {
			model.document = documentDetails;
			model.item = documentDetails.item;
			model.node = documentDetails.item.node;

			model.displayName = (model.item.displayName != null) ? model.item.displayName : model.item.fileName;
			model.downloadName = encodeURIComponent(model.displayName);


			model.contentURL = documentDetails.item.node.contentURL;
			
			var siteName = AlfrescoUtil.getSiteFromPath(documentDetails);
			
			if (model.taskId) {
				model.returnUrl = "/share/page/task-edit?taskId=" + model.taskId;
			} else if (!model.returnUrl) {
				if (siteName) {
					model.returnUrl = "/share/page/site/" + siteName + "/document-details?nodeRef=" + model.nodeRef;
				} else {
					model.returnUrl = "/share/page/context/mine/document-details?nodeRef=" + model.nodeRef;
				}
			}
			
			var showBackButton = true;
			
			if (model.mode == "sign") {
				model.signatureStatus = model.node.properties["sign:status"];
				showBackButton = model.signatureStatus == "Signed";
			}
			
			if (showBackButton) {
				model.linkButtons.push({
					id: "back-button",
					href : model.returnUrl,
					label: (user && user.isGuest) ? msg.get("button.login") : msg.get("button.back"),
					cssClass: "brand-bgcolor-2"
				});
			}
			
			if (documentDetails.item.workingCopy) {
				if (documentDetails.item.workingCopy.isWorkingCopy && documentDetails.item.workingCopy.sourceNodeRef) {
					model.sourceNodeRef = documentDetails.item.workingCopy.sourceNodeRef;
					var sourceDetails = AlfrescoUtil.getNodeDetails(model.sourceNodeRef, null);
					model.item.version = sourceDetails.item.version;
				}
			}
		}

	    model.versions = [];
		var versions = getVersions(model.sourceNodeRef);
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
         signatureStatus :  model.signatureStatus != null ? model.signatureStatus : null,
         compareContentURL : model.compareContentURL!=null ? model.compareContentURL : null,
		 contentURL : model.contentURL!=null ? model.contentURL : null,
	     fileName : model.displayName,
		 userId : user.id,
		 returnUrl : model.returnUrl,
		 encryptedLicenseKey : model.document.metadata.custom.artworks.licenseKey,
		 issuerCertificateURL : model.document.metadata.custom.artworks.issuerCertificateURL,
		 mimetype : model.document.item.node.mimetype,
		 parent : model.document.item.parent.nodeRef
      }
   };
   model.widgets = [widget];
		
	
}

main();
