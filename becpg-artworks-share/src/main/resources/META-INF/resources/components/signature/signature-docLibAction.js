(function() {
	
	
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
            	Alfresco.util.PopupManager.displayMessage({
                     text : this.msg("message.signature-checkout.success")
				  });
		
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
	            	  location.reload();
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