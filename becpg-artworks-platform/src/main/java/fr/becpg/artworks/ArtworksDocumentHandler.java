package fr.becpg.artworks;

import org.alfresco.service.cmr.repository.NodeRef;

public interface ArtworksDocumentHandler {

	public String checkoutDocument(NodeRef nodeRef);
	public void checkinDocument(NodeRef nodeRef);
	public void cancelDocument(NodeRef nodeRef);
	public String getDocumentView(NodeRef nodeRef,String userId, String returnUrl);
	
	
}
