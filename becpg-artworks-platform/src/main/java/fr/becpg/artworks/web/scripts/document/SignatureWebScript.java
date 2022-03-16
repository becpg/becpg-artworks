/*
 *
 */
package fr.becpg.artworks.web.scripts.document;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.artworks.signature.SignatureService;

/**
 * @author valentinleblanc
 */
public class SignatureWebScript extends AbstractArtworksWebScript {

	private static final String ACTION_SIGN = "sign";
	private static final String PARAM_RECIPIENT = "recipient";

	public void setSignatureService(SignatureService signatureService) {
		setDocumentHandler(signatureService);
	}
	
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String action = templateArgs.get(PARAM_ACTION);
		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);

		NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);

		if (ACTION_SIGN.equals(action)) {
			
			NodeRef recipient = new NodeRef(req.getParameter(PARAM_RECIPIENT));
			
			if (!nodeService.exists(recipient)) {
				throw new WebScriptException("Recipient node doesn't exist");
			}
					
			if (nodeService.exists(nodeRef) && (lockService.getLockStatus(nodeRef) == LockStatus.NO_LOCK)) {
				((SignatureService) documentHandler).sign(nodeRef, recipient);
			} else {
				throw new WebScriptException("Node is locked or doesn't exist");
			}
		} else {
			super.execute(req, res);
		}
	}
}
