package fr.becpg.artworks;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

import fr.becpg.artworks.compare.CompareDocumentService;

@RunWith(AlfrescoTestRunner.class)
public class CompareDocumentServiceIT extends RepoBaseTest {

	private CompareDocumentService compareDocumentService;
	
	@Before
	@Override
	public void initialize() {
		super.initialize();
		ApplicationContext ctx = getApplicationContext();
        if (ctx != null) {
        	Object bean = ctx.getBean("compareDocumentService");
        	if (bean instanceof CompareDocumentService) {
        		compareDocumentService = (CompareDocumentService) bean;
        	}
        }
	}
	
	@Test
	public void testComparePdf() throws IOException {
		
		NodeRef contentNode1 = transactionHelper.doInTransaction(() -> {
			return createNodeWithContent(testFolder, "sample1.pdf", "becpg/repo/document/sample1.pdf");
		}, false, true);
		
		assertTrue(nodeService.exists(contentNode1));
		
		NodeRef contentNode2 = transactionHelper.doInTransaction(() -> {
			return createNodeWithContent(testFolder, "sample2.pdf", "becpg/repo/document/sample2.pdf");
		}, false, true);
		
        assertTrue(nodeService.exists(contentNode2));
        
        File resultFile = new File("result");
        
        OutputStream out = new FileOutputStream(resultFile);
        
        compareDocumentService.compare(contentNode1, contentNode2, out);
        
        InputStream in = new FileInputStream(resultFile);
        
        assertTrue(resultFile.exists());
        
        String resultMimetype = mimetypeService.guessMimetype(resultFile.getName(), in);
        
        assertTrue(resultMimetype != null && resultMimetype.contains("pdf"));
	}
	
	@Test
	public void testCompareImage() throws IOException {
		
		NodeRef contentNode1 = transactionHelper.doInTransaction(() -> {
			return createNodeWithContent(testFolder, "sample1.jpg", "becpg/repo/document/sample1.jpg");
		}, false, true);
		
		assertTrue(nodeService.exists(contentNode1));
		
		NodeRef contentNode2 = transactionHelper.doInTransaction(() -> {
			return createNodeWithContent(testFolder, "sample2.jpg", "becpg/repo/document/sample2.jpg");
		}, false, true);
		
        assertTrue(nodeService.exists(contentNode2));
        
        File resultFile = new File("result");
        
        OutputStream out = new FileOutputStream(resultFile);
        
        compareDocumentService.compare(contentNode1, contentNode2, out);
        
        InputStream in = new FileInputStream(resultFile);
        
        assertTrue(resultFile.exists());
        
        String resultMimetype = mimetypeService.guessMimetype(resultFile.getName(), in);
        
        assertTrue(resultMimetype != null && resultMimetype.contains("image"));
	}
}
