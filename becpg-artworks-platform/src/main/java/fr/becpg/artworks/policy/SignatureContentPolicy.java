package fr.becpg.artworks.policy;

import java.io.Serializable;
import java.util.List;

import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.artworks.signature.model.SignatureModel;
import fr.becpg.artworks.signature.model.SignatureStatus;

/**
 * <p>SignatureContentPolicy class.</p>
 *
 * @author matthieu
 */
public class SignatureContentPolicy implements ContentServicePolicies.OnContentUpdatePolicy {

	private PolicyComponent policyComponent;
	
	private NodeService nodeService;

	/**
	 * <p>Setter for the field <code>policyComponent</code>.</p>
	 *
	 * @param policyComponent a {@link org.alfresco.repo.policy.PolicyComponent} object
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	
	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * <p>init.</p>
	 */
	public void init() {
		this.policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME,
				SignatureModel.ASPECT_SIGNATURE,
				new JavaBehaviour(this, "onContentUpdate", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
	}

	/** {@inheritDoc} */
	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
		
		if (nodeService.exists(nodeRef) && nodeService.hasAspect(nodeRef, SignatureModel.ASPECT_SIGNATURE)) {
			Serializable signatureStatus = nodeService.getProperty(nodeRef, SignatureModel.PROP_STATUS);
			
			if (!SignatureStatus.ReadyToSign.toString().equals(signatureStatus)) {
				List<AssociationRef> preparedRecipients = nodeService.getTargetAssocs(nodeRef, SignatureModel.ASSOC_PREPARED_RECIPIENTS);
				
				for (AssociationRef preparedRecipient : preparedRecipients) {
					nodeService.removeAssociation(nodeRef, preparedRecipient.getTargetRef(), SignatureModel.ASSOC_PREPARED_RECIPIENTS);
				}
				
				nodeService.setProperty(nodeRef, SignatureModel.PROP_STATUS, SignatureStatus.Initialized);
				nodeService.removeProperty(nodeRef, SignatureModel.PROP_RECIPIENTS_DATA);
				nodeService.removeProperty(nodeRef, SignatureModel.PROP_VALIDATION_DATE);
				
				List<AssociationRef> validator = nodeService.getTargetAssocs(nodeRef, SignatureModel.ASSOC_VALIDATOR);
				
				for (AssociationRef preparedRecipient : validator) {
					nodeService.removeAssociation(nodeRef, preparedRecipient.getTargetRef(), SignatureModel.ASSOC_VALIDATOR);
				}
			}
		}
	}

}
