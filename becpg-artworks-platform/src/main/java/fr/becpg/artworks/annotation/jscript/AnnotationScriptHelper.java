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
package fr.becpg.artworks.annotation.jscript;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;

import fr.becpg.artworks.annotation.AnnotationService;

/**
 * Annotation script methods
 *
 * @author Philippe
 */
public final class AnnotationScriptHelper extends BaseScopableProcessorExtension {

	private AnnotationService annotationService;

	/**
	 * <p>Setter for the field <code>annotationService</code>.</p>
	 *
	 * @param annotationService a {@link fr.becpg.artworks.annotation.AnnotationService} object
	 */
	public void setAnnotationService(AnnotationService annotationService) {
		this.annotationService = annotationService;
	}
	
	
	/**
	 * <p>checkoutDocument.</p>
	 *
	 * @param scriptNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link java.lang.String} object
	 */
	public String checkoutDocument(ScriptNode scriptNode) {
		return annotationService.checkoutDocument(scriptNode.getNodeRef());
	}

	/**
	 * <p>getOrCreateRemoteView.</p>
	 *
	 * @param scriptNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link java.lang.String} object
	 */
	public String getOrCreateRemoteView(ScriptNode scriptNode) {
		return annotationService.getDocumentView(scriptNode.getNodeRef(), null ,null);
	}

	/**
	 * <p>checkinDocument.</p>
	 *
	 * @param scriptNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 */
	public void checkinDocument(ScriptNode scriptNode) {
		annotationService.checkinDocument(scriptNode.getNodeRef());
	}

	/**
	 * <p>cancelDocument.</p>
	 *
	 * @param scriptNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 */
	public void cancelDocument(ScriptNode scriptNode) {
		annotationService.cancelDocument(scriptNode.getNodeRef());
	}
}
