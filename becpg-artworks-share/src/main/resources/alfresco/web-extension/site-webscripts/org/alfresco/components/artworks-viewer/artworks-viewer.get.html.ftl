
<@markup id="css" >
   <#-- YUI -->
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/base.css" group="template-common" />
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/yui-fonts-grids.css" group="template-common" />
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/yui/columnbrowser/assets/columnbrowser.css" group="template-common" />
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/yui/columnbrowser/assets/skins/default/columnbrowser-skin.css" group="template-common" />
   <#if theme = 'default'>
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/yui/assets/skins/default/skin.css" group="template-common" />
   <#else>
      <@link rel="stylesheet" type="text/css" href="${url.context}/res/themes/${theme}/yui/assets/skin.css" group="template-common" /> 
   </#if>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/themes/${theme}/presentation.css" group="template-common" />
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/quickshare/header.css" group="template-common"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/quickshare/node-header.css" group="template-common"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/document-details/document-versions.css" group="artworks-viewer"/>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/artworks-viewer/artworks-viewer.css" group="artworks-viewer"/>
</@>

<@markup id="js">
    <@script src="${url.context}/res/components/artworks-viewer/pdftron/lib/webviewer.min.js" group="artworks-viewer"/>
    <@script src="${url.context}/res/components/artworks-viewer/artworks-viewer.js" group="artworks-viewer"/>
</@>


 <@markup id="widgets">
   <@inlineScript group="artworks-viewer">
       var URL_CONTEXT = "${url.context}";
	   var PROXY_URI = "${url.context}/proxy/alfresco/";
	   var CSRF_POLICY = {
         enabled: ${((config.scoped["CSRFPolicy"]["filter"].getChildren("rule")?size > 0)?string)!false},
         cookie: "${config.scoped["CSRFPolicy"]["client"].getChildValue("cookie")!""}",
         header: "${config.scoped["CSRFPolicy"]["client"].getChildValue("header")!""}",
         parameter: "${config.scoped["CSRFPolicy"]["client"].getChildValue("parameter")!""}",
         properties: {}
      };
      <#if config.scoped["CSRFPolicy"]["properties"]??>
         <#assign csrfProperties = (config.scoped["CSRFPolicy"]["properties"].children)![]>
         <#list csrfProperties as csrfProperty>
        CSRF_POLICY.properties["${csrfProperty.name?js_string}"] = "${(csrfProperty.value!"")?js_string}";
         </#list>
      </#if>
	   
	   var JS_LOCALE =  "${locale}";
	   var USERNAME_DISPLAYNAME = "${user.properties["firstName"]?html} <#if user.properties["lastName"]??>${user.properties["lastName"]?html}</#if>";
	   
	   function reloadWithCompare(compareToNodeRef, mode) {
	      var url = new URL(window.location.href);
	      url.searchParams.set("compareTo", compareToNodeRef);
	      url.searchParams.set("mode", mode);
	      window.top.location.href = url.toString();
	   }
   </@>
   <@createWidgets group="artworks-viewer"/>
</@>


<@markup id="html">
		<#assign el=args.htmlid?html/>
		<#include "../../include/alfresco-macros.lib.ftl" />
   		<@uniqueIdDiv>
   		 <#if mode?? && mode == "version">
			 <div class="document-versions">
			   <div class="document-details-panel document-versions">
				    <#if versions??>
				       <h2 id="${el}-heading" class="thin dark">${msg("header.versionHistory")}</h2>
	                  <div class="panel-body">
	                  	<h3 class="thin dark">${msg("section.latestVersion")}</h3>
	                     <div id="${el}-latestVersion" class="current-version version-list">
	                     	<#list versions as version>
						        <div class="version-panel-left">
						           <span class="document-version">${version.label}</span>
						        </div>
						        <div class="version-panel-right">
						           <h3 class="thin dark" > <a href="javascript:void(0);" onclick="reloadWithCompare('', '')">${version.name}</a>
						           </h3>
						           <div class="clear"></div>
						           <div class="version-details">
						              <div class="version-details-left">
						                <img class="icon" src ="${url.context}/proxy/alfresco/slingshot/profile/avatar/${version.creator.userName}/thumbnail/avatar32">
						              </div>
						              <div class="version-details-right">
							              <#assign modifyUser = node.properties["cm:modifier"]>
	     								  <#assign modifyDate = node.properties["cm:modified"]>
	     								  <#assign modifierLink = userProfileLink(version.creator.userName,version.creator.firstName+" "+version.creator.lastName, 'class="theme-color-1"', modifyUser.isDeleted!false) >
	                      					${msg("label.modified-by-user-on-date", modifierLink, "<span id='${el}-modifyDate'>${modifyDate.iso8601}</span>")} <br/>
											<#if version.description??>
												${version.description}
											<#else>
												<span class="faded">( ${msg("label.noComment")})</span>
											</#if>
						              </div>
						           </div>
						        </div>
						        <div class="clear"></div>
	   						 <#break>
						   </#list>
	                     </div>
	                     <hr />
	                     <h3 class="thin dark">${msg("section.olderVersion")}</h3>
	                     <div id="${el}-olderVersions" class="version-list">
						   	<#list versions as version>
						   	   <#if version?counter!=1 >
						        <div class="version-panel-left">
						           <span class="document-version">${version.label}</span>
						        </div>
						        <div class="version-panel-right">
						           <h3 class="thin dark" >${version.name}</h3>
						           <span class="actions">
						           	<a href="javascript:void(0);" class="compare" title="${msg("label.compare.text")}"
									   onclick="reloadWithCompare('${version.nodeRef}', 'text')">&nbsp;</a>
									<a href="javascript:void(0);" class="compare-overlay" title="${msg("label.compare.overlay")}"
									   onclick="reloadWithCompare('${version.nodeRef}', 'overlay')">&nbsp;</a>
						           </span>
						           <div class="clear"></div>
						           <div class="version-details">
						              <div class="version-details-left">
						                <img class="icon" src ="${url.context}/proxy/alfresco/slingshot/profile/avatar/${version.creator.userName}/thumbnail/avatar32">
						              </div>
						              <div class="version-details-right">
							              <#assign modifyUser = node.properties["cm:modifier"]>
	     								  <#assign modifyDate = node.properties["cm:modified"]>
	     								  <#assign modifierLink = userProfileLink(version.creator.userName,version.creator.firstName+" "+version.creator.lastName, 'class="theme-color-1"', modifyUser.isDeleted!false) >
	                      				  ${msg("label.modified-by-user-on-date", modifierLink, "<span id='${el}-modifyDate'>${modifyDate.iso8601}</span>")} <br/>
											<#if version.description??>
												${version.description}
											<#else>
												<span class="faded">( ${msg("label.noComment")})</span>
											</#if>
						              </div>
						           </div>
						        </div>				
						        <div class="clear"></div>
							  </#if>
						   </#list>
						  </div>
	                  </div>
					</#if>
		       </div>
	        </div>
	     </div>
		 <#else>
	   		<div id="hd">
		   		 <div class="quickshare-header">
		
		            <div class="quickshare-header-brand-colors">
		               <div class="brand-bgcolor-6"></div>
		               <div class="brand-bgcolor-5"></div>
		               <div class="brand-bgcolor-4"></div>
		               <div class="brand-bgcolor-3"></div>
		               <div class="brand-bgcolor-2"></div>
		               <div class="brand-bgcolor-1"></div>
		               <div class="clear"></div>
		            </div>
		
		            <div class="quickshare-header-left">
		               <img width="180" src="${url.context}/res/components/images/alfresco-logo.svg">
		            </div>
		
		            <div class="quickshare-header-right">
		               <@markup id="linkButtons">
		                  <#list linkButtons as linkButton>
		                     <a href="${linkButton.href}" class="brand-button ${linkButton.cssClass!""}" tabindex="0">${linkButton.label?html}</a>
		                  </#list>
		               </@markup>
		            </div>
		           
		
		            <div class="clear"></div>
		
		         </div>
		         <div class="yui-gc quickshare-node-header">
		               <div class="yui-u first">
		            <#-- Icon -->
		            <img src="${url.context}/res/components/images/filetypes/${fileIcon(displayName,48)}"
		                 onerror="this.src='${url.context}/res/components/images/filetypes/generic-file-48.png'"
		                 title="${displayName?html}" class="quickshare-node-header-info-thumbnail" width="48" /> 
		
		            <#-- Title -->
		            <h1 class="quickshare-node-header-info-title thin dark">${displayName?html} <span id="document-version" class="document-version">${item.version}</span> 
		              <#if compareDisplayName??>${msg("label.compareTo")} ${compareDisplayName} <span id="document-version" class="document-version">${compareItem.version}</span></#if></h1>
		   
		            <#-- 
		            <#assign modifyUser = node.properties["cm:modifier"]>
	             	<#assign modifyDate = node.properties["cm:modified"]>
	                <#assign modifierLink = userProfileLink(modifyUser.userName, modifyUser.displayName, 'class="theme-color-1"', modifyUser.isDeleted!false) >
		            <div>
		               ${msg("label.modified-by-user-on-date", modifierLink, "<span id='${el}-modifyDate'>${modifyDate.iso8601}</span>")}
		            </div>
		
					-->
		               </div>
		               <div class="yui-u quickshare-node-action"> 
		               <#assign saveButtonLabel = msg("button.save")>
		               <#if mode?? && mode == "sign" && signatureStatus != "Signed" >
		              	  <#assign saveButtonLabel = msg("actions.viewer.validate-signature")>
		                  <!-- Cancel Button -->
		                  <span class="yui-button yui-link-button">
		                     <span class="first-child">
		                        <a href="${returnUrl}" id="${el}-cancelButton" tabindex="1">${msg("button.cancel")}</a>
		                     </span>
		                  </span>
		               </#if>
		                  <!-- Save Button -->
		                  <span class="yui-button yui-link-button yui-button-disabled onDownloadDocumentClick">
		                     <span class="first-child">
		                        <a href="#" id="${el}-saveButton" tabindex="0">${saveButtonLabel}</a>
		                     </span>
		                  </span>
		               </div>
		
		         </div>
	         </div>
			 <div id="bd">
				   <div id="yui-main">
				         <div class="yui-b">
						   <div id="${el}-viewer" style="width: 100%; height: 800px; margin: 0 auto;"></div>
		               </div>
		           </div>
		        </div>
	     </#if>
   </@>
</@>

