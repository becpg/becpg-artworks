/*
 *
 */
package fr.becpg.artworks.web.scripts.document;

import fr.becpg.artworks.annotation.AnnotationService;

/**
 * <p>AnnotationWebScript class.</p>
 *
 * @author querephi
 */
public class AnnotationWebScript extends AbstractArtworksWebScript {

	/**
	 * <p>setAnnotationService.</p>
	 *
	 * @param annotationService a {@link fr.becpg.artworks.annotation.AnnotationService} object
	 */
	public void setAnnotationService(AnnotationService annotationService) {
		setDocumentHandler(annotationService);
	}

}
