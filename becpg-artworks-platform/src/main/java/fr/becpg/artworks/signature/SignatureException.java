package fr.becpg.artworks.signature;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * <p>SignatureException class.</p>
 *
 * @author matthieu
 */
public class SignatureException extends AlfrescoRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for SignatureException.</p>
	 *
	 * @param msgId a {@link java.lang.String} object
	 */
	public SignatureException(String msgId) {
		super(msgId);
	}

	/**
	 * <p>Constructor for SignatureException.</p>
	 *
	 * @param msgId a {@link java.lang.String} object
	 * @param e a {@link java.lang.Exception} object
	 */
	public SignatureException(String msgId, Exception e) {
		super(msgId, e);
	}

}
