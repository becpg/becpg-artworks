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
package fr.becpg.artworks.jscript;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;

import fr.becpg.artworks.signature.SignatureService;

/**
 * Utility script methods
 *
 * @author valentin.leblanc
 * @version $Id: $Id
 */
public final class BeCPGArtworksScriptHelper extends BaseScopableProcessorExtension {
	
	private SignatureService signatureService;
	
	public void setSignatureService(SignatureService signatureService) {
		this.signatureService = signatureService;
	}
	
	public String getSignatureViewUrl(ScriptNode document, ScriptNode recipient) {
		return signatureService.getSignatureView(document.getNodeRef(), recipient.getNodeRef());
	}
	
	public String sendForSignature(ScriptNode document) {
		return signatureService.sendForSignature(document.getNodeRef(), false);
	}
	
	public void checkinSignature(ScriptNode document) {
		signatureService.retrieveSignedDocument(document.getNodeRef());
	}
	
}
