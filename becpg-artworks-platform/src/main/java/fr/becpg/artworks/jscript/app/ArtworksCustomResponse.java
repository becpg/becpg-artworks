package fr.becpg.artworks.jscript.app;

import java.io.Serializable;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.repo.jscript.app.CustomResponse;

/**
 * Return current state of Annotation module
 *
 * @author quere
 */
public class ArtworksCustomResponse implements CustomResponse {

	private String annotationViewerLicenseKey;

	private String annotationAuthorization;
	
	private String signatureAuthorization;
	
	private String issuerCertificateURL;
	
	/**
	 * <p>Setter for the field <code>annotationViewerLicenseKey</code>.</p>
	 *
	 * @param annotationViewerLicenseKey a {@link java.lang.String} object
	 */
	public void setAnnotationViewerLicenseKey(String annotationViewerLicenseKey) {
		this.annotationViewerLicenseKey = annotationViewerLicenseKey;
	}

	/**
	 * <p>Setter for the field <code>annotationAuthorization</code>.</p>
	 *
	 * @param annotationAuthorization a {@link java.lang.String} object
	 */
	public void setAnnotationAuthorization(String annotationAuthorization) {
		this.annotationAuthorization = annotationAuthorization;
	}

	/**
	 * <p>Setter for the field <code>signatureAuthorization</code>.</p>
	 *
	 * @param signatureAuthorization a {@link java.lang.String} object
	 */
	public void setSignatureAuthorization(String signatureAuthorization) {
		this.signatureAuthorization = signatureAuthorization;
	}
	
	/**
	 * <p>Setter for the field <code>issuerCertificateURL</code>.</p>
	 *
	 * @param issuerCertificateURL a {@link java.lang.String} object
	 */
	public void setIssuerCertificateURL(String issuerCertificateURL) {
		this.issuerCertificateURL = issuerCertificateURL;
	}

	/** {@inheritDoc} */
	@Override
	public Serializable populate() {
		boolean annotationExternalEnabled = false;
		boolean signatureExternalEnabled = false;
		
		boolean viewerEnabled = false;
		
		if ((annotationAuthorization != null) && !annotationAuthorization.isEmpty()) {
			annotationExternalEnabled = true;
		}
		
		if ((signatureAuthorization != null) && !signatureAuthorization.isEmpty()) {
			signatureExternalEnabled = true;
		}

		if ((annotationViewerLicenseKey != null) && !annotationViewerLicenseKey.isEmpty()) {
			viewerEnabled = true;
		}
		

		Map<String, Serializable> jsonObj = new LinkedHashMap<>(1);
		jsonObj.put("annotationExternal", annotationExternalEnabled);
		jsonObj.put("signatureExternal", signatureExternalEnabled);
		jsonObj.put("signature", signatureExternalEnabled || viewerEnabled);
		jsonObj.put("viewer", viewerEnabled);
		if (viewerEnabled) {
			jsonObj.put("licenseKey", new String(Base64.getEncoder().encode(annotationViewerLicenseKey.getBytes())));
			
			jsonObj.put("issuerCertificateURL", issuerCertificateURL);
		}

		return (Serializable) jsonObj;
	}

}
