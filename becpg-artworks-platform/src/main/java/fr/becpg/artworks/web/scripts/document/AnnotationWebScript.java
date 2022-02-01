/*
 *
 */
package fr.becpg.artworks.web.scripts.document;

import fr.becpg.artworks.annotation.AnnotationService;

/**
 * @author querephi
 */
public class AnnotationWebScript extends AbstractArtworksWebScript {

	public void setAnnotationService(AnnotationService annotationService) {
		setDocumentHandler(annotationService);
	}

}