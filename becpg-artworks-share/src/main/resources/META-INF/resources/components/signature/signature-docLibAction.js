(function() {
	
	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionCancelSignature",
	   fn : function onActionCancelSignature(asset) {
	   	
		   Alfresco.util.PopupManager.displayMessage({
         	 text : this.msg("message.signature-cancel.inprogress")
		   });
		   
		   Alfresco.util.Ajax.request({
            method : Alfresco.util.Ajax.GET,
            url : Alfresco.constants.PROXY_URI + "becpg/signature/node/" + asset.nodeRef.replace(":/", "") + "/cancel",
            successCallback : {
               fn : function (data) {
 				if (data.json && data.json.returnedNodeRef) {
               		 setTimeout(function() {location.replace("document-details?nodeRef=" + data.json.returnedNodeRef,"_blank");},1000);
                  } else {
	 				location.reload();
				  }
               },
               scope : this
            },
            failureCallback : {
               fn : function (data) {
               	Alfresco.util.PopupManager.displayMessage({
                     text : this.msg("message.signature-cancel.failure")
                  });
               },
               scope : this
            }
         });
	   }
	});
	
	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionPrepareSignature",
	   fn : function onActionPrepareSignature(asset) {
	   	
		   Alfresco.util.PopupManager.displayMessage({
         	 text : this.msg("message.signature-prepare.inprogress")
		   });

		   Alfresco.util.Ajax.request({
            method : Alfresco.util.Ajax.GET,
            url : Alfresco.constants.PROXY_URI + "becpg/signature/node/" + asset.nodeRef.replace(":/", "") + "/checkout",
            successCallback : {
               fn : function (data) {
				  if (data.json && data.json.viewerUrl) {
               		 setTimeout(function() {location.replace(data.json.viewerUrl,"_blank");},1000);
                  } else {
	 				location.reload();
				  }
               },
               scope : this
            },
            failureCallback : {
               fn : function (data) {
               	Alfresco.util.PopupManager.displayMessage({
                     text : this.msg("message.signature-prepare.failure")
                  });
               },
               scope : this
            }
         });
	   }
	});
	
	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionViewSignature",
	   fn : function onActionViewSignature(asset) {
	   	
			Alfresco.util.PopupManager.displayMessage({
         	 text : this.msg("message.signature-view.inprogress")
		   });

		   Alfresco.util.Ajax.request({
            method : Alfresco.util.Ajax.GET,
            url : Alfresco.constants.PROXY_URI + "becpg/signature/node/" + asset.nodeRef.replace(":/", "") + "/create-session",
            successCallback : {
               fn : function (data) {
				  if (data.json && data.json.viewerUrl) {
               		 setTimeout(function() {location.replace(data.json.viewerUrl,"_blank");},1000);
                  } else {
	 				location.reload();
				  }
               },
               scope : this
            },
            failureCallback : {
               fn : function (data) {
               	Alfresco.util.PopupManager.displayMessage({
                     text : this.msg("message.signature-view.failure")
                  });
               },
               scope : this
            }
         });
	   }
	});
	
	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionCheckOutSignature",
	   fn : function onActionCheckOutSignature(asset) {
	   	
		   Alfresco.util.PopupManager.displayMessage({
         	 text : this.msg("message.signature-checkout.inprogress")
		   });
		   
		   Alfresco.util.Ajax.request({
            method : Alfresco.util.Ajax.GET,
            url : Alfresco.constants.PROXY_URI + "becpg/signature/node/" + asset.nodeRef.replace(":/", "") + "/checkout",
            successCallback : {
               fn : function (data) {
				  if (data.json && data.json.viewerUrl) {
               		 setTimeout(function() {window.open(data.json.viewerUrl,"_blank"); location.reload();},1000);
                  } else {
	 				location.reload();
				  }
               },
               scope : this
            },
            failureCallback : {
               fn : function (data) {
               	Alfresco.util.PopupManager.displayMessage({
                     text : this.msg("message.signature-checkout.failure")
                  });
               },
               scope : this
            }
         });
	   }
	});

	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionCheckInSignature",
	   fn : function onActionCheckInSignature(asset) {
	   	
	   	Alfresco.util.PopupManager.displayMessage({
	        	 text : this.msg("message.signature-checkin.inprogress")
			   });
			   
			   Alfresco.util.Ajax.request({
	           method : Alfresco.util.Ajax.GET,
	           url : Alfresco.constants.PROXY_URI + "becpg/signature/node/" + asset.nodeRef.replace(":/", "") + "/checkin",
	           successCallback : {
	              fn : function (data) {
				  if (data.json && data.json.returnedNodeRef) {
               		 setTimeout(function() {location.replace("document-details?nodeRef=" + data.json.returnedNodeRef,"_blank");},1000);
                  } else {
	 				location.reload();
				  }
	              },
	              scope : this
	           },
	           failureCallback : {
	              fn : function (data) {
	              	Alfresco.util.PopupManager.displayMessage({
	                    text : this.msg("message.signature-checkin.failure")
	                 });
	              },
	              scope : this
	           }
	        });
	   }
	});
	
})();