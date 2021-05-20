package fr.becpg.artworks.compare;

import java.io.IOException;
import java.io.OutputStream;

import org.alfresco.service.cmr.repository.NodeRef;

public interface CompareDocumentService {

	public String compare(NodeRef expectedNode, NodeRef actualNode, OutputStream out) throws IOException;

}