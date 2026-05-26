/*
 *
 */
package fr.becpg.artworks.web.scripts.document;

import fr.becpg.artworks.signature.SignatureService;

/**
 * <p>SignatureWebScript class.</p>
 *
 * @author valentinleblanc
 */
public class SignatureWebScript extends AbstractArtworksWebScript {

	/**
	 * <p>setSignatureService.</p>
	 *
	 * @param signatureService a {@link fr.becpg.artworks.signature.SignatureService} object
	 */
	public void setSignatureService(SignatureService signatureService) {
		setDocumentHandler(signatureService);
	}
	
}
