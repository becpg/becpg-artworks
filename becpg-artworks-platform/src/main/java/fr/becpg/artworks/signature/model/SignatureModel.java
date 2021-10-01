package fr.becpg.artworks.signature.model;

import org.alfresco.service.namespace.QName;

public final class SignatureModel {

	private SignatureModel() {
	  throw new IllegalStateException("Constants class helper only");
	}
	
	static final String SIGNATURE_URI = "http://www.becpg.fr/signature/1.0";
	// Signature aspect
	public static final QName ASPECT_SIGNATURE = QName.createQName(SignatureModel.SIGNATURE_URI, "signatureAspect");
	public static final QName PROP_SIGNATURE_DOCUMENT_IDENTIFIER = QName.createQName(SignatureModel.SIGNATURE_URI, "signatureDocumentIdentifier");
	public static final QName ASSOC_SIGNATURE_RECIPIENTS = QName.createQName(SignatureModel.SIGNATURE_URI, "recipients");
	public static final QName PROP_SIGNATURE_CLIENT_USER_IDS = QName.createQName(SignatureModel.SIGNATURE_URI, "clientUserIds");

}