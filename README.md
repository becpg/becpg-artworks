# beCPG Artworks – Annotation, Comparison and Digital Signature for Alfresco

[![License: LGPL v3](https://img.shields.io/badge/License-LGPL_v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3+-red.svg)](https://maven.apache.org/)
[![GitHub issues](https://img.shields.io/github/issues/becpg/becpg-artworks)](https://github.com/becpg/becpg-artworks/issues)
[![GitHub stars](https://img.shields.io/github/stars/becpg/becpg-artworks)](https://github.com/becpg/becpg-artworks/stargazers)
[![Documentation](https://img.shields.io/badge/docs-docs.becpg.net-brightgreen)](https://docs.becpg.fr)
[![Website](https://img.shields.io/badge/website-becpg.net-blue)](http://www.becpg.net)

---

## 🚀 What is beCPG Artworks?

**beCPG Artworks** is an addon for **Alfresco Content Services** that provides advanced document handling features. 
It can be used as a standalone Alfresco extension or seamlessly integrated into **beCPG PLM**.

It empowers users to annotate, compare, and digitally sign documents directly within the Alfresco Share interface.

👉 [**Request a Live Demo**](https://www.becpg.net/community/) | [**Contact Us**](https://www.becpg.net/contact-us/)

---

## 📸 Screenshots

### 🖋️ Annotation Feature
![Annotation](docs/images/annotation3.png)
*Collaborative annotation on PDF documents*

<p align="center">
  <img src="docs/images/text-comparison.png" width="30%" alt="Text Comparison" />
  <img src="docs/images/image-comparison.png" width="30%" alt="Image Comparison" />
  <img src="docs/images/colors.png" width="30%" alt="Color Extraction" />
</p>
<p align="center">
  <img src="docs/images/layers.png" width="45%" alt="Layers" />
  <img src="docs/images/measure.png" width="45%" alt="Measurements" />
</p>

### 🔍 Comparison Feature
![Comparison](docs/images/comparison.png)
*Side-by-side comparison of document versions*

<p align="center">
  <img src="docs/images/comparison2.png" width="45%" alt="Comparison 2" />
  <img src="docs/images/comparison3.png" width="45%" alt="Comparison 3" />
</p>

### ✍️ Digital Signature Feature
<p align="center">
  <img src="docs/images/signature1.png" width="22%" alt="Add aspect" />
  <img src="docs/images/signature2.png" width="22%" alt="Add recipients" />
  <img src="docs/images/signature3.png" width="22%" alt="Send for signature" />
  <img src="docs/images/signature4.png" width="22%" alt="Signed document" />
</p>
*Step-by-step digital signature process*

---

## 💡 Key Features

### 🖋️ Annotation
Annotation feature is provided with two implementations:
* **Standalone using [PDFTron](https://www.pdftron.com/)**:
  * Add annotations, track text changes, visualize image changes.
  * Extract colors, layers, and perform measurements.
* **External using [Kami](https://www.kamiapp.com/)** annotation manager.

### 🔍 Comparison
Enables image and PDF comparison between 2 versions of a document.
* PDF text and image comparison are provided with standalone PDFTron Viewer.
* Available directly from the document version view.

### ✍️ Digital Signature
Internal signature using **PDFTron** and **PDFBox**, or external via **DocuSign** or **YouSign**.
1. Add signature aspect to the document.
2. Manage recipients and send for signature.
3. Automated email notifications and signed document check-in.

---

## 🛠️ Installation

### Prerequisites
- Alfresco Community 25.3+
- Java 17+
- Maven 3.3.0+

### Build
This project uses Alfresco SDK 4.2.
```bash
./run.sh build_start
```

### Install
1. Download the two **AMPs** (platform & share) from the [releases](https://github.com/becpg/becpg-artworks/releases).
2. Install them into your Alfresco instance:
   ```bash
   java -jar alfresco-mmt.jar install becpg-artworks-platform.amp webapps/alfresco.war -force
   java -jar alfresco-mmt.jar install becpg-artworks-share.amp webapps/share.war -force
   ```

---

## ⚙️ Configuration

Refer to the [Documentation](https://docs.becpg.fr) for detailed setup of:
- **DocuSign / Kami / PDFTron** licenses and tokens.
- **Keystore** configuration for internal digital signatures.
- **JVM System Properties** for secure password management.

---

## 🤝 Community & Contributing

* [Contributing Guidelines](CONTRIBUTING.md)
* [Code of Conduct](CODE_OF_CONDUCT.md)
* [Security Policy](SECURITY.md)
* [AI Assistant Guidelines](AGENTS.md)

---

## 📩 Contact

Interested in trying **beCPG PLM** or this addon?

* 🌐 [Website](https://www.becpg.net)
* 📖 [Documentation](https://docs.becpg.fr)
* 🎮 [Live Demo](https://www.becpg.net/community/)
* ✉️ [Contact Us](https://www.becpg.net/contact-us/)

---
⚡ **beCPG Artworks – Enhance your document lifecycle with annotation and digital signatures.**
