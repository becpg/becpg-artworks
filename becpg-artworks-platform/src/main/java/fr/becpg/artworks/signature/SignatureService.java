package fr.becpg.artworks.signature;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;

import fr.becpg.artworks.ArtworksDocumentHandler;

/**
 * <p>SignatureService interface.</p>
 *
 * @author matthieu
 */
public interface SignatureService extends ArtworksDocumentHandler{

	/**
	 * <p>prepareForSignature.</p>
	 *
	 * @param originalNode a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param recipients a {@link java.util.List} object
	 * @param notifyByMail a boolean
	 * @param params a {@link java.lang.String} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef prepareForSignature(NodeRef originalNode, List<NodeRef> recipients, boolean notifyByMail, String... params);
	
	/**
	 * <p>signDocument.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef signDocument(NodeRef nodeRef);

	/**
	 * <p>prepareForSignature.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param recipientNodes a {@link java.util.List} object
	 * @param jsonParams a {@link org.json.JSONObject} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef prepareForSignature(NodeRef nodeRef, List<NodeRef> recipientNodes, JSONObject jsonParams);
	
}
