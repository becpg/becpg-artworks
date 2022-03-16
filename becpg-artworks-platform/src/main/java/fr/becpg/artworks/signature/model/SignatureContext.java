package fr.becpg.artworks.signature.model;

import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

public class SignatureContext {

	private String signaturePage;
	
	private String signatureDimensions;
	
	private String initialsDimensions;
	
	private String signatureAnchorInfo;
	
	private String initialsAnchorInfo;
	
	private List<NodeRef> nodeRecipients;
	
	private List<NodeRef> recipients;
	
	public String getSignaturePage() {
		return signaturePage;
	}

	public void setSignaturePage(String signaturePage) {
		this.signaturePage = signaturePage;
	}

	public String getSignatureDimensions() {
		return signatureDimensions;
	}
	
	public void setSignatureDimensions(String signatureDimensions) {
		this.signatureDimensions = signatureDimensions;
	}

	public String getInitialsDimensions() {
		return initialsDimensions;
	}

	public void setInitialsDimensions(String initialsDimensions) {
		this.initialsDimensions = initialsDimensions;
	}

	public String getSignatureAnchorInfo() {
		return signatureAnchorInfo;
	}

	public void setSignatureAnchorInfo(String signatureAnchorInfo) {
		this.signatureAnchorInfo = signatureAnchorInfo;
	}

	public String getInitialsAnchorInfo() {
		return initialsAnchorInfo;
	}

	public void setInitialsAnchorInfo(String initialsAnchorInfo) {
		this.initialsAnchorInfo = initialsAnchorInfo;
	}

	public List<NodeRef> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<NodeRef> recipients) {
		this.recipients = recipients;
	}
	
	public List<NodeRef> getNodeRecipients() {
		return nodeRecipients;
	}
	
	public void setNodeRecipients(List<NodeRef> nodeRecipients) {
		this.nodeRecipients = nodeRecipients;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(signaturePage, signatureDimensions, initialsDimensions, signatureAnchorInfo, initialsAnchorInfo, recipients);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		SignatureContext other = (SignatureContext) obj;
		return Objects.equals(signaturePage, other.signaturePage)
				&& Objects.equals(signatureDimensions, other.signatureDimensions)
				&& Objects.equals(initialsDimensions, other.initialsDimensions)
				&& Objects.equals(signatureAnchorInfo, other.signatureAnchorInfo)
				&& Objects.equals(initialsAnchorInfo, other.initialsAnchorInfo)
				&& Objects.equals(recipients, other.recipients);
	}

	@Override
	public String toString() {
		return "SignatureContext [signaturePage=" + signaturePage + ", signatureDimensions=" + signatureDimensions
				+ ", initialsDimensions=" + initialsDimensions + ", signatureAnchorInfo=" + signatureAnchorInfo
				+ ", initialsAnchorInfo=" + initialsAnchorInfo + ", recipients=" + recipients + "]";
	}

}
