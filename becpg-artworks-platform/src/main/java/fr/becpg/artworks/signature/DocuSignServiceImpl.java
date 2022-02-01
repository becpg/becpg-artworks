/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.artworks.signature;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import fr.becpg.artworks.signature.model.SignatureModel;

/**
 *
 * @author valentin.leblanc
 *
 */
@Service
public final class DocuSignServiceImpl implements SignatureService {

	private static final String DOCUSIGN_BASE_URL = "https://demo.docusign.net/restapi/v2.1/accounts/";

	private static final String ENVELOPES = "/envelopes/";

	private static final String ENVELOPE_ID = "envelopeId";

	private static final String BEARER = "Bearer ";

	private static final String FAIL_TO_PARSE_JSON = "Fail to parse JSON";

	private static final String AUTHORIZATION = "Authorization";

	private static final String EMAIL_SUBJECT = "beCPG Document signing";

	private static Log logger = LogFactory.getLog(DocuSignServiceImpl.class);

	private NodeService nodeService;

	private ContentService contentService;

	private PersonService personService;

	private CheckOutCheckInService checkOutCheckInService;

	private String signatureAuthorization;

	private MimetypeService mimetypeService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
		this.checkOutCheckInService = checkOutCheckInService;
	}

	public void setSignatureAuthorization(String signatureAuthorization) {
		this.signatureAuthorization = signatureAuthorization;
	}

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	@Override
	public String checkoutDocument(NodeRef nodeRef) {
		return prepareForSignature(nodeRef, true);
	}

	@Override
	public String prepareForSignature(NodeRef nodeRef, boolean notifyByMail) {

		logger.debug("sendForSignature");

		String accountId = signatureAuthorization.split(";")[0];

		String accessToken = signatureAuthorization.split(";")[1];

		String envelopeId = null;

		ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

		if (contentReader.exists()) {
			HttpHeaders headers = new HttpHeaders();
			headers.add(AUTHORIZATION, BEARER + accessToken);
			RestTemplate restTemplate = new RestTemplate();

			File contentFile;
			boolean deleteContentFileOnCompletion = false;
			if (FileContentReader.class.isAssignableFrom(contentReader.getClass())) {
				// Grab the content straight from the content store if we can...
				contentFile = ((FileContentReader) contentReader).getFile();
			} else {
				// ...otherwise copy it to a temp file and use the copy...
				File tempDir = TempFileProvider.getLongLifeTempDir("signature");
				contentFile = TempFileProvider.createTempFile("signature", "", tempDir);
				contentReader.getContent(contentFile);
				deleteContentFileOnCompletion = true;
			}

			try {
				headers.setContentType(MediaType.APPLICATION_JSON);

				byte[] bytes = FileUtils.readFileToByteArray(contentFile);

				String encodedFile = Base64.getEncoder().encodeToString(bytes);

				String fileExtension = mimetypeService
						.getExtension(mimetypeService.guessMimetype((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)));

				List<NodeRef> recipientsNodeRefs = new ArrayList<>();

				nodeService.getTargetAssocs(nodeRef, SignatureModel.ASSOC_SIGNATURE_RECIPIENTS)
						.forEach(assoc -> recipientsNodeRefs.add(assoc.getTargetRef()));

				if (recipientsNodeRefs.isEmpty()) {
					recipientsNodeRefs.add(personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser()));
				}

				JSONObject body = new JSONObject();

				JSONArray documents = new JSONArray();

				JSONObject document = new JSONObject();

				document.put("documentBase64", encodedFile);
				document.put("documentId", "1");
				document.put("fileExtension", fileExtension);
				document.put("name", nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));

				documents.put(document);

				body.put("documents", documents);

				body.put("emailSubject", EMAIL_SUBJECT);

				JSONObject recipients = new JSONObject();
				JSONArray signers = new JSONArray();

				int clientUserId = 1;

				JSONObject clientUserIds = new JSONObject();

				for (NodeRef recipient : recipientsNodeRefs) {
					String userDisplayName = nodeService.getProperty(recipient, ContentModel.PROP_FIRSTNAME) + " "
							+ nodeService.getProperty(recipient, ContentModel.PROP_LASTNAME);
					String email = (String) nodeService.getProperty(recipient, ContentModel.PROP_EMAIL);

					JSONObject signer = new JSONObject();

					signer.put("email", email);
					signer.put("name", userDisplayName);
					signer.put("recipientId", clientUserId);
					if (!notifyByMail) {
						signer.put("clientUserId", clientUserId);
					}

					signers.put(signer);
					clientUserIds.put(recipient.toString(), clientUserId);

					clientUserId++;
				}

				nodeService.setProperty(nodeRef, SignatureModel.PROP_SIGNATURE_CLIENT_USER_IDS, clientUserIds.toString());

				recipients.put("signers", signers);

				body.put("recipients", recipients);
				body.put("status", "sent");

				HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

				String ret = restTemplate.postForObject(DOCUSIGN_BASE_URL + accountId + ENVELOPES, entity, String.class);

				JSONObject jsonObject = new JSONObject(ret);
				if (jsonObject.has(ENVELOPE_ID)) {
					envelopeId = jsonObject.getString(ENVELOPE_ID);
				}
			} catch (JSONException | IOException e) {
				logger.error(FAIL_TO_PARSE_JSON, e);
			} finally {
				if (deleteContentFileOnCompletion) {
					try {
						Files.delete(contentFile.toPath());
					} catch (IOException e) {
						logger.error("Failed to delete file", e);
					}
				}
			}

			// Store documentIdentifier on document
			Map<QName, Serializable> annotationProperties = new HashMap<>();
			annotationProperties.put(SignatureModel.PROP_SIGNATURE_DOCUMENT_IDENTIFIER, envelopeId);
			nodeService.addAspect(nodeRef, SignatureModel.ASPECT_SIGNATURE, annotationProperties);

		}

		return envelopeId;
	}

	@Override
	public String getDocumentView(NodeRef nodeRef, String userId, String returnUrl) {

		String accountId = signatureAuthorization.split(";")[0];

		String accessToken = signatureAuthorization.split(";")[1];

		String envelopeId = (String) nodeService.getProperty(nodeRef, SignatureModel.PROP_SIGNATURE_DOCUMENT_IDENTIFIER);
		String userDisplayName = userId;
		String email = "no-reply@becpg.fr";
		NodeRef personNodeRef = personService.getPerson(userId);
		if (personNodeRef != null) {
			userDisplayName = nodeService.getProperty(personNodeRef, ContentModel.PROP_FIRSTNAME) + " "
					+ nodeService.getProperty(personNodeRef, ContentModel.PROP_LASTNAME);
			email = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_EMAIL);
		}

		String url = DOCUSIGN_BASE_URL + accountId + ENVELOPES + envelopeId + "/views/recipient";

		HttpHeaders headers = new HttpHeaders();
		headers.add(AUTHORIZATION, BEARER + accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		JSONObject body = new JSONObject();
		body.put("returnUrl", returnUrl);
		body.put("userName", userDisplayName);
		body.put("authenticationMethod", "email");
		body.put("email", email);
		body.put("clientUserId",
				new JSONObject((String) nodeService.getProperty(nodeRef, SignatureModel.PROP_SIGNATURE_CLIENT_USER_IDS)).get(userId));

		RestTemplate restTemplate = new RestTemplate();

		HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
		String ret = restTemplate.postForObject(url, entity, String.class);

		JSONObject jsonObject = new JSONObject(ret);
		if (jsonObject.has("url")) {
			return jsonObject.getString("url");
		}

		return "null";
	}

	@Override
	public void checkinDocument(NodeRef nodeRef) {

		String accountId = signatureAuthorization.split(";")[0];

		String accessToken = signatureAuthorization.split(";")[1];

		String envelopeId = (String) nodeService.getProperty(nodeRef, SignatureModel.PROP_SIGNATURE_DOCUMENT_IDENTIFIER);

		String url = DOCUSIGN_BASE_URL + accountId + ENVELOPES + envelopeId;

		HttpHeaders headers = new HttpHeaders();
		headers.add(AUTHORIZATION, BEARER + accessToken);

		RestTemplate restTemplate = new RestTemplate();

		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<String> statusResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

		String status = new JSONObject(statusResponse.getBody()).getString("status");

		if (!"completed".equalsIgnoreCase(status)) {
			throw new WebScriptException("Document signature is not completed");
		}

		url += "/documents/1/";

		ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

		byte[] signedDocumentBytes = response.getBody();

		if (signedDocumentBytes != null) {

			NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(nodeRef);
			Map<String, Serializable> versionProperties = new HashMap<>();
			versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MINOR);
			checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
			writeContent(nodeRef, signedDocumentBytes);
		} else {
			throw new WebScriptException("Signed document could not be retrieved from DocuSign");
		}

		url = DOCUSIGN_BASE_URL + accountId + ENVELOPES + envelopeId + "/documents/certificate";

		ResponseEntity<byte[]> certificateResponse = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

		byte[] certficateBytes = certificateResponse.getBody();

		if (certficateBytes != null) {

			String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME) + " - Signature Certificate";

			NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();

			NodeRef certificateNode = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, name);

			if (certificateNode == null) {
				Map<QName, Serializable> props = new HashMap<>();
				props.put(ContentModel.PROP_NAME, name);
				certificateNode = nodeService
						.createNode(parent, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT, props).getChildRef();
				nodeService.addAspect(certificateNode, ContentModel.ASPECT_VERSIONABLE, null);
			} else {
				NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(certificateNode);
				Map<String, Serializable> versionProperties = new HashMap<>();
				versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MINOR);
				checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
			}

			writeContent(certificateNode, certficateBytes);

		} else {
			throw new WebScriptException("Signature Certificate could not be retrieved from DocuSign");
		}

		cancelDocument(nodeRef);
		
	}

	private void writeContent(NodeRef nodeRef, byte[] ret) {
		ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);

		try (InputStream targetStream = new ByteArrayInputStream(ret)) {

			String mimetype = mimetypeService.guessMimetype(null, targetStream);
			ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
			Charset charset = charsetFinder.getCharset(targetStream, mimetype);
			String encoding = charset.name();

			writer.setEncoding(encoding);
			writer.setMimetype(mimetype);
			writer.putContent(targetStream);

		} catch (ContentIOException | IOException e) {
			logger.error("Failed to write content to node", e);
		}
	}

	@Override
	public void cancelDocument(NodeRef nodeRef) {

		String accountId = signatureAuthorization.split(";")[0];

		String accessToken = signatureAuthorization.split(";")[1];

		String envelopeId = (String) nodeService.getProperty(nodeRef, SignatureModel.PROP_SIGNATURE_DOCUMENT_IDENTIFIER);

		String url = DOCUSIGN_BASE_URL + accountId + ENVELOPES + envelopeId + "/documents/";

		HttpHeaders headers = new HttpHeaders();
		headers.add(AUTHORIZATION, BEARER + accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		RestTemplate restTemplate = new RestTemplate();

		String body = "{" + "  \"textCustomFields\": [" + "  ]," + "  \"listCustomFields\": [" + "  ]" + "}";

		HttpEntity<String> entity = new HttpEntity<>(body, headers);

		Map<String, String> uriParam = new HashMap<>();
		uriParam.put("accountId", accountId);
		uriParam.put(ENVELOPE_ID, envelopeId);

		try {
			restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class, uriParam).getBody();
		} catch (RestClientException e) {
			logger.info("Could not delete envelope " + envelopeId);
		}

		nodeService.removeAspect(nodeRef, SignatureModel.ASPECT_SIGNATURE);
	}

}
