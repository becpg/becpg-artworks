package fr.becpg.artworks;

import static org.junit.Assert.assertNotNull;

import org.alfresco.model.ContentModel;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.artworks.annotation.jscript.AnnotationScriptHelper;

@RunWith(value = AlfrescoTestRunner.class)
public class AnnotationServiceIT extends AbstractAlfrescoIT  {

	private Log logger = LogFactory.getLog(AnnotationServiceIT.class);

	private AnnotationScriptHelper annotationScriptHelper;


	@Before
	public void initialize() {
		ApplicationContext ctx = getApplicationContext();
        if (ctx != null) {
        	Object bean = ctx.getBean("annotationScriptHelper");
        	if (bean instanceof AnnotationScriptHelper) {
        		annotationScriptHelper = (AnnotationScriptHelper) bean;
        	}
        }
	}

    /**
     * Get the node reference for the /Company Home top folder in Alfresco.
     * Use the standard node locator service.
     *
     * @return the node reference for /Company Home
     */
    private NodeRef getCompanyHomeNodeRef() {
        return getServiceRegistry().getNodeLocatorService().getNode(CompanyHomeNodeLocator.NAME, null, null);
    }
	
	@Test
	public void testAnnotation() {
		
		getServiceRegistry().getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				NodeRef nodeRef = getServiceRegistry().getNodeService()
						.createNode(getCompanyHomeNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT)
						.getChildRef();
				
				getServiceRegistry().getNodeService().setProperty(nodeRef, ContentModel.PROP_NAME, "file.pdf");

				ContentWriter writer = getServiceRegistry().getContentService().getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
				if (writer != null) {

					ClassPathResource resource = new ClassPathResource("becpg/repo/document/file.pdf");

					writer.setMimetype("application/pdf");
					writer.putContent(resource.getInputStream());

					if (logger.isDebugEnabled()) {
						logger.debug("File successfully modified");
					}
				} else {
					logger.error("Cannot write node");
				}
								
				ScriptNode scriptNode = new ScriptNode(nodeRef,getServiceRegistry());
				String documentIdentifier = annotationScriptHelper.checkoutDocument(scriptNode);
				assertNotNull(documentIdentifier);
				String sessionUrl = annotationScriptHelper.getOrCreateRemoteView(scriptNode);
				assertNotNull(sessionUrl);
				annotationScriptHelper.checkinDocument(scriptNode);
				return null;
			}
		}, false, true);
	}

}
