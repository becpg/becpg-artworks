/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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
package fr.becpg.artworks.annotation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import fr.becpg.artworks.annotation.model.AnnotationModel;

/**
 * Annotation service
 *
 * @author Philippe
 *
 */
public final class KamiServiceImpl implements AnnotationService {

	private static final String FAIL_TO_PARSE_JSON = "Fail to parse JSON";

	private static final String AUTHORIZATION = "Authorization";

	private static Log logger = LogFactory.getLog(KamiServiceImpl.class);

	private NodeService nodeService;

	private ContentService contentService;

	private PersonService personService;

	private CheckOutCheckInService checkOutCheckInService;

	private String annotationAuthorization;

	private Integer sessionDurationInDays = 1;

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

	public void setAnnotationAuthorization(String annotationAuthorization) {
		this.annotationAuthorization = annotationAuthorization;
	}

	public void setSessionDurationInDays(Integer sessionDurationInDays) {
		this.sessionDurationInDays = sessionDurationInDays;
	}

	/**
	 * Upload a document to Kami
	 * @param scriptNode
	 * @return documentIdentifier
	 */
	@Override
	public String checkoutDocument(NodeRef nodeRef) {
		logger.debug("uploadDocument");
		String documentIdentifier = null;
		ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

		if (contentReader.exists()) {
			HttpHeaders headers = new HttpHeaders();
			headers.add(AUTHORIZATION, annotationAuthorization);
			RestTemplate restTemplate = new RestTemplate();

			File contentFile;
			boolean deleteContentFileOnCompletion = false;
			if (FileContentReader.class.isAssignableFrom(contentReader.getClass())) {
				// Grab the content straight from the content store if we can...
				contentFile = ((FileContentReader) contentReader).getFile();
			} else {
				// ...otherwise copy it to a temp file and use the copy...
				File tempDir = TempFileProvider.getLongLifeTempDir("annotation");
				contentFile = TempFileProvider.createTempFile("annotation", "", tempDir);
				contentReader.getContent(contentFile);
				deleteContentFileOnCompletion = true;
			}

			try {
				String url = "https://api.notablepdf.com/upload/embed/documents";
				headers.setContentType(MediaType.MULTIPART_FORM_DATA);
				MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
				map.add("name", nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
				map.add("document", new FileSystemResource(contentFile));

				HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);
				String ret = restTemplate.postForObject(url, entity, String.class);

				JSONObject jsonObject = new JSONObject(ret);
				if (jsonObject.has("document_identifier")) {
					documentIdentifier = jsonObject.getString("document_identifier");
				}
			} catch (JSONException e) {
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
			annotationProperties.put(AnnotationModel.PROP_ANNOTATION_DOCUMENT_IDENTIFIER, documentIdentifier);
			nodeService.addAspect(nodeRef, AnnotationModel.ASPECT_ANNOTATION, annotationProperties);
		}
		logger.debug("documentIdentifier" + documentIdentifier);
		return documentIdentifier;
	}

	/**
	 * Creates a time-limited session and returns a URL
	 * @param documentIdentifier
	 * @param userId
	 * @param sessionDurationInDays
	 * @return
	 */
	@Override
	public String getDocumentView(NodeRef nodeRef, NodeRef personNodeRef, NodeRef task) {

		if (personNodeRef == null) {
			personNodeRef = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
		}

		String userDisplayName = nodeService.getProperty(personNodeRef, ContentModel.PROP_FIRSTNAME) + " "
				+ nodeService.getProperty(personNodeRef, ContentModel.PROP_LASTNAME);
		String userId = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, sessionDurationInDays);

		String documentIdentifier = (String) nodeService.getProperty(nodeRef, AnnotationModel.PROP_ANNOTATION_DOCUMENT_IDENTIFIER);
		String url = "https://api.notablepdf.com/embed/sessions";
		String requestJson = "{\"document_identifier\": \"" + documentIdentifier + "\", \"user\": {\"name\": \"" + userDisplayName
				+ "\", \"user_id\": \"" + userId + "\"}, \"expires_at\": \"" + calendar.getTime()
				+ "\", \"viewer_options\": {\"theme\":\"light\",\"show_save\": false,\"show_print\": false,\"show_help\": false,\"show_menu\": false,\"tool_visibility\": { \"normal\": true, \"highlight\": false, \"strikethrough\": true, \"underline\": true, \"comment\": true, \"text\": true, \"equation\": true, \"drawing\": true, \"shape\": true, \"eraser\": true, \"image\": true, \"autograph\": false }}, \"editable\": true}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(AUTHORIZATION, annotationAuthorization);

		RestTemplate restTemplate = new RestTemplate();

		HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
		String ret = restTemplate.postForObject(url, entity, String.class);

		String viewerUrl = "";
		try {
			JSONObject jsonObject = new JSONObject(ret);
			if (jsonObject.has("viewer_url")) {
				viewerUrl = jsonObject.getString("viewer_url");
			}
		} catch (JSONException e) {
			logger.error(FAIL_TO_PARSE_JSON, e);
		}

		logger.debug("viewerUrl " + viewerUrl);
		return viewerUrl;
	}

	/**
	 * Export and delete document from Kami
	 * @param scriptNode
	 * @return
	 * @throws InterruptedException
	 */
	@Override
	public NodeRef checkinDocument(NodeRef nodeRef) {
		logger.debug("exportDocument");
		String documentIdentifier = (String) nodeService.getProperty(nodeRef, AnnotationModel.PROP_ANNOTATION_DOCUMENT_IDENTIFIER);

		// Create document export
		String url = "https://api.notablepdf.com/embed/exports";
		String requestJson = "{  \"document_identifier\": \"" + documentIdentifier + "\",  \"export_type\": \"annotation\"}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(AUTHORIZATION, annotationAuthorization);

		RestTemplate restTemplate = new RestTemplate();

		HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
		String ret = restTemplate.postForObject(url, entity, String.class);

		String exportId = "";
		try {
			JSONObject jsonObject = new JSONObject(ret);
			if (jsonObject.has("id")) {
				exportId = jsonObject.getString("id");
				logger.debug("exportId " + exportId);
			}
		} catch (JSONException e) {
			logger.error(FAIL_TO_PARSE_JSON, e);
		}

		// Get document export
		url = "https://api.notablepdf.com/embed/exports/" + exportId;
		String fileUrl = "null";
		int tryGet = 0;

		// Try 10 times because status of export may be pending
		while ((tryGet < 10) && ("null".equals(fileUrl))) {
			if (tryGet > 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.error("Fail to sleep 1000ms", e);
				}
			}
			tryGet++;
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			if (logger.isDebugEnabled()) {
				logger.debug("tryGet : " + tryGet + " Response " + response);
			}
			try {
				JSONObject jsonObject = new JSONObject(response.getBody());
				if (jsonObject.has("file_url")) {
					fileUrl = jsonObject.getString("file_url");
				}
			} catch (JSONException e) {
				logger.error(FAIL_TO_PARSE_JSON, e);
			}
		}

		if (fileUrl != null) {
			NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(nodeRef);
			Map<String, Serializable> versionProperties = new HashMap<>();
			versionProperties.put(Version.PROP_DESCRIPTION, I18NUtil.getMessage("annotation.version.description"));
			versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MINOR);
			checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
			ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);

			if (writer != null) {
				try {
					copyContent(fileUrl, writer);
				} catch (MalformedURLException e) {
					throw new WebScriptException(e.getMessage());
				}
			}
		} else {
			throw new WebScriptException("File url is null so checkin annotation is not done.");
		}
		
		cancelDocument(nodeRef);
		
		return nodeRef;
	}

	private void copyContent(String fileUrl, ContentWriter writer) throws MalformedURLException {
		logger.debug("Load file from url " + fileUrl);
		URL targetUrl = new URL(fileUrl);

		try (InputStream input = targetUrl.openStream(); OutputStream ouput = writer.getContentOutputStream()) {

			IOUtils.copy(input, ouput);

		} catch (IOException e) {
			logger.error("Failed to write content in output stream", e);
		}
	}

	@Override
	public NodeRef cancelDocument(NodeRef nodeRef) {
		logger.debug("deleteDocument");
		String documentIdentifier = (String) nodeService.getProperty(nodeRef, AnnotationModel.PROP_ANNOTATION_DOCUMENT_IDENTIFIER);
		String url = "https://api.notablepdf.com/embed/documents/" + documentIdentifier;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.add(AUTHORIZATION, annotationAuthorization);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> entity = new HttpEntity<>("", headers);
		restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
		nodeService.removeAspect(nodeRef, AnnotationModel.ASPECT_ANNOTATION);
		
		return nodeRef;
	}
}
