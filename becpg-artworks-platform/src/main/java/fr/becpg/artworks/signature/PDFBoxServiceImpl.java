package fr.becpg.artworks.signature;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTerminalField;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.becpg.artworks.helper.NodeContentHelper;
import fr.becpg.artworks.signature.model.SignatureContext;
import fr.becpg.artworks.signature.model.SignatureModel;
import fr.becpg.artworks.signature.model.SignatureStatus;
import jakarta.xml.bind.DatatypeConverter;

/**
 * 
 * @author valentin
 * 
 */
@Service
public class PDFBoxServiceImpl implements SignatureService {
	
	private static final String NODE_REF = "nodeRef";

	private static final String RECIPIENTS = "recipients";

	private static final Log logger = LogFactory.getLog(PDFBoxServiceImpl.class);

	private static final int RIGHT_DIRECTION = 1;
	private static final int LEFT_DIRECTION = 2;
	private static final int UP_DIRECTION = 3;
	private static final int DOWN_DIRECTION = 4;
	
	private static final int LEFT_POSITION = 0;
	private static final int MIDDLE_POSITION = 1;
	private static final int RIGHT_POSITION = 2;
	private static final int BOTTOM_POSITION = 0;
	private static final int TOP_POSITION = 2;
	
	private static final int SIGNATURE_SIZE = 0x5000;

	private CheckOutCheckInService checkOutCheckInService;
	
	private ContentService contentService;
	
	private NodeService nodeService;
	
	private BehaviourFilter policyBehaviourFilter;
	
	private AuthorityService authorityService;

	@Value("${beCPG.signature.reason}")
	private String signatureReason;
	
	@Value("${beCPG.signature.location}")
	private String signatureLocation;
	
	@Value("${beCPG.signature.contactInfo}")
	private String signatureContactInfo;
	
	@Value("${beCPG.signature.tsaUrl}")
	private String tsaUrl;
	
	@Value("${beCPG.signature.keystore.alias}")
	private String alias;
	
	@Value("${beCPG.signature.keystore.password}")
	private String password;
	
	private PersonService personService;
	
	private NodeContentHelper nodeContentHelper;
	
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}
	
	public void setNodeContentHelper(NodeContentHelper nodeContentHelper) {
		this.nodeContentHelper = nodeContentHelper;
	}
	
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}
	
    public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
		this.checkOutCheckInService = checkOutCheckInService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}
	
	@Override
	public String checkoutDocument(NodeRef nodeRef) {
		
		if (!nodeService.hasAspect(nodeRef, SignatureModel.ASPECT_SIGNATURE)) {
			nodeService.addAspect(nodeRef, SignatureModel.ASPECT_SIGNATURE, null);
		}
		
		List<AssociationRef> recipientAssocs = nodeService.getTargetAssocs(nodeRef, SignatureModel.ASSOC_RECIPIENTS);

		if (recipientAssocs.isEmpty()) {
			NodeRef currentUser = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
			nodeService.createAssociation(nodeRef, currentUser, SignatureModel.ASSOC_RECIPIENTS);
		}
		
		return prepareForSignature(nodeRef, new ArrayList<>(), false).toString();
	}
	
	@Override
	public NodeRef checkinDocument(NodeRef nodeRef) {
		return signDocument(nodeRef);
	}

	@Override
	public NodeRef prepareForSignature(NodeRef originalNode, List<NodeRef> recipients, boolean notifyByMail, String... params) {
		
		try {
			
			policyBehaviourFilter.disableBehaviour(SignatureModel.ASPECT_SIGNATURE);
			
			if (logger.isDebugEnabled()) {
				logger.debug("prepareForSignature : originalNode = " + originalNode + ", recipients = " + recipients + ", params = " + (params == null ? params : Arrays.asList(params)));
			}
			
			List<NodeRef> nodeRecipients = new ArrayList<>();
			
			nodeService.getTargetAssocs(originalNode, SignatureModel.ASSOC_RECIPIENTS).forEach(assoc -> nodeRecipients.addAll(extractPeopleFromGroup(assoc.getTargetRef())));
			
			// if no recipient set : take all the recipients from node aspect
			if (recipients.isEmpty()) {
				recipients.addAll(nodeRecipients);
			}
			
			SignatureContext context = buildSignatureContext(nodeRecipients, recipients, params);
			
			if (logger.isDebugEnabled()) {
				logger.debug(context.toString());
			}
		
		
			updatePreparationInformation(originalNode, context);
			
			for (NodeRef recipient : recipients) {
				nodeService.createAssociation(originalNode, recipient, SignatureModel.ASSOC_PREPARED_RECIPIENTS);
			}
			
			NodeRef workingCopyNode = checkOutCheckInService.checkout(originalNode);
			
			InputStream originalContentInputStream = contentService.getReader(originalNode, ContentModel.PROP_CONTENT).getContentInputStream();
			
			byte[] preparedDocument = prepareForSignature(originalContentInputStream, context);
			
			nodeContentHelper.writeContent(workingCopyNode, preparedDocument);
			
			return workingCopyNode;
		} catch (IOException e) {
			String documentName = (String) nodeService.getProperty(originalNode, ContentModel.PROP_NAME);
			throw new SignatureException("Error while preparing signature for '" + documentName + "' : " + e.getMessage());
		} finally {
			policyBehaviourFilter.enableBehaviour(SignatureModel.ASPECT_SIGNATURE);
		}
	    
	}
	
	@Override
	public NodeRef signDocument(NodeRef nodeRef) {

		try {

			policyBehaviourFilter.disableBehaviour(SignatureModel.ASPECT_SIGNATURE);

			NodeRef signingNode = nodeRef;

			boolean checkin = false;

			if (nodeService.hasAspect(signingNode, ContentModel.ASPECT_WORKING_COPY)) {
				checkin = true;
			} else {
				List<AssociationRef> workingCopyAssocs = nodeService.getTargetAssocs(signingNode, ContentModel.ASSOC_WORKING_COPY_LINK);

				if (!workingCopyAssocs.isEmpty()) {
					signingNode = workingCopyAssocs.get(0).getTargetRef();
					checkin = true;
				}
			}

			List<AssociationRef> preparedRecipients = nodeService.getTargetAssocs(signingNode, SignatureModel.ASSOC_PREPARED_RECIPIENTS);

			for (AssociationRef preparedRecipient : preparedRecipients) {
				sign(signingNode, preparedRecipient.getTargetRef());
			}

			if (checkin) {
				nodeRef = checkOutCheckInService.checkin(signingNode, null);

				for (AssociationRef preparedRecipient : preparedRecipients) {
					nodeService.removeAssociation(nodeRef, preparedRecipient.getTargetRef(), SignatureModel.ASSOC_PREPARED_RECIPIENTS);
				}
			}

		} finally {
			policyBehaviourFilter.enableBehaviour(SignatureModel.ASPECT_SIGNATURE);
		}

		return nodeRef;
	}

	@Override
	public NodeRef cancelDocument(NodeRef nodeRef) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("cancelDocument : nodeRef = " + nodeRef);
		}

        List<AssociationRef> assocs = nodeService.getTargetAssocs(nodeRef, ContentModel.ASSOC_WORKING_COPY_LINK);
        
        if (!assocs.isEmpty()) {
        	checkOutCheckInService.cancelCheckout(assocs.get(0).getTargetRef());
        }
        
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
        	nodeRef = checkOutCheckInService.cancelCheckout(nodeRef);
        }
        
        nodeService.removeAspect(nodeRef, SignatureModel.ASPECT_SIGNATURE);
        
        return nodeRef;
	}

	@Override
	public String getDocumentView(NodeRef nodeRef, NodeRef user, NodeRef task) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("getDocumentView : nodeRef = " + nodeRef + ", user = " + user + ", task = " + task);
		}

        NodeRef workingNode = null;
        
        List<AssociationRef> assocs = nodeService.getTargetAssocs(nodeRef, ContentModel.ASSOC_WORKING_COPY_LINK);
        
        if (!assocs.isEmpty()) {
        	workingNode = assocs.get(0).getTargetRef();
        } else {
        	workingNode = nodeRef;
        }
        
        String requestParams = "?nodeRef=" + workingNode + "&mode=sign";
        
        if (task != null) {
        	requestParams += "&taskId={task|pjt:tlWorkflowTaskInstance}";
        }
        
        return "artworks-viewer" + requestParams;
	}
	
	private List<NodeRef> extractPeopleFromGroup(NodeRef authority) {
		Set<String> ret = new HashSet<>();
		if (nodeService.getType(authority).equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
			String authorityName = (String) nodeService.getProperty(authority, ContentModel.PROP_AUTHORITY_NAME);
			ret = authorityService.getContainedAuthorities(AuthorityType.USER, authorityName, false);
		} else {
			ret.add((String) nodeService.getProperty(authority, ContentModel.PROP_USERNAME));
		}

		return ret.stream().map(username -> personService.getPerson(username)).collect(Collectors.toList());
	}
				
	private void sign(NodeRef nodeRef, NodeRef recipient) {

		try {

			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
			
			if (logger.isDebugEnabled()) {
				logger.debug("sign : nodeRef = " + nodeRef + ", recipient = " + recipient);
			}
	
			try {
				signContent(nodeRef, recipient);
			} catch (IOException e) {
				throw new SignatureException("Failed to create signature", e);
			}
			
			nodeService.removeAssociation(nodeRef, recipient, SignatureModel.ASSOC_PREPARED_RECIPIENTS);
			
			try {
				updateSignatureInformation(nodeRef, recipient);
			} catch (InvalidTypeException | ContentIOException | InvalidNodeRefException | IOException e) {
				throw new SignatureException("Failed to update signature information", e);
			}
		} finally {
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
		}
	}

	private void updatePreparationInformation(NodeRef originalNode, SignatureContext context) {
		JSONObject recipientDataJson = new JSONObject();
	
		Object recipientData = nodeService.getProperty(originalNode, SignatureModel.PROP_RECIPIENTS_DATA);
				
		if (recipientData instanceof String && !((String) recipientData).isBlank()) {
			recipientDataJson = new JSONObject((String) recipientData);
		}
		
		JSONArray recipientsArray = new JSONArray();
		
		if (recipientDataJson.has(RECIPIENTS)) {
			recipientsArray = recipientDataJson.getJSONArray(RECIPIENTS);
		}
		
		for (NodeRef recipient : context.getRecipients()) {
			if (context.getNodeRecipients().contains(recipient)) {
				JSONObject recipientJson = new JSONObject();
				
				int indexToRemove = -1;
		    	
		    	for (int i = 0; i < recipientsArray.length(); i++) {
					if (recipientsArray.getJSONObject(i).get(NODE_REF).toString().equals(recipient.toString())) {
						recipientJson = recipientsArray.getJSONObject(i);
						indexToRemove = i;
						break;
					}
				}
		    	
		    	if (indexToRemove != -1) {
		    		recipientsArray.remove(indexToRemove);
		    	}
				
				recipientJson.put(NODE_REF, recipient);
		    	recipientJson.put("username", nodeService.getProperty(recipient, ContentModel.PROP_USERNAME));
				recipientJson.put("preparationDate", Calendar.getInstance().getTime());
				
				recipientsArray.put(recipientJson);
				
				recipientDataJson.put(RECIPIENTS, recipientsArray);
			}
		}
		
		nodeService.setProperty(originalNode, SignatureModel.PROP_RECIPIENTS_DATA, recipientDataJson.toString());
		nodeService.setProperty(originalNode, SignatureModel.PROP_STATUS, SignatureStatus.Prepared);
	}

	private void updateSignatureInformation(NodeRef nodeRef, NodeRef recipient) throws InvalidTypeException, ContentIOException, InvalidNodeRefException, IOException {
		
		byte[] signedContent = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT).getContentInputStream().readAllBytes();
	    
	    JSONObject recipientDataJson = new JSONObject();

	    Object recipientData = nodeService.getProperty(nodeRef, SignatureModel.PROP_RECIPIENTS_DATA);
	    		
	    if (recipientData instanceof String) {
	    	recipientDataJson = new JSONObject((String) recipientData);
	    }
	    
	    JSONArray recipientsArray = new JSONArray();
    	
    	if (recipientDataJson.has(RECIPIENTS)) {
    		recipientsArray = recipientDataJson.getJSONArray(RECIPIENTS);
    	}
    	
    	JSONObject recipientJson = new JSONObject();
    	
    	int indexToRemove = -1;
    	
    	for (int i = 0; i < recipientsArray.length(); i++) {
			if (recipientsArray.getJSONObject(i).get(NODE_REF).toString().equals(recipient.toString())) {
				recipientJson = recipientsArray.getJSONObject(i);
				indexToRemove = i;
				break;
			}
		}
    	
    	if (indexToRemove != -1) {
    		recipientsArray.remove(indexToRemove);
    	}
    	
    	String userDisplayName = nodeService.getProperty(recipient, ContentModel.PROP_FIRSTNAME) + " " + nodeService.getProperty(recipient, ContentModel.PROP_LASTNAME);
    	
    	PDSignature signature = extractSignature(signedContent, userDisplayName);
    	
    	try {
    		recipientJson.put("signatureDate", extractTimeStampDate(signedContent, signature));
    		
    		String contentThumbprint = DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA256").digest(signature.getContents(signedContent))).toLowerCase();

    		recipientJson.put("contentThumbprint", contentThumbprint);
    	} catch (JSONException | IOException | CMSException | TSPException | NoSuchAlgorithmException e) {
    		throw new SignatureException("Failed to extract time stamp date from signature");
		}
    	
    	recipientsArray.put(recipientJson);
    	
    	recipientDataJson.put(RECIPIENTS, recipientsArray);
	    
    	nodeService.setProperty(nodeRef, SignatureModel.PROP_RECIPIENTS_DATA, recipientDataJson.toString());
    	
    	if (allSigned(nodeRef, recipientsArray)) {
    		nodeService.setProperty(nodeRef, SignatureModel.PROP_STATUS, SignatureStatus.Signed);
    	} else if (!nodeService.getTargetAssocs(nodeRef, SignatureModel.ASSOC_PREPARED_RECIPIENTS).isEmpty()) {
    		nodeService.setProperty(nodeRef, SignatureModel.PROP_STATUS, SignatureStatus.Prepared);
    	} else {
    		nodeService.setProperty(nodeRef, SignatureModel.PROP_STATUS, SignatureStatus.Initialized);
    	}
	}
	
	private PDSignature extractSignature(byte[] signedContent, String userDisplayName) throws IOException {
		try (PDDocument document = PDDocument.load(signedContent)) {

			for (PDSignature signature : document.getSignatureDictionaries()) {

				if (signature.getName().equals(userDisplayName)) {
					return signature;
				}
			}
		}
		return null;
	}
	
	private Date extractTimeStampDate(byte[] signedFile, PDSignature signature) throws IOException, CMSException, TSPException {
		
		try (PDDocument document = PDDocument.load(signedFile)) {

			COSString contents = (COSString) signature.getCOSObject().getDictionaryObject(COSName.CONTENTS);

			byte[] buf;

			try (ByteArrayInputStream fis = new ByteArrayInputStream(signedFile)) {
				buf = signature.getSignedContent(fis);
			}

			CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(buf), contents.getBytes());
			Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
			SignerInformation signerInformation = signers.iterator().next();

			TimeStampToken timeStampToken = SignatureUtils.extractTimeStampTokenFromSignerInformation(signerInformation);

			if (timeStampToken != null && timeStampToken.getTimeStampInfo() != null) {
				return timeStampToken.getTimeStampInfo().getGenTime();
			}
			
			return null;
		}
	}

	private boolean allSigned(NodeRef nodeRef, JSONArray recipientsArray) {
    	
		List<NodeRef> nodeRecipients = new ArrayList<>();
		
		nodeService.getTargetAssocs(nodeRef, SignatureModel.ASSOC_RECIPIENTS).forEach(assoc -> nodeRecipients.addAll(extractPeopleFromGroup(assoc.getTargetRef())));

		for (NodeRef nodeRecipient : nodeRecipients) {
			
			boolean hasSigned = false;
			
			for (int i = 0; i < recipientsArray.length(); i++) {
				if (recipientsArray.getJSONObject(i).get(NODE_REF).toString().equals(nodeRecipient.toString()) && recipientsArray.getJSONObject(i).has("signatureDate")) {
					hasSigned = true;
					break;
				}
			}
			
			if (!hasSigned) {
				return false;
			}
		}
		
		return true;
		
	}
	
	private PDField removeField(PDDocument document, String fullFieldName) throws IOException {
	    PDDocumentCatalog documentCatalog = document.getDocumentCatalog();
	    PDAcroForm acroForm = documentCatalog.getAcroForm();

	    if (acroForm == null) {
	        return null;
	    }

	    PDField targetField = null;

	    for (PDField field : acroForm.getFieldTree()) {
	        if (fullFieldName.equals(field.getFullyQualifiedName())) {
	            targetField = field;
	            break;
	        }
	    }
	    if (targetField == null) {
	        return null;
	    }

	    PDNonTerminalField parentField = targetField.getParent();
	    if (parentField != null) {
	        List<PDField> childFields = parentField.getChildren();
	        for (PDField field : childFields)
	        {
	            if (field.getCOSObject().equals(targetField.getCOSObject())) {
	                childFields.remove(field);
	                parentField.setChildren(childFields);
	                break;
	            }
	        }
	    } else {
	        List<PDField> rootFields = acroForm.getFields();
	        for (PDField field : rootFields)
	        {
	            if (field.getCOSObject().equals(targetField.getCOSObject())) {
	                rootFields.remove(field);
	                break;
	            }
	        }
	    }
	    
	    removeWidgets(targetField);
	    
		COSDictionary dictionary = document.getDocumentCatalog().getCOSObject();
		dictionary.setNeedToBeUpdated(true);
		dictionary = (COSDictionary) dictionary.getDictionaryObject(COSName.ACRO_FORM);
		dictionary.setNeedToBeUpdated(true);
		COSArray array = (COSArray) dictionary.getDictionaryObject(COSName.FIELDS);
		array.setNeedToBeUpdated(true);

	    return targetField;
	}
	
	private void removeWidgets(PDField targetField) throws IOException {
	    if (targetField instanceof PDTerminalField) {
	        List<PDAnnotationWidget> widgets = ((PDTerminalField)targetField).getWidgets();
	        for (PDAnnotationWidget widget : widgets) {
	            PDPage page = widget.getPage();
	            if (page != null) {
	                List<PDAnnotation> annotations = page.getAnnotations();
	                for (PDAnnotation annotation : annotations) {
	                    if (annotation.getCOSObject().equals(widget.getCOSObject()))
	                    {
	                        annotations.remove(annotation);
	                        break;
	                    }
	                }
	                
	                COSDictionary item = page.getCOSObject();
	                
	                while (item.containsKey(COSName.PARENT)) {
	    				COSBase parent = item.getDictionaryObject(COSName.PARENT);
	    				if (parent instanceof COSDictionary) {
	    					item = (COSDictionary) parent;
	    					item.setNeedToBeUpdated(true);
	    				}
	    			}
	                
	                page.getCOSObject().setNeedToBeUpdated(true);
	                
	            }
	        }
	    } else if (targetField instanceof PDNonTerminalField) {
	        List<PDField> childFields = ((PDNonTerminalField)targetField).getChildren();
	        for (PDField field : childFields)
	            removeWidgets(field);
	    }
	}
	
	private void signContent(NodeRef nodeRef, NodeRef recipient) throws IOException {
	
			InputStream input = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT).getContentInputStream();

			PDDocument document = PDDocument.load(input);
			
			int accessPermissions = SignatureUtils.getMDPPermission(document);
			if (accessPermissions == 1) {
				throw new IllegalStateException("No changes to the document are permitted due to DocMDP transform parameters dictionary");
			}
			
			PDSignature signature = null;
			
			if (recipient != null) {
				String userName = formatUsername(recipient);

				PDSignatureField signatureField = findSignatureField(document, userName);
				
				if (signatureField == null) {
					logger.warn("The signature field for '" + userName + "' was not found.");
					userName = (String) nodeService.getProperty(recipient, ContentModel.PROP_USERNAME);
					signatureField = findSignatureField(document, userName);
					if (signatureField == null) {
						throw new IllegalStateException("The signature field for '" + userName + "' was not found.");
					}
				}
				
				// retrieve signature dictionary
				signature = signatureField.getSignature();
				
				if (signature == null) {
					signature = new PDSignature();
					signatureField.getCOSObject().setItem(COSName.V, signature);
				} else {
					throw new IllegalStateException("The signature field for '" + userName + "' is already signed.");
				}
			} else {
				signature = new PDSignature();
			}
			
			// we allow additional signatures with incremental saves
			if (document.getVersion() >= 1.5f && accessPermissions == 0) {
				SignatureUtils.setMDPPermission(document, signature, 3);
			}
			
			String userDisplayName = nodeService.getProperty(recipient, ContentModel.PROP_FIRSTNAME) + " " + nodeService.getProperty(recipient, ContentModel.PROP_LASTNAME);
			
			signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
			signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
			signature.setName(userDisplayName);
			signature.setReason(signatureReason);
			signature.setLocation(signatureLocation);
			signature.setContactInfo(signatureContactInfo);
			signature.setSignDate(Calendar.getInstance());
			
			SignatureOptions signatureOptions = new SignatureOptions();
			signatureOptions.setPreferredSignatureSize(SIGNATURE_SIZE);
			
			document.addSignature(signature, signatureOptions);
			try (OutputStream output = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true).getContentOutputStream()) {
				ExternalSigningSupport externalSigning = document.saveIncrementalForExternalSigning(output);
				byte[] cmsSignature = new byte[0];
				
				try {
					
					Certificate[] certificationChain = SignatureUtils.getCertificateChain(alias);
					
					if (certificationChain == null) {
						throw new SignatureException("Signature certificate could not be found for alias: " + alias);
					}
					
					X509Certificate certificate = (X509Certificate) certificationChain[0];
					PrivateKey privateKey = SignatureUtils.getSignaturePrivateKey(alias, password);
					
					CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
					ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256WithRSA").build(privateKey);
					gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build())
									.build(sha1Signer, certificate));
					gen.addCertificates(new JcaCertStore(Arrays.asList(certificationChain)));
					CMSProcessableInputStream msg = new CMSProcessableInputStream(externalSigning.getContent());
					CMSSignedData signedData = gen.generate(msg, false);
					
					if (tsaUrl != null && !tsaUrl.isEmpty()) {
						ValidationTimeStamp validation;
						validation = new ValidationTimeStamp(tsaUrl);
						signedData = validation.addSignedTimeStamp(signedData);
					}
					cmsSignature = signedData.getEncoded();
					
					if (logger.isDebugEnabled()) {
						logger.debug("signature length = " + cmsSignature.length);
					}
					
				} catch (GeneralSecurityException | CMSException | OperatorCreationException | IOException e) {
					throw new SignatureException(e.getMessage());
				}
				
				externalSigning.setSignature(cmsSignature);
			}
		}

	private PDSignatureField findSignatureField(PDDocument document, String userName) throws IOException {
		PDField removedField = removeField(document, userName);
		
		while (removedField != null) {
			removedField = removeField(document, userName);
		}
		
		PDSignatureField signatureField = findMatchingSignatureField(document, userName + "-signature");
		return signatureField;
	}

	private byte[] prepareForSignature(InputStream input, SignatureContext context) throws IOException {

		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {

			PDDocument document = PDDocument.load(input);

			int signaturePageNumber = 0;

			if ("last".equals(context.getSignaturePage())) {
				signaturePageNumber = document.getNumberOfPages() - 1;
			} else if (context.getSignaturePage().startsWith("last-")) {
				int minus = Integer.parseInt(context.getSignaturePage().split("-")[1]);
				signaturePageNumber = document.getNumberOfPages() - 1 - minus;
			} else {
				signaturePageNumber = Integer.parseInt(context.getSignaturePage());
			}
			
			// signature fields
			addFields(document, signaturePageNumber, context, true);
			
			// initials fields
			for (int pageNumber = 0; pageNumber < document.getNumberOfPages(); pageNumber++) {
				if (pageNumber != signaturePageNumber) {
					addFields(document, pageNumber, context, false);
				}
			}
			
			document.saveIncremental(output);

			return output.toByteArray();

		}
	}

	private void addFields(PDDocument document, int pageNumber, SignatureContext context, boolean isSignatureField) throws IOException {
		
		String[] dimensions = isSignatureField ? context.getSignatureDimensions().split(",") : context.getInitialsDimensions().split(",") ;
		
		String[] anchorInfo = isSignatureField ? context.getSignatureAnchorInfo().split(",") : context.getInitialsAnchorInfo().split(",") ;
		
		PDPage page = document.getPage(pageNumber);

		PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

		if (acroForm == null) {
			acroForm = new PDAcroForm(document);
			document.getDocumentCatalog().setAcroForm(acroForm);
		}
		
		for (int index = 0; index < context.getNodeRecipients().size(); index++) {
			
			NodeRef recipient = context.getNodeRecipients().get(index);
			
			if (context.getRecipients().contains(recipient)) {
				
				acroForm.setDefaultAppearance("/Helv 0 Tf 0 g");
				
				PDSignatureField signatureField = new PDSignatureField(acroForm);
				
				String fieldName = formatUsername(recipient);
				
				if (isSignatureField) {
					fieldName += "-signature";
				}
				
				signatureField.setPartialName(fieldName);
				
				PDAnnotationWidget fieldWidget = signatureField.getWidgets().get(0);
				
				float width = Float.parseFloat(dimensions[0]);
				float height = Float.parseFloat(dimensions[1]);
				float x = -1;
				float y = -1;
				
				boolean useAnchor = false;
				
				if (anchorInfo != null && anchorInfo.length == 3 && !anchorInfo[0].isBlank()) {
					
					int xPosition = Integer.parseInt(anchorInfo[1]);
					int yPosition = Integer.parseInt(anchorInfo[2]);
					
					float[] coordinates = PDFTextLocator.getCoordinates(document, anchorInfo[0], pageNumber);
					
					if (coordinates[0] != -1 && coordinates[1] != -1 && coordinates[2] != -1 && coordinates[3] != -1) {
						
						useAnchor = true;
						
						switch (xPosition) {
						case LEFT_POSITION:
							x = coordinates[0] - width;
							break;
						case MIDDLE_POSITION:
							x = (coordinates[1] + coordinates[0]) / 2 - width / 2;
							break;
						case RIGHT_POSITION:
							x = coordinates[1];
							break;
						default:
						}
						
						switch (yPosition) {
						case BOTTOM_POSITION:
							y = coordinates[2] - height;
							break;
						case MIDDLE_POSITION:
							y = (coordinates[3] + coordinates[2]) / 2 - height / 2;
							break;
						case TOP_POSITION:
							y = coordinates[3];
							break;
						default:
						}
					}
				}
				
				if (!useAnchor) {
					x = page.getMediaBox().getWidth() * Float.parseFloat(dimensions[4]) / 100;
					y = page.getMediaBox().getHeight() * Float.parseFloat(dimensions[5]) / 100;
				}
				
				int direction = Integer.parseInt(dimensions[2]);
				int gap = Integer.parseInt(dimensions[3]);
				
				switch (direction) {
				case RIGHT_DIRECTION:
					x += index * gap;
					break;
				case LEFT_DIRECTION:
					x -= index * gap;
					break;
				case UP_DIRECTION:
					y += index * gap;
					break;
				case DOWN_DIRECTION:
					y -= index * gap;
					break;
				default:
				}
				
				if (x + width > page.getMediaBox().getWidth()) {
					x = (x + width) % page.getMediaBox().getWidth();
				}
				if (y + height > page.getMediaBox().getHeight()) {
					y = (y + height) % page.getMediaBox().getHeight();
				}
				
				PDRectangle fieldRect = new PDRectangle(x, y, width, height);
				
				fieldWidget.setRectangle(fieldRect);
				
				fieldWidget.setPage(page);
				
				fieldWidget.setPrinted(true);
				
				acroForm.getFields().add(signatureField);
				
				page.getAnnotations().add(fieldWidget);
				
				COSDictionary dictionary = document.getDocumentCatalog().getCOSObject();
				dictionary.setNeedToBeUpdated(true);
				dictionary = (COSDictionary) dictionary.getDictionaryObject(COSName.ACRO_FORM);
				dictionary.setNeedToBeUpdated(true);
				COSArray array = (COSArray) dictionary.getDictionaryObject(COSName.FIELDS);
				array.setNeedToBeUpdated(true);

				COSDictionary item = page.getCOSObject();
                item.setNeedToBeUpdated(true);
				
                COSBase annots = item.getDictionaryObject(COSName.ANNOTS).getCOSObject();
                
                if (annots instanceof COSArray) {
                	
                	((COSArray) annots).setNeedToBeUpdated(true);
                	
                	for (int i = 0; i < ((COSArray) annots).size(); i++) {
                		COSBase object = ((COSArray) annots).get(i).getCOSObject().getCOSObject();
                		
                		if (object instanceof COSDictionary) {
                			((COSDictionary) object).setNeedToBeUpdated(true);
                		}
                	}
                }
                
                while (item.containsKey(COSName.PARENT)) {
    				COSBase parent = item.getDictionaryObject(COSName.PARENT);
    				if (parent instanceof COSDictionary) {
    					item = (COSDictionary) parent;
    					item.setNeedToBeUpdated(true);
    				}
    			}
			}
		}
	}

	private String formatUsername(NodeRef recipient) {
		return ((String) nodeService.getProperty(recipient, ContentModel.PROP_USERNAME)).replace(".", "_");
	}

	private SignatureContext buildSignatureContext(List<NodeRef> nodeRecipients, List<NodeRef> recipients, String... params) {

		SignatureContext signatureContext = new SignatureContext();
		
		signatureContext.setNodeRecipients(nodeRecipients);
		
		signatureContext.setRecipients(recipients);

		if (params != null && params.length > 0) {
			signatureContext.setSignaturePage(params[0]);
		}
		
		if (params != null && params.length > 1) {
			signatureContext.setSignatureDimensions(params[1]);
		}
		
		if (params != null && params.length > 2) {
			signatureContext.setSignatureAnchorInfo(params[2]);
		}
		
		if (params != null && params.length > 3) {
			signatureContext.setInitialsDimensions(params[3]);
		}
		
		if (params != null && params.length > 4) {
			signatureContext.setInitialsAnchorInfo(params[4]);
		}
		
		return signatureContext;
	}
 	
	

	private PDSignatureField findMatchingSignatureField(PDDocument doc, String fieldName) {
		PDSignatureField signatureField = null;
		PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();

		if (acroForm != null) {
			signatureField = (PDSignatureField) acroForm.getField(fieldName);
		}

		return signatureField;
	}
	
}
