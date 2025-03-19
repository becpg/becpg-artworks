# Welcome to the beCPG Artworks Addon


<p align="center">
  <img title="becpg" alt='becpg' src='docs/images/becpg.png' ></img>
</p>

## What is beCPG Artworks Addon?

This addon provides additional features to Alfresco Content Service.

It can be used standalone or included in beCPG PLM.

beCPG is an open source Product Lifecycle Management (PLM) software designed to manage food, cosmetics and CPG products.
 It helps to accelerate innovation and reduce time-to-market while improving product quality.
https://www.becpg.fr/

### Annotation feature

Annotation feature is provided with two implementations:
 * Standalone using  [PDFTron](https://www.pdftron.com/) proprietary library, providing actions upon PDF document :
   * Add annotations
		![](docs/images/annotation3.png)
   * Track text changes
		![](docs/images/text-comparison.png)
   * Visualize image changes
		![](docs/images/image-comparison.png)
   * Extract colors
		![](docs/images/colors.png)
   * Extract layers
		![](docs/images/layers.png)
   * Perform measurement
		![](docs/images/measure.png)

(To use pdftron you have to download it and place it to
 /becpg-artworks-share/src/mains/assembly/web/components/artworks-viewer/pdftron/ See Install instructions in pdftron folder)


 * Or external using [Kami](https://www.kamiapp.com/) annotation manager 

![](docs/images/annotation.png) ![](docs/images/annotation2.png)


### Comparison feature


Enables comparison button in document version view

This enables image and PDF comparison between 2 versions of a document.

![](docs/images/comparison.png) ![](docs/images/comparison2.png) ![](docs/images/comparison3.png)

PDF text and image comparison feature are also provided with standalone PDFTron Viewer


### Digital signature feature

Digital signature is provided internally using [PDFTron](https://www.pdftron.com/) Library and [PDFBox](https://pdfbox.apache.org/) or externally using [Docusign](https://www.docusign.com/):  

1) add the signature aspect on the document
![](docs/images/signature1.png)

2) add recipients
![](docs/images/signature2.png)

3) send for signature
![](docs/images/signature3.png)

3) wait for the recipients to sign the document (you will receive an email when it's done)

4) check in the signed document
![](docs/images/signature4.png)

For details on how to use internal signatures refers to API


## Installation

### Prerequisites

-  Alfresco 7

### Build

This project uses Alfresco SDK 4.2. Refers to Alfresco SDK documentation to see how-to build and run project


### Install

 * Download the two **AMPs** provided by the **becpg-artworks** release

 * Install the two **AMPs** into your content service by running

   ```
    java -jar /root/alfresco-mmt.jar install /root/amp/ webapps/alfresco -nobackup -directory -force
   ```

   

For internal digital signature you have to install additional certificate in alfresco [keystore](https://docs.alfresco.com/content-services/latest/admin/security/#managealfkeystores):

Digital signature use the  alfresco **encryption.keystore.location** to store is certificate.

To install existing **pkcs12** certificates into default alfresco **keystore** use the following commands:

```bash
keytool -importkeystore \
        -deststorepass mp6yc0UD9e -destkeypass oKIWzVdEdA -destkeystore /usr/local/tomcat/shared/classes/alfresco/extension/keystore \
        -srckeystore my-cert.p12 -srcstoretype PKCS12  -deststoretype JCEKS -srcstorepass [p12-cert-password] \
        -alias [some-alias]
```


To create a new certificate for test purpose:

```bash
  keytool -genkeypair -alias [some-alias] -validity 365 -keyalg RSA -keysize 2048 -keypass oKIWzVdEdA -storetype JCEKS -keystore /usr/local/tomcat/shared/classes/alfresco/extension/keystore/keystore -storepass mp6yc0UD9e     
```




### Setup

Enable **DocuSign** signature by providing API access token:

```properties
 beCPG.signatureAuthorization=accountId;access_token
```

Enable **Kami** external PDF annotation by providing API access token:

```properties
 beCPG.annotationAuthorization=token
```

Enable **PDFTron** internal PDF annotation by proving licence key:

```properties
 beCPG.annotationViewerLicenseKey=licenceKey
```

Enable internal digital signature with **PDFBox** (**PDFTron** annotation viewer is required) 

```properties
 beCPG.signature.reasonInfo=Digitally signed with beCPG
 beCPG.signature.tsaUrl=https://freetsa.org/tsr
 beCPG.signature.keystore.alias=[some-alias]
 beCPG.signature.keystore.password=[some-password]
```

To configure signature certificate use the new way of specifying the configuration in JVM system properties (Don't store password on properties files) :

```yml
JAVA_TOOL_OPTIONS: "
 -DbeCPG.signature.keystore.alias=[some-alias]
 -DbeCPG.signature.keystore.password=[some-password]
"
```

Note: If left empty or not provided, features are not enabled.

Internal signature support is best integrated with beCPG PLM. If you want to use it out of box beCPG provides several JavaScript Helpers. 


## API

**beCPG Arworks** expose some JavaScript API function in order to use digital signature under the **bSign** root object

| Method                                            | Description                                                  |
| ------------------------------------------------- | ------------------------------------------------------------ |
| prepareForSignature(document, recipients, params) | ***document*** is the ScriptNode of the PDF document you want to sign, ***recipients*** is an array of ScriptNode representing the current recipients for whom the document will be prepared, ***params*** is an optional parameter which is a JSON string, the values change the signature positioning, size, and string anchors for the signature |
| signDocument(document)                            | Digitally sign the document for the prepared recipients      |
| getSignatureView(document, userName,  task)       | Returns the signature viewer URL (must be prefixed with HOST:PORT/share/page/context/mine/) |

### **Signature Properties**  
The `params` object holds details about the signature placement on the document.  

| **Field**         | **Type** | **Description**                                              |
| ----------------- | -------- | ------------------------------------------------------------ |
| `page`            | String   | The page number where the signature should be placed.        |
| `width`           | Integer  | The width of the signature.                                  |
| `height`          | Integer  | The height of the signature.                                 |
| `direction`       | String   | Direction of signature placement. Possible values: `"right"`, `"left"`, `"up"`, `"down"`. |
| `gap`             | Integer  | The gap between multiple signatures if applicable.           |
| `fromLeftRatio`   | Integer  | The horizontal positioning ratio from the left.              |
| `fromBottomRatio` | Integer  | The vertical positioning ratio from the bottom.              |
| `anchor`          | Object   | Specifies an anchor keyword-based placement (optional).      |
| `keyword`         | String   | The keyword that serves as an anchor point.                  |
| `xPosition`       | String   | Horizontal position relative to the keyword. Possible values: "left", "middle", "right". |
| `yPosition`       | String   | Vertical position relative to the keyword. Possible values: "top", "middle", "bottom". |

The JSON params has the following structure:

````json
{
  "signature": {
    "page": "string",
    "width": "integer",
    "height": "integer",
    "direction": "string",
    "gap": "integer",
    "fromLeftRatio": "integer",
    "fromBottomRatio": "integer",
    "anchor": {
      "keyword": "string",
      "xPosition": "string",
      "yPosition": "string"
    }
  },
  "initials": {
    "disable": "boolean",
    "width": "integer",
    "height": "integer",
    "direction": "string",
    "gap": "integer",
    "fromLeftRatio": "integer",
    "fromBottomRatio": "integer",
    "anchor": {
      "keyword": "string",
      "xPosition": "string",
      "yPosition": "string"
    }
  }
}
````

And these are the default values (if not provided):

```json
{
  "signature": {
    "page": "last",
    "width": 200,
    "height": 75,
    "direction": "up",
    "gap": 60,
    "fromLeftRatio": 60,
    "fromBottomRatio": 10,
    "anchor": {
      "keyword": null,
      "xPosition": null,
      "yPosition": null
    }
  },
  "initials": {
    "disable": false,
    "width": 100,
    "height": 35,
    "direction": "up",
    "gap": 30,
    "fromLeftRatio": 75,
    "fromBottomRatio": 10,
    "anchor": {
      "keyword": null,
      "xPosition": null,
      "yPosition": null
    }
  }
}
```

#### Example:

```javascript
var document = search.findNode("workspace://SpacesStore/1748e487-a0e7-4931-88e4-87a0e7393118");
document.createAssociation(person, "sign:recipients"); // you need to set all recipients
var recipients = []; // the recipients that will be prepared for signature (all if empty)
var params = {
	signature: {
		width: 200,
		height: 50,
		fromLeftRatio: 50,
		fromBottomRatio: 75
	},
	initials: {
		anchor: {
			keyword: "Page",
			xPosition: "right",
			yPosition: "bottom"
		},
		width: 80,
		height: 60,
	}
}
bSign.prepareForSignature(document, recipients, JSON.stringify(params));
```

### Use case

  You can call the beCPG Signature API inside your scripts. Here is a example of workflow using scripts :

  1. Upload a PDF document to ACS
  2. Add the signature aspect to it, with some "sign:recipients" associations which are the recipients (alfresco accounts)
  3. Call the "prepareForSignature" method  ` bSign.prepareForSignature(document, recipients, params) ` 
  Note that the chosen **recipients** must belong to the recipients defined earlier. The signature preparation can be done for each recipient or for all recipients at once, depending on your process
  4. Call the "getSignatureView" method ` bSign.getSignatureView(document, null, null) ` which will return a URL (it must be prefixed with "HOST:PORT/share/page/context/mine/")
  5. Once you enter the viewer, you can see the annotations that need to be signed. Please sign all the annotations before Saving the document
  6. Once the document is saved with the viewer, you need to complete the signature by calling the method "signDocument"
    ` bSign.signDocument(document) ` which will digitally sign the document for the prepared recipients

Example :

```javascript
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
```

Here the recipient1 must sign the document with the viewer and save it.

```javascript
// after recipient1 signed the document with the viewer : sign the document digitally for recipient1
bSign.signDocument(document);

// prepare signature of recipient2
var preparedRecipient = [];
preparedRecipient.push(recipient2);
bSign.prepareForSignature(document, preparedRecipient);

// get the signature view for recipient2
var url = bSign.getSignatureView(document,null,null);
```

Here the recipient2 must sign the document with the viewer and save it.

```javascript
// after recipient2 signed the document with the viewer : sign the document digitally for recipient2
bSign.signDocument(document);
```

