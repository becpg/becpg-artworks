/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.UrlUtil;

import fr.becpg.artworks.signature.SignatureService;

/**
 * Utility script methods
 *
 * @author valentin.leblanc
 * @version $Id: $Id
 */
public final class SignatureScriptHelper extends BaseScopableProcessorExtension {

	private SignatureService signatureService;

	private SysAdminParams sysAdminParams;

	public void setSignatureService(SignatureService signatureService) {
		this.signatureService = signatureService;
	}

	public void setSysAdminParams(SysAdminParams sysAdminParams) {
		this.sysAdminParams = sysAdminParams;
	}

	public String getSignatureViewUrl(ScriptNode document, String userName, NodeRef task) {

		String returnUrl = UrlUtil.getShareUrl(sysAdminParams) + "/service/becpg/project/task-edit-url?nodeRef=" + task.toString();

		return signatureService.getDocumentView(document.getNodeRef(), userName, returnUrl);
	}

	public String prepareForSignature(ScriptNode document) {
		return signatureService.prepareForSignature(document.getNodeRef(), false);
	}

	public void checkinAndSign(ScriptNode document) {
		signatureService.checkinDocument(document.getNodeRef());
	}

}
