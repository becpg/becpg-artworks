package fr.becpg.artworks.signature;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;

import fr.becpg.artworks.ArtworksDocumentHandler;

public interface SignatureService extends ArtworksDocumentHandler{

	public NodeRef prepareForSignature(NodeRef originalNode, List<NodeRef> recipients, boolean notifyByMail, String... params);
	
	public NodeRef signDocument(NodeRef nodeRef);

	public NodeRef prepareForSignature(NodeRef nodeRef, List<NodeRef> recipientNodes, JSONObject jsonParams);
	
}
