/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.artworks.signature.jscript;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.util.json.JSONException;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;

import fr.becpg.artworks.signature.SignatureService;
import fr.becpg.artworks.signature.model.SignatureModel;

/**
 * Utility script methods
 *
 * @author valentin.leblanc
 * @version $Id: $Id
 */
public final class SignatureScriptHelper extends BaseScopableProcessorExtension {

	private SignatureService signatureService;
	
	private BehaviourFilter policyBehaviourFilter;
	
	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}
	
	/**
	 * <p>Setter for the field <code>signatureService</code>.</p>
	 *
	 * @param signatureService a {@link fr.becpg.artworks.signature.SignatureService} object
	 */
	public void setSignatureService(SignatureService signatureService) {
		this.signatureService = signatureService;
	}
	
	private ServiceRegistry serviceRegistry;
	
	/**
	 * <p>Setter for the field <code>serviceRegistry</code>.</p>
	 *
	 * @param serviceRegistry a {@link org.alfresco.service.ServiceRegistry} object
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	
	/**
	 * <p>getSignatureView.</p>
	 *
	 * @param document a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param user a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param task a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object
	 */
	public String getSignatureView(ScriptNode document, ScriptNode user, NodeRef task) {
		return signatureService.getDocumentView(document.getNodeRef(), user == null ? null : user.getNodeRef(), task);
	}
	
	/**
	 * <p>disableSignaturePolicy.</p>
	 */
	public void disableSignaturePolicy() {
		policyBehaviourFilter.disableBehaviour(SignatureModel.ASPECT_SIGNATURE);
	}
	
	/**
	 * <p>enableSignaturePolicy.</p>
	 */
	public void enableSignaturePolicy() {
		policyBehaviourFilter.enableBehaviour(SignatureModel.ASPECT_SIGNATURE);
	}

	/**
	 * <p>prepareForSignature.</p>
	 *
	 * @param document a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param recipients an array of {@link org.alfresco.repo.jscript.ScriptNode} objects
	 * @param params a {@link java.lang.String} object
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object
	 */
	public ScriptNode prepareForSignature(ScriptNode document, ScriptNode[] recipients, String... params) {
		List<NodeRef> recipientNodes = new ArrayList<>();
		for (ScriptNode recipient : recipients) {
			recipientNodes.add(recipient.getNodeRef());
		}
		if (params.length == 1) {
			try {
				JSONObject jsonParams = new JSONObject(params[0]);
				return new ScriptNode(signatureService.prepareForSignature(document.getNodeRef(), recipientNodes, jsonParams), serviceRegistry, getScope());
			} catch (JSONException e) {
				// nothing
			}
		}
		return new ScriptNode(signatureService.prepareForSignature(document.getNodeRef(), recipientNodes, false, params), serviceRegistry, getScope());
	}
	
	/**
	 * <p>checkinDocument.</p>
	 *
	 * @param document a {@link org.alfresco.repo.jscript.ScriptNode} object
	 */
	public void checkinDocument(ScriptNode document) {
		signatureService.checkinDocument(document.getNodeRef());
	}
	
	/**
	 * <p>signDocument.</p>
	 *
	 * @param document a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object
	 */
	public ScriptNode signDocument(ScriptNode document) {
		return new ScriptNode(signatureService.signDocument(document.getNodeRef()), serviceRegistry, getScope());
	}
	
	/**
	 * <p>cancelSignature.</p>
	 *
	 * @param document a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object
	 */
	public ScriptNode cancelSignature(ScriptNode document) {
		return new ScriptNode(signatureService.cancelDocument(document.getNodeRef()), serviceRegistry, getScope());
	}

}
