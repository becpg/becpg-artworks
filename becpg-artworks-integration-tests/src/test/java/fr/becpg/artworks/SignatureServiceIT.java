package fr.becpg.artworks;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

import fr.becpg.artworks.signature.CertificateVerificationException;
import fr.becpg.artworks.signature.SignatureService;
import fr.becpg.artworks.signature.SignatureUtils;
import fr.becpg.artworks.signature.model.SignatureModel;

@RunWith(AlfrescoTestRunner.class)
public class SignatureServiceIT extends RepoBaseTest {

	private SignatureService signatureService;
	
	private static final String SIGNER_1 = "signer1";
	
	private static final String SIGNER_2 = "signer2";

	@Before
	@Override
	public void initialize() {
		super.initialize();
		ApplicationContext ctx = getApplicationContext();
		if (ctx != null) {
			Object signatureServiceBean = ctx.getBean("signatureService");
			if (signatureServiceBean instanceof SignatureService) {
				signatureService = (SignatureService) signatureServiceBean;
			}
		}
	}
	
	@Test
	public void testPrepareForSignature() throws InvalidTypeException, ContentIOException, InvalidNodeRefException, IOException, CertificateEncodingException, NoSuchAlgorithmException {
		NodeRef nodeRef = transactionHelper.doInTransaction(() -> {
			return createNodeWithContent(testFolder, "prepare-for-signature.pdf", "becpg/repo/document/input.pdf");
		}, false, true);

		NodeRef signer1 = getOrCreatePerson(SIGNER_1);
		nodeService.createAssociation(nodeRef, signer1, SignatureModel.ASSOC_RECIPIENTS);
		
		NodeRef signer2 = getOrCreatePerson(SIGNER_2);
		nodeService.createAssociation(nodeRef, signer2, SignatureModel.ASSOC_RECIPIENTS);
		
		NodeRef checkedOut = new NodeRef(signatureService.checkoutDocument(nodeRef));
		
		checkSignatureInformation(signer1, signer2, checkedOut, false, false, I18NUtil.getMessage("message.signature-status.inprogress"));

		byte[] file = contentService.getReader(checkedOut, ContentModel.PROP_CONTENT).getContentInputStream().readAllBytes();

		try (PDDocument document = PDDocument.load(file)) {
			
			for (int i = 0; i < document.getNumberOfPages() - 1; i++) {
				List<PDAnnotation> annotations = document.getPage(i).getAnnotations();
				
				assertEquals(2, annotations.size());
				
				List<String> annotationNames = new ArrayList<>();
				
				for (PDAnnotation annotation : annotations) {
					annotationNames.add(((COSString) annotation.getCOSObject().getItem("T")).getString());
				}
				
				assertTrue(annotationNames.contains(SIGNER_1));
				assertTrue(annotationNames.contains(SIGNER_2));
			}
			
			List<PDAnnotation> annotations = document.getPage(document.getNumberOfPages() - 1).getAnnotations();
			
			assertEquals(2, annotations.size());
			
			List<String> annotationNames = new ArrayList<>();
			
			for (PDAnnotation annotation : annotations) {
				annotationNames.add(((COSString) annotation.getCOSObject().getItem("T")).getString());
			}
			
			assertTrue(annotationNames.contains(SIGNER_1 + "-signature"));
			assertTrue(annotationNames.contains(SIGNER_2 + "-signature"));

		}
		
	}
	
	@Test
	public void testSignature() throws IOException, OperatorCreationException, CMSException, GeneralSecurityException, CertificateVerificationException, TSPException {

		NodeRef nodeRef = transactionHelper.doInTransaction(() -> {
			return createNodeWithContent(testFolder, "signature.pdf", "becpg/repo/document/input.pdf");
		}, false, true);
		
		NodeRef signer1 = getOrCreatePerson(SIGNER_1);
		nodeService.createAssociation(nodeRef, signer1, SignatureModel.ASSOC_RECIPIENTS);
		
		NodeRef signer2 = getOrCreatePerson(SIGNER_2);
		nodeService.createAssociation(nodeRef, signer2, SignatureModel.ASSOC_RECIPIENTS);

		byte[] originalFile = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT).getContentInputStream().readAllBytes();
		
		checkDocMDPPermissions(originalFile, 0);
		
		NodeRef workingCopy = signatureService.prepareForSignature(nodeRef, Arrays.asList(signer1), false);
		
		String view = signatureService.getDocumentView(nodeRef, SIGNER_1, null);
		
		assertEquals("artworks-viewer?nodeRef=" + workingCopy + "&mode=sign&returnUrl=/share/page/context/mine/document-details?nodeRef=" + nodeRef, view);
		
		signatureService.signDocument(workingCopy);
		
		byte[] signedFile = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT).getContentInputStream().readAllBytes();
		
		checkDocMDPPermissions(signedFile, 2);
		checkSignature(originalFile, signedFile, 1);

		checkSignatureInformation(signer1, null, nodeRef, true, false, I18NUtil.getMessage("message.signature-status.inprogress"));

		workingCopy = signatureService.prepareForSignature(nodeRef, Arrays.asList(signer2), false);

		signatureService.signDocument(workingCopy);

		signedFile = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT).getContentInputStream().readAllBytes();

		checkDocMDPPermissions(signedFile, 2);

		checkSignatureInformation(signer1, signer2, nodeRef, true, true, I18NUtil.getMessage("message.signature-status.signed"));
	
		checkSignature(originalFile, signedFile, 2);

	}
	
	@Test
	public void testCheckoutSignature() throws InvalidTypeException, ContentIOException, InvalidNodeRefException, IOException, OperatorCreationException, CMSException, GeneralSecurityException, CertificateVerificationException, TSPException {

		NodeRef nodeRef = transactionHelper.doInTransaction(() -> {
			return createNodeWithContent(testFolder, "signature.pdf", "becpg/repo/document/input.pdf");
		}, false, true);
		
		NodeRef signer1 = getOrCreatePerson(SIGNER_1);
		nodeService.createAssociation(nodeRef, signer1, SignatureModel.ASSOC_RECIPIENTS);
		
		NodeRef signer2 = getOrCreatePerson(SIGNER_2);
		nodeService.createAssociation(nodeRef, signer2, SignatureModel.ASSOC_RECIPIENTS);

		byte[] originalFile = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT).getContentInputStream().readAllBytes();
		
		checkDocMDPPermissions(originalFile, 0);
		
		NodeRef workingCopy = new NodeRef(signatureService.checkoutDocument(nodeRef));
		
		String view = signatureService.getDocumentView(nodeRef, SIGNER_1, null);
		
		assertEquals("artworks-viewer?nodeRef=" + workingCopy + "&mode=sign&returnUrl=/share/page/context/mine/document-details?nodeRef=" + nodeRef, view);
		
		signatureService.signDocument(workingCopy);
		
		byte[] signedFile = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT).getContentInputStream().readAllBytes();
		
		checkDocMDPPermissions(signedFile, 2);

		checkSignatureInformation(signer1, signer2, nodeRef, true, true, I18NUtil.getMessage("message.signature-status.signed"));

		checkSignature(originalFile, signedFile, 2);

	}

	private void checkSignatureInformation(NodeRef signer1, NodeRef signer2, NodeRef nodeRef, boolean signer1Signed, boolean signer2Signed, String statusMessage) throws CertificateEncodingException, NoSuchAlgorithmException {
		JSONObject recipientDataJson = new JSONObject();

	    Object recipientData = nodeService.getProperty(nodeRef, SignatureModel.PROP_RECIPIENTS_DATA);
	    		
	    if (recipientData instanceof String) {
	    	recipientDataJson = new JSONObject((String) recipientData);
	    }
	    
    	assertTrue(recipientDataJson.has("recipients"));
    	
    	JSONArray recipientsArray = recipientDataJson.getJSONArray("recipients");
    	
    	JSONObject signer1Json = null;
    	JSONObject signer2Json = null;
    	
    	for (int i = 0; i < recipientsArray.length(); i++) {
			if (recipientsArray.getJSONObject(i).get("nodeRef").toString().equals(signer1.toString())) {
				signer1Json = recipientsArray.getJSONObject(i);
			}
		}
    	
    	assertNotNull(signer1Json);
    	assertTrue(signer1Json.has("preparationDate"));
    	assertEquals(signer1Signed, signer1Json.has("signatureDate"));
    	assertEquals(signer1Signed, signer1Json.has("contentThumbprint"));
    	
    	if (signer2 != null) {
    		for (int i = 0; i < recipientsArray.length(); i++) {
    			if (recipientsArray.getJSONObject(i).get("nodeRef").toString().equals(signer2.toString())) {
    				signer2Json = recipientsArray.getJSONObject(i);
    			}
    		}
    		assertNotNull(signer2Json);
    		assertTrue(signer2Json.has("preparationDate"));
    		assertEquals(signer2Signed, signer2Json.has("signatureDate"));
    		assertEquals(signer2Signed, signer2Json.has("contentThumbprint"));
    	}
    	
	    String signatureStatus = (String) nodeService.getProperty(nodeRef, SignatureModel.PROP_STATUS);

    	assertEquals(statusMessage, signatureStatus);
    	
	}

	private void checkDocMDPPermissions(byte[] file, int expectedPermissions) throws IOException {
		
		int accessPermissions = -1;
		
		try (PDDocument document = PDDocument.load(file)) {
			accessPermissions = SignatureUtils.getMDPPermission(document);
		}
		
		assertEquals(expectedPermissions, accessPermissions);
	}

	private void checkSignature(byte[] origFile, byte[] signedFile, final int signatureCount) throws IOException, CMSException, OperatorCreationException, GeneralSecurityException, CertificateVerificationException, TSPException {
		int origPageCount;
		try (PDDocument document = PDDocument.load(origFile)) {
			// get string representation of pages COSObject
			origPageCount = document.getPages().getCount();
		}
		try (PDDocument document = PDDocument.load(signedFile)) {
			// PDFBOX-4261: check that object number stays the same
			Assert.assertEquals(origPageCount,document.getPages().getCount());

			 // early detection of problems in the page structure
            int p = 0;
            PDPageTree pageTree = document.getPages();
            for (PDPage page : document.getPages())
            {
                assertEquals(p, pageTree.indexOf(page));
                ++p;
            }
            
			List<PDSignature> signatureDictionaries = document.getSignatureDictionaries();
			if (signatureDictionaries.isEmpty()) {
				Assert.fail("no signature found");
			}
			
			int currentCount = 0;
			
			for (PDSignature sig : document.getSignatureDictionaries()) {
				
				COSString contents = (COSString) sig.getCOSObject().getDictionaryObject(COSName.CONTENTS);
				
				byte[] buf;
				
				try (ByteArrayInputStream fis = new ByteArrayInputStream(signedFile)) {
					buf = sig.getSignedContent(fis);
				}
				
				CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(buf), contents.getBytes());
				Store<X509CertificateHolder> certificatesStore = signedData.getCertificates();
				Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
				SignerInformation signerInformation = signers.iterator().next();
				
				@SuppressWarnings("unchecked")
				Collection<X509CertificateHolder> matches = certificatesStore.getMatches((Selector<X509CertificateHolder>) signerInformation.getSID());
				X509CertificateHolder certificateHolder = (X509CertificateHolder) matches.iterator().next();
				X509Certificate certFromSignedData = new JcaX509CertificateConverter().getCertificate(certificateHolder);
				
				Assert.assertEquals(SignatureUtils.getCertificateChain(null)[0], certFromSignedData);
				
				// CMSVerifierCertificateNotValidException means that the keystore wasn't valid
				// at signing time
				if (!signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certFromSignedData))) {
					Assert.fail("Signature verification failed");
				}
				
                TimeStampToken timeStampToken = SignatureUtils.extractTimeStampTokenFromSignerInformation(signerInformation);
                assertNotNull(timeStampToken);
                SignatureUtils.validateTimestampToken(timeStampToken);

                TimeStampTokenInfo timeStampInfo = timeStampToken.getTimeStampInfo();

                // compare the hash of the signed content with the hash in the timestamp
                byte[] tsMessageImprintDigest = timeStampInfo.getMessageImprintDigest();
                String hashAlgorithm = timeStampInfo.getMessageImprintAlgOID().getId();
                byte[] sigMessageImprintDigest = MessageDigest.getInstance(hashAlgorithm).digest(signerInformation.getSignature());
                assertArrayEquals("timestamp signature verification failed", sigMessageImprintDigest, tsMessageImprintDigest);                    

                Store<X509CertificateHolder> tsCertStore = timeStampToken.getCertificates();

                // get the certificate from the timeStampToken
                @SuppressWarnings("unchecked") // TimeStampToken.getSID() is untyped
                Collection<X509CertificateHolder> tsCertStoreMatches = tsCertStore.getMatches(timeStampToken.getSID());
                X509CertificateHolder certHolderFromTimeStamp = tsCertStoreMatches.iterator().next();
                X509Certificate certFromTimeStamp = new JcaX509CertificateConverter().getCertificate(certHolderFromTimeStamp);

                SignatureUtils.checkTimeStampCertificateUsage(certFromTimeStamp);
                
                currentCount++;
			}
			
			assertEquals(signatureCount, currentCount);
		}
	}

}
