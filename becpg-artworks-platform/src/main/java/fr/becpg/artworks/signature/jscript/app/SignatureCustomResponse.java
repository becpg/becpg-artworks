package fr.becpg.artworks.signature.jscript.app;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.repo.jscript.app.CustomResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Return current state of Signature module
 * @author leblanc
 *
 */
public class SignatureCustomResponse implements CustomResponse{
	
	private static Log logger = LogFactory.getLog(SignatureCustomResponse.class);
	private String signatureAuthorization;
	
	public void setSignatureAuthorization(String signatureAuthorization) {
		this.signatureAuthorization = signatureAuthorization;
	}

	@Override
	public Serializable populate() {
		boolean isEnabled = false;
		if(signatureAuthorization != null && !signatureAuthorization.isEmpty()){
			isEnabled = true;
		}
		logger.debug("signatureAuthorization " + signatureAuthorization + " isEnabled: " + isEnabled);
		Map<String, Serializable> jsonObj = new LinkedHashMap<>(1);
		jsonObj.put("enabled", isEnabled);
		return (Serializable)jsonObj;
	}

}
