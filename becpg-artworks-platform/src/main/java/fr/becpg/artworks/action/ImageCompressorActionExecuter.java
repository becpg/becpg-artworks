/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.artworks.action;

import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.TransformActionExecuter;
import org.alfresco.repo.content.transform.TransformerDebug;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.rendition2.SynchronousTransformClient;
import org.alfresco.repo.rendition2.TransformationOptionsConverter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Action used to compress, transform an image
 *
 * @author querephi
 * @version $Id: $Id
 */
@Deprecated
public class ImageCompressorActionExecuter extends TransformActionExecuter {

	/** The Constant NAME. */
	public static final String NAME = "compress-image";
	/** Constant <code>PARAM_CONVERT_COMMAND="convert-command"</code> */
	public static final String PARAM_CONVERT_COMMAND = "convert-command";

	private static final Log logger = LogFactory.getLog(ImageCompressorActionExecuter.class);

	private static final String CONTENT_READER_NOT_FOUND_MESSAGE = "Can not find Content Reader for document. Operation can't be performed";
	private static final String TRANSFORMING_ERROR_MESSAGE = "Some error occurred during document transforming. Error message: ";

	private ContentService contentService;

	private DictionaryService dictionaryService;
	private NodeService nodeService;

	private TransformationOptionsConverter converter;
	private SynchronousTransformClient synchronousTransformClient;

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	@Override
	public void setContentService(ContentService contentService) {
		super.setContentService(contentService);
		this.contentService = contentService;
	}

	@Override
	public void setDictionaryService(DictionaryService dictionaryService) {
		super.setDictionaryService(dictionaryService);
		this.dictionaryService = dictionaryService;
	}

	@Override
	public void setNodeService(NodeService nodeService) {
		super.setNodeService(nodeService);
		this.nodeService = nodeService;
	}

	@Override
	public void setConverter(TransformationOptionsConverter converter) {
		super.setConverter(converter);
		this.converter = converter;
	}

	@Override
	public void setSynchronousTransformClient(SynchronousTransformClient synchronousTransformClient) {
		super.setSynchronousTransformClient(synchronousTransformClient);
		this.synchronousTransformClient = synchronousTransformClient;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Add parameter definitions
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList
				.add(new ParameterDefinitionImpl(PARAM_CONVERT_COMMAND, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_CONVERT_COMMAND)));
	}

	/** {@inheritDoc} */
	@Override
	protected void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) {

		if (this.nodeService.exists(actionedUponNodeRef) == false) {
			// node doesn't exist - can't do anything
			return;
		}
		// First check that the node is a sub-type of content
		QName typeQName = this.nodeService.getType(actionedUponNodeRef);
		if (this.dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT) == false) {
			// it is not content, so can't transform
			return;
		}

		// Get the mime type
		String mimeType = (String) ruleAction.getParameterValue(PARAM_MIME_TYPE);
		// Get the content reader
		ContentReader contentReader = this.contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
		if (null == contentReader || !contentReader.exists()) {
			throw new RuleServiceException(CONTENT_READER_NOT_FOUND_MESSAGE);
		}

		TransformationOptions transformationOptions = newTransformationOptions(ruleAction, actionedUponNodeRef);
		// getExecuteAsynchronously() is not true for async convert content rules, so using Thread name
		// options.setUse(ruleAction.getExecuteAsynchronously() ? "asyncRule" :"syncRule");
		transformationOptions.setUse(Thread.currentThread().getName().contains("Async") ? "asyncRule" : "syncRule");

		String sourceMimetype = contentReader.getMimetype();
		long sourceSizeInBytes = contentReader.getSize();
		String contentUrl = contentReader.getContentUrl();
		Map<String, String> options = converter.getOptions(transformationOptions, sourceMimetype, mimeType);
		if (!synchronousTransformClient.isSupported(sourceMimetype, sourceSizeInBytes, contentUrl, mimeType, options, null, actionedUponNodeRef)) {
			String optionsString = TransformerDebug.toString(options);
			throw new RuleServiceException(String.format(TRANSFORMER_NOT_EXISTS_MESSAGE_PATTERN, sourceMimetype, mimeType, optionsString));
		}

		ContentWriter contentWriter = contentService.getWriter(actionedUponNodeRef, ContentModel.PROP_CONTENT, true);

		try {
			doTransform(ruleAction, actionedUponNodeRef, contentReader, actionedUponNodeRef, contentWriter);
		} catch (NoTransformerException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("No transformer found to execute rule: \n" + "   reader: " + contentReader + "\n" + "   writer: " + contentWriter + "\n"
						+ "   action: " + this);
			}
			throw new RuleServiceException(TRANSFORMING_ERROR_MESSAGE + e.getMessage());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Image compressor. convertCommand: " + (String) ruleAction.getParameterValue(PARAM_CONVERT_COMMAND) + " - initialSize: "
					+ contentReader.getSize() + " - afterSize: " + contentWriter.getSize());
		}
	}

	@Override
	protected TransformationOptions newTransformationOptions(Action ruleAction, NodeRef sourceNodeRef) {
		ImageTransformationOptions options = new ImageTransformationOptions();
		options.setSourceNodeRef(sourceNodeRef);
		options.setSourceContentProperty(ContentModel.PROP_NAME);
		options.setTargetContentProperty(ContentModel.PROP_NAME);

		String convertCommand = (String) ruleAction.getParameterValue(PARAM_CONVERT_COMMAND);
		options.setCommandOptions(convertCommand);

		return options;
	}

}
