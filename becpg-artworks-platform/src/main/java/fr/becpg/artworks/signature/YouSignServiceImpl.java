///*******************************************************************************
// * Copyright (C) 2010-2021 beCPG.
// *
// * This file is part of beCPG
// *
// * beCPG is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * beCPG is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
// ******************************************************************************/
//package fr.becpg.artworks.signature;
//
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.Serializable;
//import java.nio.charset.Charset;
//import java.nio.file.Files;
//import java.util.ArrayList;
//import java.util.Base64;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.alfresco.model.ContentModel;
//import org.alfresco.repo.admin.SysAdminParams;
//import org.alfresco.repo.content.encoding.ContentCharsetFinder;
//import org.alfresco.repo.content.filestore.FileContentReader;
//import org.alfresco.repo.security.authentication.AuthenticationUtil;
//import org.alfresco.repo.version.VersionBaseModel;
//import org.alfresco.service.cmr.coci.CheckOutCheckInService;
//import org.alfresco.service.cmr.repository.ContentIOException;
//import org.alfresco.service.cmr.repository.ContentReader;
//import org.alfresco.service.cmr.repository.ContentService;
//import org.alfresco.service.cmr.repository.ContentWriter;
//import org.alfresco.service.cmr.repository.MimetypeService;
//import org.alfresco.service.cmr.repository.NodeRef;
//import org.alfresco.service.cmr.repository.NodeService;
//import org.alfresco.service.cmr.security.PersonService;
//import org.alfresco.service.cmr.version.VersionType;
//import org.alfresco.service.namespace.QName;
//import org.alfresco.util.TempFileProvider;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.StringEscapeUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.springframework.extensions.webscripts.WebScriptException;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import fr.becpg.artworks.signature.model.SignatureModel;
//
///**
// *
// * @author valentin.leblanc
// *
// */
//@SuppressWarnings("deprecation")
//@Service
//public final class YouSignServiceImpl implements SignatureService {
//
//	private static final String INITIALS = "initials";
//
//	private static final String OPERATION_CUSTOM_MODES = "operationCustomModes";
//
//	private static final String OPERATION_LEVEL = "operationLevel";
//
//	private static final String CONFIG = "config";
//
//	private static final String PROCEDURE_STARTED = "procedure.started";
//
//	private static final String MEMBER_STARTED = "member.started";
//
//	private static final String TO = "to";
//
//	private static final String MESSAGE = "message";
//
//	private static final String SUBJECT = "subject";
//
//	private static final String ID = "id";
//
//	private static final String CONTENT = "content";
//
//	private static final String DESCRIPTION = "description";
//
//	private static final String NAME = "name";
//
//	private static final String FILE_OBJECTS = "fileObjects";
//
//	private static final String MENTION = "mention";
//
//	private static final String POSITION = "position";
//
//	private static final String FILE = "file";
//
//	private static final String MEMBERS = "members";
//
//	private static final String PHONE = "phone";
//
//	private static final String LASTNAME = "lastname";
//
//	private static final String FIRSTNAME = "firstname";
//
//	private static final String EMAIL = "email";
//
//	private static final String YOUSIGN_BASE_URL = "https://staging-api.yousign.com";
//
//	private static Log logger = LogFactory.getLog(YouSignServiceImpl.class);
//
//	private NodeService nodeService;
//
//	private ContentService contentService;
//
//	private PersonService personService;
//
//	private String signatureAuthorization;
//
//	private CheckOutCheckInService checkOutCheckInService;
//
//	private MimetypeService mimetypeService;
//	
//	private SysAdminParams sysAdminParams;
//
//	public void setNodeService(NodeService nodeService) {
//		this.nodeService = nodeService;
//	}
//
//	public void setContentService(ContentService contentService) {
//		this.contentService = contentService;
//	}
//
//	public void setPersonService(PersonService personService) {
//		this.personService = personService;
//	}
//
//	public void setSignatureAuthorization(String signatureAuthorization) {
//		this.signatureAuthorization = signatureAuthorization;
//	}
//	
//	public void setMimetypeService(MimetypeService mimetypeService) {
//		this.mimetypeService = mimetypeService;
//	}
//	
//	public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
//		this.checkOutCheckInService = checkOutCheckInService;
//	}
//	
//	public void setSysAdminParams(SysAdminParams sysAdminParams) {
//		this.sysAdminParams = sysAdminParams;
//	}
//
//	@Override
//	public String sendForSignature(NodeRef nodeRef, boolean notifyByMail) {
//
//		logger.debug("sendForSignature");
//
//		String apiKey = signatureAuthorization.split(";")[1];
//
//		String fileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
//
//		ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
//		
//		String fileId = null;
//
//		if (contentReader.exists()) {
//			HttpHeaders headers = new HttpHeaders();
//			headers.add(AUTHORIZATION, BEARER + apiKey);
//			RestTemplate restTemplate = new RestTemplate();
//
//			File contentFile;
//			boolean deleteContentFileOnCompletion = false;
//			if (FileContentReader.class.isAssignableFrom(contentReader.getClass())) {
//				// Grab the content straight from the content store if we can...
//				contentFile = ((FileContentReader) contentReader).getFile();
//			} else {
//				// ...otherwise copy it to a temp file and use the copy...
//				File tempDir = TempFileProvider.getLongLifeTempDir("signature");
//				contentFile = TempFileProvider.createTempFile("signature", "", tempDir);
//				contentReader.getContent(contentFile);
//				deleteContentFileOnCompletion = true;
//			}
//
//			try {
//				headers.setContentType(MediaType.APPLICATION_JSON);
//
//				byte[] bytes = FileUtils.readFileToByteArray(contentFile);
//
//				String encodedFile = Base64.getEncoder().encodeToString(bytes);
//
//				JSONObject sendFileBody = new JSONObject();
//
//				sendFileBody.put(NAME, fileName);
//				sendFileBody.put(CONTENT, encodedFile);
//
//				HttpEntity<String> sendFileEntity = new HttpEntity<>(sendFileBody.toString(), headers);
//
//				String sendFileResponse = restTemplate.postForObject(YOUSIGN_BASE_URL + "/files", sendFileEntity, String.class);
//
//				JSONObject sendFileJsonResponse = new JSONObject(sendFileResponse);
//
//
//				if (sendFileJsonResponse.has(ID)) {
//					fileId = sendFileJsonResponse.getString(ID);
//				}
//
//				JSONObject createProcedureBody = new JSONObject();
//
//				createProcedureBody.put(NAME, "beCPG YouSign procedure");
//				createProcedureBody.put(DESCRIPTION, "beCPG YouSign description");
////				createProcedureBody.put(INITIALS, true);
//
//				JSONArray members = new JSONArray();
//
//				List<NodeRef> recipientsNodeRefs = new ArrayList<>();
//
//				nodeService.getTargetAssocs(nodeRef, SignatureModel.ASSOC_SIGNATURE_RECIPIENTS)
//						.forEach(assoc -> recipientsNodeRefs.add(assoc.getTargetRef()));
//
//				if (recipientsNodeRefs.isEmpty()) {
//					recipientsNodeRefs.add(personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser()));
//				}
//
//				for (NodeRef recipient : recipientsNodeRefs) {
//
//					JSONObject member = new JSONObject();
//
//					member.put(FIRSTNAME, nodeService.getProperty(recipient, ContentModel.PROP_FIRSTNAME));
//					member.put(LASTNAME, "test");
//					member.put(EMAIL, nodeService.getProperty(recipient, ContentModel.PROP_EMAIL));
//					member.put(PHONE, nodeService.getProperty(recipient, ContentModel.PROP_MOBILE));
//					member.put(OPERATION_LEVEL, "custom");
//					member.put(OPERATION_CUSTOM_MODES, new JSONArray("[\"email\"]"));
//					
//					JSONObject operationModeEmailConfig = new JSONObject();
//					
//					operationModeEmailConfig.put(SUBJECT, "Your security code");
//					operationModeEmailConfig.put(CONTENT, "To finalize your electronic signature, use the following security code: {{code}}");
//
//					JSONArray fileObjects = new JSONArray();
//
//					JSONObject fileObject = new JSONObject();
//
//					fileObject.put(FILE, fileId);
//					fileObject.put(POSITION, "230,499,464,589");
//					fileObject.put(MENTION, "Read and approved");
//
//					fileObjects.put(fileObject);
//					
//					member.put(FILE_OBJECTS, fileObjects);
//					
//					members.put(member);
//				}
//
//				createProcedureBody.put(MEMBERS, members);
//				
//				if (notifyByMail) {
//					JSONObject config = new JSONObject();
//					
//					JSONObject email = new JSONObject();
//					
//					JSONArray memberStartedArray = new JSONArray();
//					
//					JSONObject memberStarted = new JSONObject();
//					
//					memberStarted.put(SUBJECT, RECIPIENT_EMAIL_SUBJECT);
//					memberStarted.put(MESSAGE, "Hello <tag data-tag-type=\"string\" data-tag-name=\"recipient.firstname\"></tag> <tag data-tag-type=\"string\" data-tag-name=\"recipient.lastname\"></tag>, <br><br> You have ben invited to sign a document, please click on the following button to read it: <tag data-tag-type=\"button\" data-tag-name=\"url\" data-tag-title=\"Access to documents\">Access to documents</tag>");
//					memberStarted.put(TO, new JSONArray("[\"@member\"]"));
//					
//					memberStartedArray.put(memberStarted);
//					
//					email.put(MEMBER_STARTED, memberStartedArray);
//					
//					JSONArray procedureStartedArray = new JSONArray();
//					
//					JSONObject procedureStarted = new JSONObject();
//					
//					procedureStarted.put(SUBJECT, SENDER_EMAIL_SUBJECT);
//					procedureStarted.put(MESSAGE, "Hello, you created a signing procedure.");
//					
//					JSONArray procedureToArray = new JSONArray();
//					procedureToArray.put("@creator");
//					procedureToArray.put("@members");
//					
//					procedureStarted.put(TO, procedureToArray);
//					
//					procedureStartedArray.put(procedureStarted);
//					
//					email.put(PROCEDURE_STARTED, procedureStartedArray);
//					
//					config.put(EMAIL, email);
//					
//					createProcedureBody.put(CONFIG, config);
//				}
//				
//				HttpEntity<String> createProcedureEntity = new HttpEntity<>(createProcedureBody.toString(), headers);
//
//				String createProcedureResponse = restTemplate.postForObject(YOUSIGN_BASE_URL + "/procedures", createProcedureEntity, String.class);
//
//				JSONObject createProcedureJsonResponse = new JSONObject(createProcedureResponse);
//
//				if (createProcedureJsonResponse.has(MEMBERS)) {
//					
//					JSONObject membersData = new JSONObject();
//					
//					JSONArray responseMembers = createProcedureJsonResponse.getJSONArray(MEMBERS);
//					
//					for (NodeRef recipient : recipientsNodeRefs) {
//						for (int i = 0; i < responseMembers.length(); i++) {
//							JSONObject responseMember = responseMembers.getJSONObject(i);
//							
//							if (responseMember.getString(FIRSTNAME).equals(nodeService.getProperty(recipient, ContentModel.PROP_FIRSTNAME))
//									&& responseMember.getString(LASTNAME).equals("test")
//									&& responseMember.getString(EMAIL).equals(nodeService.getProperty(recipient, ContentModel.PROP_EMAIL))
//									&& responseMember.getString(PHONE).equals(nodeService.getProperty(recipient, ContentModel.PROP_MOBILE))) {
//								membersData.put(recipient.toString(), responseMember.get(ID));
//							}
//						}
//					}
//					
//					nodeService.setProperty(nodeRef, SignatureModel.PROP_SIGNATURE_DATA, membersData.toString());
//					
//				}
//			} catch (JSONException | IOException e) {
//				logger.error(SIGNATURE_ERROR, e);
//				throw new WebScriptException(SIGNATURE_ERROR);
//			} finally {
//				if (deleteContentFileOnCompletion) {
//					try {
//						Files.delete(contentFile.toPath());
//					} catch (IOException e) {
//						logger.error("Failed to delete file", e);
//					}
//				}
//			}
//
//			Map<QName, Serializable> signatureProperties = new HashMap<>();
//			signatureProperties.put(SignatureModel.PROP_SIGNATURE_DOCUMENT_IDENTIFIER, fileId);
//			nodeService.addAspect(nodeRef, SignatureModel.ASPECT_SIGNATURE, signatureProperties);
//
//		}
//
//		return fileId;
//	}
//
//	@Override
//	public String getSignatureView(NodeRef nodeRef, NodeRef recipient, NodeRef task) {
//		
//		JSONObject jsonData = new JSONObject((String) nodeService.getProperty(nodeRef, SignatureModel.PROP_SIGNATURE_DATA));
//		
//		String memberId = (String) jsonData.get(recipient.toString());
//
//		return YOUSIGN_BASE_URL + "/procedure/sign?members=" + memberId;
//	}
//
//	@Override
//	public void retrieveSignedDocument(NodeRef nodeRef) {
//		
//		String apiKey = signatureAuthorization.split(";")[1];
//
//		String fileId = (String) nodeService.getProperty(nodeRef, SignatureModel.PROP_SIGNATURE_DOCUMENT_IDENTIFIER);
//		
//		String url = YOUSIGN_BASE_URL + fileId + "/download";
//		
//		HttpHeaders headers = new HttpHeaders();
//		headers.add(AUTHORIZATION, BEARER + apiKey);
//		
//		RestTemplate restTemplate = new RestTemplate();
//
//		HttpEntity<String> entity = new HttpEntity<>(headers);
//		
//		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//
//		String signedDocument = response.getBody();
//		
//		if (signedDocument != null) {
//			signedDocument = signedDocument.substring(1, signedDocument.length() - 1);
//		}
//		
//		signedDocument = StringEscapeUtils.unescapeJson(signedDocument);
//		
//	    byte[] signedDocumentBytes = Base64.getDecoder().decode(signedDocument);
//		
//		if (signedDocumentBytes != null) {
//			
//			NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(nodeRef);
//			Map<String, Serializable> versionProperties = new HashMap<>();
//			versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MINOR);
//			checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
//			writeContent(nodeRef, signedDocumentBytes);
//		} else {
//			throw new WebScriptException("Signed document could not be retrieved from DocuSign");
//		}
//		
//	}
//	
//	private void writeContent(NodeRef nodeRef, byte[] ret) {
//		ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
//		
//		try (InputStream targetStream = new ByteArrayInputStream(ret)) {
//		
//			String mimetype = mimetypeService.guessMimetype(FILE, targetStream);
//			ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
//			Charset charset = charsetFinder.getCharset(targetStream, mimetype);
//			String encoding = charset.name();
//			
//			writer.setEncoding(encoding);
//			writer.setMimetype(mimetype);
//			writer.putContent(targetStream);
//		
//		} catch (ContentIOException | IOException e) {
//			logger.error("Failed to write content to node", e);
//		}
//	}
//
//	@Override
//	public void deleteDocument(NodeRef nodeRef) {
//		
//	}
//
//	@Override
//	public boolean isActive() {
//		return signatureAuthorization != null && signatureAuthorization.toLowerCase().contains("yousign");
//	}
//}
