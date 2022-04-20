package fr.becpg.artworks.compare;

import java.io.File;
import java.io.IOException;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TempFileProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.artworks.compare.pdfcompare.CompareResult;
import fr.becpg.artworks.compare.pdfcompare.PdfComparator;
import fr.becpg.artworks.helper.NodeContentHelper;

@Service
public class PdfFileComparePlugin implements CompareDocumentPlugin {

	private static final String PDF_IDENTIFIER = "pdf";
	
	private static final String RESULT_FILE_NAME = "result";
	
	private static final String PDF_EXTENSION = ".pdf";

	@Autowired
	private ContentService contentService;

	@Autowired
	private MimetypeService mimetypeService;
	
	@Autowired
	private NodeContentHelper nodeContentHelper;

	@Override
	public File compare(NodeRef node1, NodeRef node2) throws IOException {
		
		File file1 = nodeContentHelper.createContentFile(node1);
		File file2 = nodeContentHelper.createContentFile(node2);

		PdfComparator<CompareResult> pdfComparator = new PdfComparator<>(file1, file2);
		
		File result = TempFileProvider.createTempFile(RESULT_FILE_NAME, PDF_EXTENSION);
		
		String name = result.getName();
        String fileName = name.replace(PDF_EXTENSION, "");
		
		pdfComparator.compare().writeTo(result.getPath().replace(name, fileName));

		nodeContentHelper.deleteFile(file1);
		nodeContentHelper.deleteFile(file2);

		return result;
		
	}
	
	@Override
	public boolean accepts(NodeRef nodeRef) {
		
		ContentReader reader = this.contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

		String mimeType = mimetypeService.guessMimetype(null, reader);

		return mimeType.contains(PDF_IDENTIFIER);
		
	}
	
}
