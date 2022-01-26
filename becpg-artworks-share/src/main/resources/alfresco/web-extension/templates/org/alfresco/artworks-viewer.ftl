<#include "include/alfresco-template.ftl" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="${locale}" xml:lang="${locale}">
<head>
   <title><@region id="head-title" scope="global" chromeless="true"/></title>
  <meta
      name="viewport"
      content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no"
    />
    
	   <!-- Icons -->
	   <link rel="shortcut icon" href="${url.context}/res/favicon.ico" type="image/vnd.microsoft.icon" />
	   <link rel="icon" href="${url.context}/res/favicon.ico" type="image/vnd.microsoft.icon" />

 
   <@outputJavaScript/>
   <@outputCSS/>
   
</head>

<@templateBody>
   <@region id="artworks-viewer" scope="template" />
</@>

