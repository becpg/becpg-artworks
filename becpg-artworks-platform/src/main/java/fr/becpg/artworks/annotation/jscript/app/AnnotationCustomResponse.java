package fr.becpg.artworks.annotation.jscript.app;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.repo.jscript.app.CustomResponse;

/**
 * Return current state of Annotation module
 * @author quere
 *
 */
public class AnnotationCustomResponse implements CustomResponse {

	private String annotationViewerLicenseKey;

	private String annotationAuthorization;


	public void setAnnotationViewerLicenseKey(String annotationViewerLicenseKey) {
		this.annotationViewerLicenseKey = annotationViewerLicenseKey;
	}

	public void setAnnotationAuthorization(String annotationAuthorization) {
		this.annotationAuthorization = annotationAuthorization;
	}


	@Override
	public Serializable populate() {
		boolean externalEnabled = false;
		boolean viewerEnabled = false;
		if ((annotationAuthorization != null) && !annotationAuthorization.isEmpty()) {
			externalEnabled = true;
		}

		if ((annotationViewerLicenseKey != null) && !annotationViewerLicenseKey.isEmpty()) {
			viewerEnabled = true;
		}

		Map<String, Serializable> jsonObj = new LinkedHashMap<>(1);
		jsonObj.put("external", externalEnabled);
		jsonObj.put("viewer", viewerEnabled);
		if (viewerEnabled) {
			jsonObj.put("licenseKey", annotationViewerLicenseKey);
		}

		return (Serializable) jsonObj;
	}

}
