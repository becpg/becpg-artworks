package fr.becpg.artworks.annotation.model;

import org.alfresco.service.namespace.QName;

/**
 * <p>AnnotationModel class.</p>
 *
 * @author matthieu
 */
public final class AnnotationModel {

	/**
	 * <p>Constructor for AnnotationModel.</p>
	 */
	private AnnotationModel() {
	  throw new IllegalStateException("Constants class helper only");
	}
	
	/** Constant <code>ANNOTATION_URI="http://www.becpg.fr/annotation/1.0"</code> */
	static final String ANNOTATION_URI = "http://www.becpg.fr/annotation/1.0";
	// Annotation aspect
	/** Constant <code>ASPECT_ANNOTATION</code> */
	public static final QName ASPECT_ANNOTATION = QName.createQName(AnnotationModel.ANNOTATION_URI, "annotationAspect");
	/** Constant <code>PROP_ANNOTATION_DOCUMENT_IDENTIFIER</code> */
	public static final QName PROP_ANNOTATION_DOCUMENT_IDENTIFIER = QName.createQName(AnnotationModel.ANNOTATION_URI, "annotationDocumentIdentifier");

}
