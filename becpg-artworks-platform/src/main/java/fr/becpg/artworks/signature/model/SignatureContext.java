package fr.becpg.artworks.signature.model;

import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

public class SignatureContext {

	// ex: "0","3","last","last-1"
	private String signaturePage = "last";
	
	// width,height,direction(1=right,2=left,3=up,4=down),gap,fromLeftProportion,fromBottomProportion
	private String signatureDimensions = "100,50,3,60,75,10";
	
	// keyWord,xposition(0=left,1=middle,2=right),yposition(0=bottom,1=middle,2=top)
	private String signatureAnchorInfo = "signature,2,0";
	
	// width,height,direction(1=right,2=left,3=up,4=down),gap,fromLeftProportion,fromBottomProportion
	private String initialsDimensions = "50,25,3,30,75,10";
	
	// keyWord,xposition(0=left,1=middle,2=right),yposition(0=bottom,1=middle,2=top)
	private String initialsAnchorInfo = "Page,0,2";

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
