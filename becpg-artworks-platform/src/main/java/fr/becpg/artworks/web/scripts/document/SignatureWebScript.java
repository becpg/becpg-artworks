/*
 *
 */
package fr.becpg.artworks.web.scripts.document;

import fr.becpg.artworks.signature.SignatureService;

/**
 * @author valentinleblanc
 */
public class SignatureWebScript extends AbstractArtworksWebScript {

	public void setSignatureService(SignatureService signatureService) {
		setDocumentHandler(signatureService);
	}
	
}
