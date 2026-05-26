package fr.becpg.artworks;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>ArtworksDocumentHandler interface.</p>
 *
 * @author matthieu
 */
public interface ArtworksDocumentHandler {

	/**
	 * <p>checkoutDocument.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object
	 */
	public String checkoutDocument(NodeRef nodeRef);
	/**
	 * <p>checkinDocument.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef checkinDocument(NodeRef nodeRef);
	/**
	 * <p>cancelDocument.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef cancelDocument(NodeRef nodeRef);
	/**
	 * <p>getDocumentView.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param user a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param task a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object
	 */
	public String getDocumentView(NodeRef nodeRef, NodeRef user, NodeRef task);
	
	
}
