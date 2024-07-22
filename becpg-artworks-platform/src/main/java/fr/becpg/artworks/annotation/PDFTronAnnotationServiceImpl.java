/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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
package fr.becpg.artworks.annotation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.springframework.extensions.surf.util.I18NUtil;

public final class PDFTronAnnotationServiceImpl implements AnnotationService {

	private CheckOutCheckInService checkOutCheckInService;

	public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
		this.checkOutCheckInService = checkOutCheckInService;
	}

	@Override
	public String checkoutDocument(NodeRef nodeRef) {
		return checkOutCheckInService.checkout(nodeRef).toString();
		
	}

	@Override
	public String getDocumentView(NodeRef nodeRef, NodeRef personNodeRef, NodeRef task) {
		return "artworks-viewer?nodeRef=" + checkOutCheckInService.getWorkingCopy(nodeRef);
	}

	@Override
	public NodeRef checkinDocument(NodeRef nodeRef) {
		Map<String, Serializable> versionProperties = new HashMap<>();
		versionProperties.put(Version.PROP_DESCRIPTION, I18NUtil.getMessage("annotation.version.description"));
		versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MINOR);
		return checkOutCheckInService.checkin(nodeRef, versionProperties);
	}

	@Override
	public NodeRef cancelDocument(NodeRef nodeRef) {
		return checkOutCheckInService.cancelCheckout(nodeRef);
	}
}
