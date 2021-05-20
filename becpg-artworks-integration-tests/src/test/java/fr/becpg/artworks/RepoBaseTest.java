package fr.becpg.artworks;

import java.io.IOException;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.springframework.core.io.ClassPathResource;

public abstract class RepoBaseTest extends AbstractAlfrescoIT {

	protected NodeService nodeService;
	protected NamespaceService namespaceService;
	protected SearchService searchService;
	protected ContentService contentService;
	protected RetryingTransactionHelper transactionHelper;
	protected NodeRef testFolder;
	protected MimetypeService mimetypeService;

	@Rule
	public TestName name = new TestName();

	@Before
	public void initialize() {
		
		AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

		nodeService = getServiceRegistry().getNodeService();
		
		namespaceService = getServiceRegistry().getNamespaceService();
		
		searchService = getServiceRegistry().getSearchService();
		
		contentService = getServiceRegistry().getContentService();
		
		transactionHelper = getServiceRegistry().getRetryingTransactionHelper();
		
		mimetypeService = getServiceRegistry().getMimetypeService();
		
		NodeRef parentFolder = transactionHelper.doInTransaction(() -> {
			
			List<NodeRef> refs = searchService.selectNodes(nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE), "/app:company_home", null, namespaceService, false);
			
			NodeRef jUnitFolder = nodeService.getChildByName(refs.get(0), ContentModel.ASSOC_CONTAINS, "JUnit tests");
			
			if (jUnitFolder == null) {
				jUnitFolder = nodeService.createNode(refs.get(0), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_FOLDER).getChildRef();
				nodeService.setProperty(jUnitFolder, ContentModel.PROP_NAME, "JUnit tests");
			}
			
			NodeRef folder = nodeService.getChildByName(jUnitFolder, ContentModel.ASSOC_CONTAINS, getClass().getSimpleName());
			
			if (folder == null) {
				folder = nodeService.createNode(jUnitFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_FOLDER).getChildRef();
				nodeService.setProperty(folder, ContentModel.PROP_NAME, getClass().getSimpleName());
			}
			
			return folder;
			
		}, false, true);
		
		testFolder = transactionHelper.doInTransaction(() -> {
			NodeRef node = nodeService.getChildByName(parentFolder, ContentModel.ASSOC_CONTAINS, name.getMethodName());
			
			if (node != null) {
				nodeService.deleteNode(node);
			}
			
			node = nodeService.createNode(parentFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_FOLDER).getChildRef();
			nodeService.setProperty(node, ContentModel.PROP_NAME, name.getMethodName());
			
			return node;
		}, false, true);
	}
	
	protected NodeRef createNodeWithContent(NodeRef parent, String name, String resourceLocation) throws IOException {
		NodeRef contentNode = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT).getChildRef();
       
        nodeService.setProperty(contentNode, ContentModel.PROP_NAME, name);
        
        ClassPathResource resource = new ClassPathResource(resourceLocation);
        
		ContentWriter contentWriter = contentService.getWriter(contentNode, ContentModel.PROP_CONTENT, true);
		contentWriter.setEncoding("UTF-8");
		contentWriter.setMimetype(getServiceRegistry().getMimetypeService().guessMimetype(name, resource.getInputStream()));
		
		contentWriter.putContent(resource.getInputStream());
		
		return contentNode;
	}
}
