# beCPG Artworks

This addon provides additional features to your Alfresco Content Service.

It can be use standalone or included in beCPG PLM.
beCPG is an open source Product Lifecycle Management (PLM) software designed to manage food, cosmetics and CPG products. It helps to accelerate innovation and reduce time-to-market while improving product quality.
https://www.becpg.fr/

# Internal signature feature :

### Installation
  
  In order to use the internal signature tool provided by beCPG, you need to
  
  * Provide a PDFTron license key for the viewer by setting the property "beCPG.annotationViewerLicenseKey"
  * Generate a signature certificate and install it into the platform alfresco keystore "/usr/local/tomcat/shared/classes/alfresco/extension/keystore"
  	You can for example mount a volume pointing towards this location in your docker-compose file : 
  	`- becpg-artworks-keystore-volume:/usr/local/tomcat/shared/classes/alfresco/extension/keystore` And then create a certificate for testing using the command line :  `sudo keytool -genkeypair -alias mykey -validity 365 -keyalg RSA -keysize 1024 -keypass mykeypass -storetype jceks -keystore /var/lib/docker/volumes/becpg_4_0_becpg_keystore_volume/_data/keystore -storepass XXXXX`
  * Provide the "alias" and "password" of your certificate to the system by setting the properties "beCPG.signature.keystore.alias" and "beCPG.signature.keystore.password"
  Here for example : 
  
 	 	` - DbeCPG.signature.keystore.alias=mykey -DbeCPG.signature.keystore.password=mykeypass `

### Usage

  You can call the beCPG Signature API inside your scripts. Here is a example of workflow using scripts :
  
  1. Upload a PDF document to ACS
  2. Add the signature aspect to it, with some "sign:recipients" associations which are the recipients (alfresco accounts)
  3. Call the "prepareForSignature" method
  ` bSign.prepareForSignature(document, recipients, params) ` 
  document is the ScriptNode of the PDF document you want to sign, recipients is an array of ScriptNode representing the current recipients (they must belong to the recipients defined earlier. The signature preparation can be done for each recipient or for all recipients at once, depending on your process), params is an optional parameters which is an array of strings, the values change the signature positioning, size, and string anchors for the signature
  4. Call the "getSignatureView" method ` bSign.getSignatureView(document, null, null) ` which will return a URL (it must be prefixed with "https://HOST:PORT/share/page/context/mine/") example : http://localhost:8180/share/page/context/mine/artworks-viewer?nodeRef=workspace://SpacesStore/eba3017f-176b-4316-aafd-c2037c477cae&mode=sign&returnUrl=/share/page/context/mine/document-details?nodeRef=workspace://SpacesStore/38a2e156-73c0-47d9-8efd-24c84782c084
  5. Once you enter the viewer, you can see the annotations that need to be signed. Please sign all the annotations before Saving the document
  6. Once the document is saved with the viewer, you need to complete the signature by calling the method "signDocument"
  ` bSign.signDocument(document) ` which will digitally sign the document for the prepared recipients
	
Example :

	var document = search.findNode("workspace://SpacesStore/38a2e156-73c0-47d9-8efd-24c84782c084");
	var recipient1 = search.findNode("workspace://SpacesStore/16c4ef69-aab4-4017-9d84-d3e1b526086f");
	var recipient2 = search.findNode("workspace://SpacesStore/b4273aa7-b66c-4cfe-81c0-02f95a26d7af");
	
	// create the recipients associations
	document.createAssociation(recipient1, "sign:recipients");
	document.createAssociation(recipient2, "sign:recipients");
	
	// prepare signature of recipient1
	var preparedRecipient = [];
	preparedRecipient.push(recipient1);
	bSign.prepareForSignature(document, preparedRecipient);
	
	// get the signature view for recipient1
	var url = bSign.getSignatureView(document,null,null);

Here the recipient1 must sign the document with the viewer and save it.

	// after recipient1 signed the document with the viewer : sign the document digitally for recipient1
	bSign.signDocument(document);
	
	// prepare signature of recipient2
	var preparedRecipient = [];
	preparedRecipient.push(recipient2);
	bSign.prepareForSignature(document, preparedRecipient);
	
	// get the signature view for recipient2
	var url = bSign.getSignatureView(document,null,null);

Here the recipient2 must sign the document with the viewer and save it.

	// after recipient2 signed the document with the viewer : sign the document digitally for recipient2
	bSign.signDocument(document);


Parameters :

The signature feature is designed so that each page of the document will contain a signature field, either a "final signature" field, either a "initials" signature. You can define in which page you want to see the final signature, among other things.

The "params" of the "prepareForSignature" method have default values if nothing is set. Here is the default params structure :

	prepareForSignature(document, recipients) = prepareForSignature(document, recipients, "last", "100,50,1,150,300,300", "signature,2,0", "50,25,3,30,150,150", "Page,0,2")
	
 * "last" : defines the page number of the final signature. It can be a number ("1", "14", ...) or a number from the end ("last", "last-2", ...)
 * "100,50,1,150,300,300" : defines the signature field dimensions (width,height,direction(1=right,2=left,3=up,4=down),gap,rightMargin,bottomMargin). Direction is the direction of the multiple fields on each page (if there are multiple recipients). The margins are here to positionate the first field in case of a non-matching keyword
 * "signature,2,0" : defines the signature field anchor information (keyWord,xposition(0=left,1=middle,2=right),yposition(0=bottom,1=middle,2=top))
 * "50,25,3,30,150,150" : defines the initials field dimensions (width,height,direction(1=right,2=left,3=up,4=down),gap,rightMargin,bottomMargin)
 * "Page,0,2" : defines the initials field anchor information (keyWord,xposition(0=left,1=middle,2=right),yposition(0=bottom,1=middle,2=top))



# Annotation feature : provides an annotation tool for PDF files
  * In order to use the pdf file annotation feature, you need to provide a Kami license key by setting the property "beCPG.annotationAuthorization" under docker-compose.override.yml (sample file provided)
  
![](doc-images/annotation.png) ![](doc-images/annotation2.png)

# Comparison feature : enables comparison button in document version view

This enables image and PDF comparison between 2 versions of a document.

![](doc-images/comparison.png) ![](doc-images/comparison2.png) ![](doc-images/comparison3.png)

# Document signature feature : provides a signing tool with DocuSign
  * In order to use the signature feature, you need to provide a DocuSign accountId and a valid access_token by setting the property "beCPG.signatureAuthorization" under docker-compose.override.yml (-DbeCPG.signatureAuthorization='accountId;access_token')

1) add the signature aspect on the document
![](doc-images/signature1.png)

2) add recipients
![](doc-images/signature2.png)

3) send for signature
![](doc-images/signature3.png)

3) wait for the recipients to sign the document (you will receive an email when it's done)

4) checkin the signed document
![](doc-images/signature4.png)



# Setup
 
 * download the two AMPs provided by the becpg-artworks release
 * install the two AMPs into your content service by running "java -jar /root/alfresco-mmt.jar install /root/amp/ webapps/alfresco -nobackup -directory -force" in your Dockerfile
 
 
# Build

This is an All-In-One (AIO) project for Alfresco SDK 4.2.

Run with `./run.sh build_start` or `./run.bat build_start` and verify that it

 * Runs Alfresco Content Service (ACS)
 * Runs Alfresco Share
 * Runs Alfresco Search Service (ASS)
 * Runs PostgreSQL database
 * Deploys the JAR assembled modules
 
All the services of the project are now run as docker containers. The run script offers the next tasks:

 * `build_start`. Build the whole project, recreate the ACS and Share docker images, start the dockerised environment composed by ACS, Share, ASS and 
 PostgreSQL and tail the logs of all the containers.
 * `build_start_it_supported`. Build the whole project including dependencies required for IT execution, recreate the ACS and Share docker images, start the 
 dockerised environment composed by ACS, Share, ASS and PostgreSQL and tail the logs of all the containers.
 * `start`. Start the dockerised environment without building the project and tail the logs of all the containers.
 * `stop`. Stop the dockerised environment.
 * `purge`. Stop the dockerised container and delete all the persistent data (docker volumes).
 * `tail`. Tail the logs of all the containers.
 * `reload_share`. Build the Share module, recreate the Share docker image and restart the Share container.
 * `reload_acs`. Build the ACS module, recreate the ACS docker image and restart the ACS container.
 * `build_test`. Build the whole project, recreate the ACS and Share docker images, start the dockerised environment, execute the integration tests from the
 `integration-tests` module and stop the environment.
 * `test`. Execute the integration tests (the environment must be already started).

