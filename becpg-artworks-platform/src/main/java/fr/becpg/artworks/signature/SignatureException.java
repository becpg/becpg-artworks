package fr.becpg.artworks.signature;

import org.alfresco.error.AlfrescoRuntimeException;

public class SignatureException extends AlfrescoRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SignatureException(String msgId) {
		super(msgId);
	}

	public SignatureException(String msgId, Exception e) {
		super(msgId, e);
	}

}
