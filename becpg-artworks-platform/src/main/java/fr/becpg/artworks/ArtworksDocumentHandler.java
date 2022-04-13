package fr.becpg.artworks;

import org.alfresco.service.cmr.repository.NodeRef;

public interface ArtworksDocumentHandler {

	public String checkoutDocument(NodeRef nodeRef);
	public NodeRef checkinDocument(NodeRef nodeRef);
	public NodeRef cancelDocument(NodeRef nodeRef);
	public String getDocumentView(NodeRef nodeRef, NodeRef user, NodeRef task);
	
	
}
