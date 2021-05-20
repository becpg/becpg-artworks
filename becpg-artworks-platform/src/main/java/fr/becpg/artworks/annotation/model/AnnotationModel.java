package fr.becpg.artworks.annotation.model;

import org.alfresco.service.namespace.QName;

public final class AnnotationModel {

	private AnnotationModel() {
	  throw new IllegalStateException("Constants class helper only");
	}
	
	static final String ANNOTATION_URI = "http://www.becpg.fr/annotation/1.0";
	// Annotation aspect
	public static final QName ASPECT_ANNOTATION = QName.createQName(AnnotationModel.ANNOTATION_URI, "annotationAspect");
	public static final QName PROP_ANNOTATION_DOCUMENT_IDENTIFIER = QName.createQName(AnnotationModel.ANNOTATION_URI, "annotationDocumentIdentifier");

}