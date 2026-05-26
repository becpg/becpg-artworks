package fr.becpg.artworks.signature.model;

import org.alfresco.service.namespace.QName;

/**
 * <p>SignatureModel class.</p>
 *
 * @author matthieu
 */
public final class SignatureModel {

	/**
	 * <p>Constructor for SignatureModel.</p>
	 */
	private SignatureModel() {
	  throw new IllegalStateException("Constants class helper only");
	}
	
	/** Constant <code>SIGNATURE_URI="http://www.becpg.fr/signature/1.0"</code> */
	static final String SIGNATURE_URI = "http://www.becpg.fr/signature/1.0";
	// Signature aspect
	/** Constant <code>ASPECT_SIGNATURE</code> */
	public static final QName ASPECT_SIGNATURE = QName.createQName(SignatureModel.SIGNATURE_URI, "signatureAspect");
	/** Constant <code>PROP_DOCUMENT_IDENTIFIER</code> */
	public static final QName PROP_DOCUMENT_IDENTIFIER = QName.createQName(SignatureModel.SIGNATURE_URI, "documentIdentifier");
	/** Constant <code>ASSOC_RECIPIENTS</code> */
	public static final QName ASSOC_RECIPIENTS = QName.createQName(SignatureModel.SIGNATURE_URI, "recipients");
	/** Constant <code>ASSOC_PREPARED_RECIPIENTS</code> */
	public static final QName ASSOC_PREPARED_RECIPIENTS = QName.createQName(SignatureModel.SIGNATURE_URI, "preparedRecipients");
	/** Constant <code>ASSOC_VALIDATOR</code> */
	public static final QName ASSOC_VALIDATOR = QName.createQName(SignatureModel.SIGNATURE_URI, "validator");
	/** Constant <code>PROP_VALIDATION_DATE</code> */
	public static final QName PROP_VALIDATION_DATE = QName.createQName(SignatureModel.SIGNATURE_URI, "validationDate");
	/** Constant <code>PROP_RECIPIENTS_DATA</code> */
	public static final QName PROP_RECIPIENTS_DATA = QName.createQName(SignatureModel.SIGNATURE_URI, "recipientsData");
	/** Constant <code>PROP_STATUS</code> */
	public static final QName PROP_STATUS = QName.createQName(SignatureModel.SIGNATURE_URI, "status");

}
