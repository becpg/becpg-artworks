package fr.becpg.artworks.signature.model;

import org.alfresco.service.namespace.QName;

public final class SignatureModel {

	private SignatureModel() {
	  throw new IllegalStateException("Constants class helper only");
	}
	
	static final String SIGNATURE_URI = "http://www.becpg.fr/signature/1.0";
	// Signature aspect
	public static final QName ASPECT_SIGNATURE = QName.createQName(SignatureModel.SIGNATURE_URI, "signatureAspect");
	public static final QName PROP_DOCUMENT_IDENTIFIER = QName.createQName(SignatureModel.SIGNATURE_URI, "documentIdentifier");
	public static final QName ASSOC_RECIPIENTS = QName.createQName(SignatureModel.SIGNATURE_URI, "recipients");
	public static final QName ASSOC_PREPARED_RECIPIENTS = QName.createQName(SignatureModel.SIGNATURE_URI, "preparedRecipients");
	public static final QName ASSOC_VALIDATOR = QName.createQName(SignatureModel.SIGNATURE_URI, "validator");
	public static final QName PROP_VALIDATION_DATE = QName.createQName(SignatureModel.SIGNATURE_URI, "validationDate");
	public static final QName PROP_RECIPIENTS_DATA = QName.createQName(SignatureModel.SIGNATURE_URI, "recipientsData");
	public static final QName PROP_STATUS = QName.createQName(SignatureModel.SIGNATURE_URI, "status");

}