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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.artworks.signature.SignatureService;

/**
 * Utility script methods
 *
 * @author valentin.leblanc
 * @version $Id: $Id
 */
public final class SignatureScriptHelper extends BaseScopableProcessorExtension {

	private SignatureService signatureService;

	public void setSignatureService(SignatureService signatureService) {
		this.signatureService = signatureService;
	}

	public String getSignatureView(ScriptNode document, String userName, NodeRef task) {
		return signatureService.getDocumentView(document.getNodeRef(), userName, task);
	}

	public String prepareForSignature(ScriptNode document, ScriptNode[] recipients, String... params) {
		
		List<NodeRef> recipientNodes = new ArrayList<>();
			
		for (ScriptNode recipient : recipients) {
			recipientNodes.add(recipient.getNodeRef());
		}
		
		return signatureService.prepareForSignature(document.getNodeRef(), recipientNodes, false, params);
	}

	public void checkinDocument(ScriptNode document) {
		signatureService.checkinDocument(document.getNodeRef());
	}
	
	public void signDocument(ScriptNode document) {
		signatureService.signDocument(document.getNodeRef());
	}

}
