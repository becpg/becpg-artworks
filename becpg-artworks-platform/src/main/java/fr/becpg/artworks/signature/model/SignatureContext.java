package fr.becpg.artworks.signature.model;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public class SignatureContext {

	// ex: "0","3","last","last-1"
	private String signaturePage = "last";
	
	private int signatureWidth = 200;
	private int signatureHeight = 75;
	private String signatureDirection = "up";
	private int signatureGap = 60;
	private int signatureFromLeftRatio = 60;
	private int signatureFromBottomRatio = 10;
	
	private String signatureAnchorKeyword = null;
	private String signatureAnchorXPosition = null;
	private String signatureAnchorYPosition = null;
	
	private int initialsWidth = 100;
	private int initialsHeight = 35;
	private String initialsDirection = "up";
	private int initialsGap = 30;
	private int initialsFromLeftRatio = 75;
	private int initialsFromBottomRatio = 10;

	
	private String initialsAnchorKeyword = null;
	private String initialsAnchorXPosition = null;
	private String initialsAnchorYPosition = null;
	
	private List<NodeRef> nodeRecipients;
	
	private List<NodeRef> recipients;
	
	private boolean disableInitials = false;
	
	public void setDisableInitials(boolean disableInitials) {
		this.disableInitials = disableInitials;
	}
	
	public boolean isDisableInitials() {
		return disableInitials;
	}
	
	public String getSignaturePage() {
		return signaturePage;
	}

	public void setSignaturePage(String signaturePage) {
		this.signaturePage = signaturePage;
	}

	public String getInitialsAnchorKeyword() {
		return initialsAnchorKeyword;
	}
	
	public String getSignatureAnchorKeyword() {
		return signatureAnchorKeyword;
	}
	
	public String getSignatureAnchorXPosition() {
		return signatureAnchorXPosition;
	}
	
	public String getSignatureAnchorYPosition() {
		return signatureAnchorYPosition;
	}
	
	public void setInitialsAnchorKeyword(String initialsAnchorKeyword) {
		this.initialsAnchorKeyword = initialsAnchorKeyword;
	}
	
	public void setSignatureAnchorKeyword(String signatureAnchorKeyword) {
		this.signatureAnchorKeyword = signatureAnchorKeyword;
	}
	
	public void setSignatureAnchorXPosition(String signatureAnchorXPosition) {
		this.signatureAnchorXPosition = signatureAnchorXPosition;
	}
	
	public void setSignatureAnchorYPosition(String signatureAnchorYPosition) {
		this.signatureAnchorYPosition = signatureAnchorYPosition;
	}
	
	public String getInitialsAnchorXPosition() {
		return initialsAnchorXPosition;
	}

	public void setInitialsAnchorXPosition(String initialsAnchorXPosition) {
		this.initialsAnchorXPosition = initialsAnchorXPosition;
	}

	public String getInitialsAnchorYPosition() {
		return initialsAnchorYPosition;
	}

	public void setInitialsAnchorYPosition(String initialsAnchorYPosition) {
		this.initialsAnchorYPosition = initialsAnchorYPosition;
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

	public String getInitialsDirection() {
		return initialsDirection;
	}

	public void setInitialsDirection(String initialsDirection) {
		this.initialsDirection = initialsDirection;
	}

	public int getSignatureWidth() {
		return signatureWidth;
	}

	public void setSignatureWidth(int signatureWidth) {
		this.signatureWidth = signatureWidth;
	}

	public int getSignatureHeight() {
		return signatureHeight;
	}

	public void setSignatureHeight(int signatureHeight) {
		this.signatureHeight = signatureHeight;
	}

	public String getSignatureDirection() {
		return signatureDirection;
	}

	public void setSignatureDirection(String signatureDirection) {
		this.signatureDirection = signatureDirection;
	}

	public int getSignatureGap() {
		return signatureGap;
	}

	public void setSignatureGap(int signatureGap) {
		this.signatureGap = signatureGap;
	}

	public int getSignatureFromLeftRatio() {
		return signatureFromLeftRatio;
	}

	public void setSignatureFromLeftRatio(int signatureFromLeftRatio) {
		this.signatureFromLeftRatio = signatureFromLeftRatio;
	}

	public int getSignatureFromBottomRatio() {
		return signatureFromBottomRatio;
	}

	public void setSignatureFromBottomRatio(int signatureFromBottomRatio) {
		this.signatureFromBottomRatio = signatureFromBottomRatio;
	}

	public int getInitialsWidth() {
		return initialsWidth;
	}

	public void setInitialsWidth(int initialsWidth) {
		this.initialsWidth = initialsWidth;
	}

	public int getInitialsHeight() {
		return initialsHeight;
	}

	public void setInitialsHeight(int initialsHeight) {
		this.initialsHeight = initialsHeight;
	}

	public int getInitialsGap() {
		return initialsGap;
	}

	public void setInitialsGap(int initialsGap) {
		this.initialsGap = initialsGap;
	}

	public int getInitialsFromLeftRatio() {
		return initialsFromLeftRatio;
	}

	public void setInitialsFromLeftRatio(int initialsFromLeftRatio) {
		this.initialsFromLeftRatio = initialsFromLeftRatio;
	}

	public int getInitialsFromBottomRatio() {
		return initialsFromBottomRatio;
	}

	public void setInitialsFromBottomRatio(int initialsFromBottomRatio) {
		this.initialsFromBottomRatio = initialsFromBottomRatio;
	}

	@Override
	public String toString() {
		return "SignatureContext [signaturePage=" + signaturePage + ", signatureWidth=" + signatureWidth + ", signatureHeight=" + signatureHeight
				+ ", signatureDirection=" + signatureDirection + ", signatureGap=" + signatureGap + ", signatureFromLeftRatio="
				+ signatureFromLeftRatio + ", signatureFromBottomRatio=" + signatureFromBottomRatio + ", signatureAnchorKeyword="
				+ signatureAnchorKeyword + ", signatureAnchorXPosition=" + signatureAnchorXPosition + ", signatureAnchorYPosition="
				+ signatureAnchorYPosition + ", initialsWidth=" + initialsWidth + ", initialsHeight=" + initialsHeight + ", initialsDirection="
				+ initialsDirection + ", initialsGap=" + initialsGap + ", initialsFromLeftRatio=" + initialsFromLeftRatio
				+ ", initialsFromBottomRatio=" + initialsFromBottomRatio + ", initialsAnchorKeyword=" + initialsAnchorKeyword
				+ ", initialsAnchorXPosition=" + initialsAnchorXPosition + ", initialsAnchorYPosition=" + initialsAnchorYPosition
				+ ", nodeRecipients=" + nodeRecipients + ", recipients=" + recipients + ", disableInitials=" + disableInitials + "]";
	}

}
