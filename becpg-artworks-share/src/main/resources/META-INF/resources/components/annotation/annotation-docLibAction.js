(function() {
	
	
	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionCheckOutAnnotation",
	   fn : function onActionCheckOutAnnotation(asset) {
	   	
		   Alfresco.util.PopupManager.displayMessage({
         	 text : this.msg("message.annotation-checkout.inprogress")
		   });
		   
		   Alfresco.util.Ajax.request({
            method : Alfresco.util.Ajax.GET,
            url : Alfresco.constants.PROXY_URI + "becpg/annotation/node/" + asset.nodeRef.replace(":/", "") + "/checkout",
            successCallback : {
               fn : function (data) {
               	if (data.json) {
               		 setTimeout(function() {window.open(data.json.viewerUrl,"_blank"); location.reload();},1000);
                  }
               },
               scope : this
            },
            failureCallback : {
               fn : function (data) {
               	Alfresco.util.PopupManager.displayMessage({
                     text : this.msg("message.annotation-checkout.failure")
                  });
               },
               scope : this
            }
         });
	   }
	});

	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionCheckInAnnotation",
	   fn : function onActionCheckInAnnotation(asset) {
	   	
	   	Alfresco.util.PopupManager.displayMessage({
	        	 text : this.msg("message.annotation-checkin.inprogress")
			   });
			   
			   Alfresco.util.Ajax.request({
	           method : Alfresco.util.Ajax.GET,
	           url : Alfresco.constants.PROXY_URI + "becpg/annotation/node/" + asset.nodeRef.replace(":/", "") + "/checkin",
	           successCallback : {
	              fn : function (data) {
	            	  location.reload();
	              },
	              scope : this
	           },
	           failureCallback : {
	              fn : function (data) {
	              	Alfresco.util.PopupManager.displayMessage({
	                    text : this.msg("message.annotation-checkin.failure")
	                 });
	              },
	              scope : this
	           }
	        });
	   }
	});
	
	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionCancelCheckOutAnnotation",
	   fn : function onActionCancelCheckOutAnnotation(asset) {
	   	
	   	Alfresco.util.PopupManager.displayMessage({
	        	 text : this.msg("message.annotation-cancel.inprogress")
			   });
			   
			   Alfresco.util.Ajax.request({
	           method : Alfresco.util.Ajax.GET,
	           url : Alfresco.constants.PROXY_URI + "becpg/annotation/node/" + asset.nodeRef.replace(":/", "") + "/cancel",
	           successCallback : {
	              fn : function (data) {
	            	  location.reload();
	              },
	              scope : this
	           },
	           failureCallback : {
	              fn : function (data) {
	              	Alfresco.util.PopupManager.displayMessage({
	                    text : this.msg("message.annotation-cancel.failure")
	                 });
	              },
	              scope : this
	           }
	        });
	   }
	});
	
	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionCreateSessionAnnotation",
	   fn : function onActionCreateSessionAnnotation(asset) {
	   	
	   	Alfresco.util.PopupManager.displayMessage({
        	 text : this.msg("message.annotation-create-session.inprogress")
		   });
		   
		   Alfresco.util.Ajax.request({
           method : Alfresco.util.Ajax.GET,
           url : Alfresco.constants.PROXY_URI + "becpg/annotation/node/" + asset.nodeRef.replace(":/", "") + "/create-session",
           successCallback : {
              fn : function (data) {
              	if (data.json) {
              		 setTimeout(function(){ window.open(data.json.viewerUrl,"_blank")},1000);
                 }
              },
              scope : this
           },
           failureCallback : {
              fn : function (data) {
              	Alfresco.util.PopupManager.displayMessage({
                    text : this.msg("message.annotation-create-session.failure")
                 });
              },
              scope : this
           }
        });
	   }

	});

})();