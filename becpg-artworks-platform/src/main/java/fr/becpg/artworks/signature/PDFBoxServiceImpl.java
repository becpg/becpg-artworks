package fr.becpg.artworks.signature;

import java.io.IOException;

import org.alfresco.encryption.AlfrescoKeyStore;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * @author matthieu
 * Exemple https://github.com/apache/pdfbox/tree/trunk/examples/src/main/java/org/apache/pdfbox/examples/signature
 * 
 * MetadataEncryptor d'alfresco + iTEXT: 
 * https://github.com/rouxemmanuel/DigitalSigning/blob/master/DigitalSigningAlfresco/src/main/java/org/alfresco/plugin/digitalSigning/service/SigningService.java
 */
public class PDFBoxServiceImpl implements SignatureService{
	
	@Autowired
	@Qualifier("keyStore")
	private AlfrescoKeyStore mainKeyStore;
	
	
	
	private String signatureLocationInfo;
	private String signatureReasonInfo;
	private String signatureContactInfo;
	
	private String signatureAnnotationFieldName;
	
//	
//	private SignatureOptions signatureOptions;
//    private PDVisibleSignDesigner visibleSignDesigner;
//    private final PDVisibleSigProperties visibleSignatureProperties = new PDVisibleSigProperties();
//    private boolean lateExternalSigning = false;
//    private MemoryUsageSetting memoryUsageSetting = MemoryUsageSetting.setupMainMemoryOnly();
//    private PDDocument doc = null;
//    
//    
//    private Certificate[] certificateChain;
//    private String tsaUrl;
//    private boolean externalSigning;
    
    
    

//    @Override
//    public byte[] sign(InputStream content) throws IOException
//    {
//    	
//        Key privateKey = mainKeyStore.getKey("signature");
//      //  Certificate[] certChain = mainKeyStore.get.getCertificateChain("signature");
//    	
//        // cannot be done private (interface)
////        try
////        {
////            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
////            X509Certificate cert = (X509Certificate) certificateChain[0];
////            ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256WithRSA").build((PrivateKey) privateKey);
////            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer, cert));
////            gen.addCertificates(new JcaCertStore(Arrays.asList(certificateChain)));
////            CMSProcessableInputStream msg = new CMSProcessableInputStream(content);
////            CMSSignedData signedData = gen.generate(msg, false);
////            if (tsaUrl != null && tsaUrl.length() > 0)
////            {
////                ValidationTimeStamp validation = new ValidationTimeStamp(tsaUrl);
////                signedData = validation.addSignedTimeStamp(signedData);
////            }
////            return signedData.getEncoded();
////        }
////        catch (GeneralSecurityException | CMSException | OperatorCreationException e)
////        {
////            throw new IOException(e);
////        }
//        
//        return null;
//    }
    
    
    
	
	@Override
	public String checkoutDocument(NodeRef nodeRef) {
		return prepareForSignature(nodeRef, false);
	}
	
	@Override
	public void checkinDocument(NodeRef nodeRef) {
		
		
//		File inputFile; File signedFile; String tsaUrl; String signatureFieldName;
//		
//		if (inputFile == null || !inputFile.exists())
//        {
//            throw new IOException("Document for signing does not exist");
//        }
//
//        setTsaUrl(tsaUrl);
//
//        // creating output document and prepare the IO streams.
//        if (doc == null)
//        {
//            doc = Loader.loadPDF(inputFile, memoryUsageSetting);
//        }
//        
//        try (FileOutputStream fos = new FileOutputStream(signedFile))
//        {
//            int accessPermissions = SigUtils.getMDPPermission(doc);
//            if (accessPermissions == 1)
//            {
//                throw new IllegalStateException("No changes to the document are permitted due to DocMDP transform parameters dictionary");
//            }
//            // Note that PDFBox has a bug that visual signing on certified files with permission 2
//            // doesn't work properly, see PDFBOX-3699. As long as this issue is open, you may want to
//            // be careful with such files.
//
//            PDSignature signature;
//
//            // sign a PDF with an existing empty signature, as created by the CreateEmptySignatureForm example.
//            signature = findExistingSignature(doc, signatureFieldName);
//
//            if (signature == null)
//            {
//                // create signature dictionary
//                signature = new PDSignature();
//            }
//
//            // Optional: certify
//            // can be done only if version is at least 1.5 and if not already set
//            // doing this on a PDF/A-1b file fails validation by Adobe preflight (PDFBOX-3821)
//            // PDF/A-1b requires PDF version 1.4 max, so don't increase the version on such files.
//            if (doc.getVersion() >= 1.5f && accessPermissions == 0)
//            {
//                SigUtils.setMDPPermission(doc, signature, 2);
//            }
//
//            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm(null);
//            if (acroForm != null && acroForm.getNeedAppearances())
//            {
//                // PDFBOX-3738 NeedAppearances true results in visible signature becoming invisible 
//                // with Adobe Reader
//                if (acroForm.getFields().isEmpty())
//                {
//                    // we can safely delete it if there are no fields
//                    acroForm.getCOSObject().removeItem(COSName.NEED_APPEARANCES);
//                    // note that if you've set MDP permissions, the removal of this item
//                    // may result in Adobe Reader claiming that the document has been changed.
//                    // and/or that field content won't be displayed properly.
//                    // ==> decide what you prefer and adjust your code accordingly.
//                }
//                else
//                {
//                    System.out.println("/NeedAppearances is set, signature may be ignored by Adobe Reader");
//                }
//            }
//
//            // default filter
//            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
//
//            // subfilter for basic and PAdES Part 2 signatures
//            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
//
//            // this builds the signature structures in a separate document
//            visibleSignatureProperties.buildSignature();
//
//            signature.setName(visibleSignatureProperties.getSignerName());
//            signature.setLocation(visibleSignatureProperties.getSignerLocation());
//            signature.setReason(visibleSignatureProperties.getSignatureReason());
//            
//            // the signing date, needed for valid signature
//            signature.setSignDate(Calendar.getInstance());
//
//            // do not set SignatureInterface instance, if external signing used
//            SignatureInterface signatureInterface = isExternalSigning() ? null : this;
//
//            // register signature dictionary and sign interface
//            if (visibleSignatureProperties.isVisualSignEnabled())
//            {
//                signatureOptions = new SignatureOptions();
//                signatureOptions.setVisualSignature(visibleSignatureProperties.getVisibleSignature());
//                signatureOptions.setPage(visibleSignatureProperties.getPage() - 1);
//                doc.addSignature(signature, signatureInterface, signatureOptions);
//            }
//            else
//            {
//                doc.addSignature(signature, signatureInterface);
//            }
//
//            if (isExternalSigning())
//            {
//                ExternalSigningSupport externalSigning = doc.saveIncrementalForExternalSigning(fos);
//                // invoke external signature service
//                byte[] cmsSignature = sign(externalSigning.getContent());
//
//                // Explanation of late external signing (off by default):
//                // If you want to add the signature in a separate step, then set an empty byte array
//                // and call signature.getByteRange() and remember the offset signature.getByteRange()[1]+1.
//                // you can write the ascii hex signature at a later time even if you don't have this
//                // PDDocument object anymore, with classic java file random access methods.
//                // If you can't remember the offset value from ByteRange because your context has changed,
//                // then open the file with PDFBox, find the field with findExistingSignature() or
//                // PDDocument.getLastSignatureDictionary() and get the ByteRange from there.
//                // Close the file and then write the signature as explained earlier in this comment.
//                if (isLateExternalSigning())
//                {
//                    // this saves the file with a 0 signature
//                    externalSigning.setSignature(new byte[0]);
//
//                    // remember the offset (add 1 because of "<")
//                    int offset = signature.getByteRange()[1] + 1;
//
//                    // now write the signature at the correct offset without any PDFBox methods
//                    try (RandomAccessFile raf = new RandomAccessFile(signedFile, "rw"))
//                    {
//                        raf.seek(offset);
//                        raf.write(Hex.getBytes(cmsSignature));
//                    }
//                }
//                else
//                {
//                    // set signature bytes received from the service and save the file
//                    externalSigning.setSignature(cmsSignature);
//                }
//            }
//            else
//            {
//                // write incremental (only for signing purpose)
//                doc.saveIncremental(fos);
//            }
//        }
//        
//        // Do not close signatureOptions before saving, because some COSStream objects within
//        // are transferred to the signed document.
//        // Do not allow signatureOptions get out of scope before saving, because then the COSDocument
//        // in signature options might by closed by gc, which would close COSStream objects prematurely.
//        // See https://issues.apache.org/jira/browse/PDFBOX-3743
//        IOUtils.closeQuietly(signatureOptions);
//        IOUtils.closeQuietly(doc);
		
		
	}
	
	
	 // Find an existing signature (assumed to be empty). You will usually not need this.
    private PDSignature findExistingSignature(PDDocument doc, String sigFieldName)
    {
//        PDSignature signature = null;
//        PDSignatureField signatureField;
//        PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm(null);
//        if (acroForm != null)
//        {
//            signatureField = (PDSignatureField) acroForm.getField(sigFieldName);
//            if (signatureField != null)
//            {
//                // retrieve signature dictionary
//                signature = signatureField.getSignature();
//                if (signature == null)
//                {
//                    signature = new PDSignature();
//                    // after solving PDFBOX-3524
//                    // signatureField.setValue(signature)
//                    // until then:
//                    signatureField.getCOSObject().setItem(COSName.V, signature);
//                }
//                else
//                {
//                    throw new IllegalStateException("The signature field " + sigFieldName + " is already signed.");
//                }
//            }
//        }
//        return signature;
    	return null;
    }
	
	@Override
	public void cancelDocument(NodeRef nodeRef) {
		//TODO Cancel checkout
		
	}
	
	@Override
	public String getDocumentView(NodeRef nodeRef, String userId, String returnUrl) {
		return null;
	}
	
	@Override
	public String prepareForSignature(NodeRef nodeRef, boolean notifyByMail) {

        // Create a new document with an empty page.
        try (PDDocument document = new PDDocument())
        {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Adobe Acrobat uses Helvetica as a default font and
            // stores that under the name '/Helv' in the resources dictionary
          //  PDFont font = new PDType1Font(FontNam.HELVETICA);
            PDResources resources = new PDResources();
           // resources.put(COSName.HELV, font);

            // Add a new AcroForm and add that to the document
            PDAcroForm acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);

            // Add and set the resources and default appearance at the form level
            acroForm.setDefaultResources(resources);

            // Acrobat sets the font size on the form level to be
            // auto sized as default. This is done by setting the font size to '0'
            String defaultAppearanceString = "/Helv 0 Tf 0 g";
            acroForm.setDefaultAppearance(defaultAppearanceString);
            // --- end of general AcroForm stuff ---

            // Create empty signature field, it will get the name "Signature1"
            PDSignatureField signatureField = new PDSignatureField(acroForm);
            PDAnnotationWidget widget = signatureField.getWidgets().get(0);
            PDRectangle rect = new PDRectangle(50, 650, 200, 50);
            widget.setRectangle(rect);
            widget.setPage(page);

            // see thread from PDFBox users mailing list 17.2.2021 - 19.2.2021
            // https://mail-archives.apache.org/mod_mbox/pdfbox-users/202102.mbox/thread
            widget.setPrinted(true);

            page.getAnnotations().add(widget);

            acroForm.getFields().add(signatureField);

           // document.save(args[0]);
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "/artworks-viewer?nodeRef="+nodeRef.toString()+"&mode=sign";
	
	}
	
	
	


}
