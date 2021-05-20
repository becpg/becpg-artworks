package fr.becpg.artworks;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.becpg.artworks.test.utils.TestWebscriptExecuters;
import fr.becpg.artworks.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.artworks.test.utils.TestWebscriptExecuters.Response;

@RunWith(AlfrescoTestRunner.class)
public class CompareDocumentWebscriptIT extends RepoBaseTest {
	
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
        
		String url = String.format("/becpg/document/compare/" + contentNode1.toString().replace("://", "/") + "/compare?entities=" + contentNode2.toString());

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		
		assertNotNull(response);
		
		assertTrue(response.getContentType().contains("pdf"));
		
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
        
		String url = String.format("/becpg/document/compare/" + contentNode1.toString().replace("://", "/") + "/compare?entities=" + contentNode2.toString());

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		
		assertNotNull(response);
		
		assertTrue(response.getContentType().contains("image"));
		
	}

}
