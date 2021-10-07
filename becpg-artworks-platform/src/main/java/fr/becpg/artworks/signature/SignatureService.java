package fr.becpg.artworks.signature;

import org.alfresco.service.cmr.repository.NodeRef;

public interface SignatureService {

	public String sendForSignature(NodeRef nodeRef, boolean notifyByMail);
	public String getSignatureView(NodeRef nodeRef, NodeRef recipient, NodeRef task);
	public void retrieveSignedDocument(NodeRef nodeRef);
	public void deleteDocument(NodeRef nodeRef);
}
