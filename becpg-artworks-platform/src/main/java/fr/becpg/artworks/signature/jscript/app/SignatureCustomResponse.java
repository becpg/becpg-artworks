package fr.becpg.artworks.signature.jscript.app;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.repo.jscript.app.CustomResponse;

/**
 * Return current state of Signature module
 * @author leblanc
 *
 */
public class SignatureCustomResponse implements CustomResponse{
	
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
		Map<String, Serializable> jsonObj = new LinkedHashMap<>(1);
		jsonObj.put("enabled", isEnabled);
		return (Serializable)jsonObj;
	}

}
