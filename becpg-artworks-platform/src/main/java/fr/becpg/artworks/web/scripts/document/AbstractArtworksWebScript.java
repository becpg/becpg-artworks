package fr.becpg.artworks.web.scripts.document;

import java.io.IOException;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.artworks.ArtworksDocumentHandler;

public abstract class AbstractArtworksWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(AbstractArtworksWebScript.class);

	private static final String ACTION_CHECKOUT = "checkout";
	private static final String ACTION_CHECKIN = "checkin";
	private static final String ACTION_CANCEL = "cancel";
	private static final String ACTION_CREATE_SESSION = "create-session";

	private static final String PARAM_ACTION = "action";
	private static final String PARAM_STORE_TYPE = "store_type";
	private static final String PARAM_STORE_ID = "store_id";
	private static final String PARAM_ID = "id";

	private ArtworksDocumentHandler documentHandler;

	private NodeService nodeService;

	private LockService lockService;

	protected void setDocumentHandler(ArtworksDocumentHandler documentHandler) {
		this.documentHandler = documentHandler;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String action = templateArgs.get(PARAM_ACTION);
		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);

		NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);
		String viewerUrl = null;

		if (nodeService.exists(nodeRef) && (lockService.getLockStatus(nodeRef) == LockStatus.NO_LOCK)) {

			if (ACTION_CHECKOUT.equals(action)) {
				documentHandler.checkoutDocument(nodeRef);
				viewerUrl = documentHandler.getDocumentView(nodeRef, AuthenticationUtil.getFullyAuthenticatedUser(), null);
			} else if (ACTION_CHECKIN.equals(action)) {
				documentHandler.checkinDocument(nodeRef);
			} else if (ACTION_CANCEL.equals(action)) {
				documentHandler.cancelDocument(nodeRef);
			} else if (ACTION_CREATE_SESSION.equals(action)) {
				viewerUrl = documentHandler.getDocumentView(nodeRef, AuthenticationUtil.getFullyAuthenticatedUser(), null);
			} else {
				String error = "Unsupported action: " + action;
				logger.error(error);
				throw new WebScriptException(error);
			}
		}

		try {

			JSONObject ret = new JSONObject();

			if (viewerUrl != null) {
				ret.put("viewerUrl", viewerUrl);
			}
			ret.put("status", "SUCCESS");

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}

	}

}
