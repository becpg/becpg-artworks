package fr.becpg.artworks.helper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContentHelper {

	private static final Log logger = LogFactory.getLog(ContentHelper.class);

	private ContentService contentService;

	private MimetypeService mimetypeService;
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	public void writeContent(NodeRef nodeRef, byte[] ret) {
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
	
	public File createContentFile(NodeRef entity) throws IOException {
		String name = (String) nodeService.getProperty(entity, ContentModel.PROP_NAME);
		
		File file = TempFileProvider.createTempFile(name.split("\\.")[0], "." + name.split("\\.")[1]);
		
		ContentReader reader = this.contentService.getReader(entity, ContentModel.PROP_CONTENT);

		if (reader != null) {
			reader.getContent(new FileOutputStream(file));
		}

		return file;
	}
	
	public void deleteFile(File file) throws IOException {
		try {
			Files.delete(file.toPath());
		} catch (NoSuchFileException e) {
			logger.debug("File does not exist : " + file.getName(), e);
		}
	}

}
