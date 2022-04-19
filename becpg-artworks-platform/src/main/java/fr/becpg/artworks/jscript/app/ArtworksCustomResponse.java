package fr.becpg.artworks.jscript.app;

import java.io.Serializable;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.repo.jscript.app.CustomResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.becpg.artworks.signature.SignatureUtils;

/**
 * Return current state of Annotation module
 * @author quere
 *
 */
public class ArtworksCustomResponse implements CustomResponse {

	private static final Logger logger = LoggerFactory.getLogger(ArtworksCustomResponse.class);
	
	private String annotationViewerLicenseKey;

	private String annotationAuthorization;
	
	private String signatureAuthorization;
	
	private String signatureKeyStoreAlias;
	
	public void setSignatureKeyStoreAlias(String signatureKeyStoreAlias) {
		this.signatureKeyStoreAlias = signatureKeyStoreAlias;
	}

	public void setAnnotationViewerLicenseKey(String annotationViewerLicenseKey) {
		this.annotationViewerLicenseKey = annotationViewerLicenseKey;
	}

	public void setAnnotationAuthorization(String annotationAuthorization) {
		this.annotationAuthorization = annotationAuthorization;
	}

	public void setSignatureAuthorization(String signatureAuthorization) {
		this.signatureAuthorization = signatureAuthorization;
	}

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
			
			Certificate[] certificationChain = SignatureUtils.getCertificateChain(signatureKeyStoreAlias);
			
			if (certificationChain != null && certificationChain.length > 0 ) {
				X509Certificate certificate = (X509Certificate) certificationChain[0];
				try {
					jsonObj.put("certificate", new String(certificate.getEncoded()));
				} catch (CertificateEncodingException e) {
					logger.error("Could not get the encoded certificate data", e);
				}
			}
		}

		return (Serializable) jsonObj;
	}

}
