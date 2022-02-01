package fr.becpg.artworks.signature;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.artworks.ArtworksDocumentHandler;

public interface SignatureService extends ArtworksDocumentHandler{

	String prepareForSignature(NodeRef nodeRef, boolean notifyByMail);
	
}
