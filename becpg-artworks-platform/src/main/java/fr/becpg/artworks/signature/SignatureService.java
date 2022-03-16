package fr.becpg.artworks.signature;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.artworks.ArtworksDocumentHandler;

public interface SignatureService extends ArtworksDocumentHandler{

	public String prepareForSignature(NodeRef originalNode, List<NodeRef> recipients, boolean notifyByMail, String... params);
	
	public void sign(NodeRef nodeRef, NodeRef recipient);
	
}
