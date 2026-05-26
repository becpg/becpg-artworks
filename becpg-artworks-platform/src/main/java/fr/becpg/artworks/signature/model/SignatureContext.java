package fr.becpg.artworks.signature.model;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>SignatureContext class.</p>
 *
 * @author matthieu
 */
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
	
	/**
	 * <p>Setter for the field <code>disableInitials</code>.</p>
	 *
	 * @param disableInitials a boolean
	 */
	public void setDisableInitials(boolean disableInitials) {
		this.disableInitials = disableInitials;
	}
	
	/**
	 * <p>isDisableInitials.</p>
	 *
	 * @return a boolean
	 */
	public boolean isDisableInitials() {
		return disableInitials;
	}
	
	/**
	 * <p>Getter for the field <code>signaturePage</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getSignaturePage() {
		return signaturePage;
	}

	/**
	 * <p>Setter for the field <code>signaturePage</code>.</p>
	 *
	 * @param signaturePage a {@link java.lang.String} object
	 */
	public void setSignaturePage(String signaturePage) {
		this.signaturePage = signaturePage;
	}

	/**
	 * <p>Getter for the field <code>initialsAnchorKeyword</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getInitialsAnchorKeyword() {
		return initialsAnchorKeyword;
	}
	
	/**
	 * <p>Getter for the field <code>signatureAnchorKeyword</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getSignatureAnchorKeyword() {
		return signatureAnchorKeyword;
	}
	
	/**
	 * <p>Getter for the field <code>signatureAnchorXPosition</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getSignatureAnchorXPosition() {
		return signatureAnchorXPosition;
	}
	
	/**
	 * <p>Getter for the field <code>signatureAnchorYPosition</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getSignatureAnchorYPosition() {
		return signatureAnchorYPosition;
	}
	
	/**
	 * <p>Setter for the field <code>initialsAnchorKeyword</code>.</p>
	 *
	 * @param initialsAnchorKeyword a {@link java.lang.String} object
	 */
	public void setInitialsAnchorKeyword(String initialsAnchorKeyword) {
		this.initialsAnchorKeyword = initialsAnchorKeyword;
	}
	
	/**
	 * <p>Setter for the field <code>signatureAnchorKeyword</code>.</p>
	 *
	 * @param signatureAnchorKeyword a {@link java.lang.String} object
	 */
	public void setSignatureAnchorKeyword(String signatureAnchorKeyword) {
		this.signatureAnchorKeyword = signatureAnchorKeyword;
	}
	
	/**
	 * <p>Setter for the field <code>signatureAnchorXPosition</code>.</p>
	 *
	 * @param signatureAnchorXPosition a {@link java.lang.String} object
	 */
	public void setSignatureAnchorXPosition(String signatureAnchorXPosition) {
		this.signatureAnchorXPosition = signatureAnchorXPosition;
	}
	
	/**
	 * <p>Setter for the field <code>signatureAnchorYPosition</code>.</p>
	 *
	 * @param signatureAnchorYPosition a {@link java.lang.String} object
	 */
	public void setSignatureAnchorYPosition(String signatureAnchorYPosition) {
		this.signatureAnchorYPosition = signatureAnchorYPosition;
	}
	
	/**
	 * <p>Getter for the field <code>initialsAnchorXPosition</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getInitialsAnchorXPosition() {
		return initialsAnchorXPosition;
	}

	/**
	 * <p>Setter for the field <code>initialsAnchorXPosition</code>.</p>
	 *
	 * @param initialsAnchorXPosition a {@link java.lang.String} object
	 */
	public void setInitialsAnchorXPosition(String initialsAnchorXPosition) {
		this.initialsAnchorXPosition = initialsAnchorXPosition;
	}

	/**
	 * <p>Getter for the field <code>initialsAnchorYPosition</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getInitialsAnchorYPosition() {
		return initialsAnchorYPosition;
	}

	/**
	 * <p>Setter for the field <code>initialsAnchorYPosition</code>.</p>
	 *
	 * @param initialsAnchorYPosition a {@link java.lang.String} object
	 */
	public void setInitialsAnchorYPosition(String initialsAnchorYPosition) {
		this.initialsAnchorYPosition = initialsAnchorYPosition;
	}

	/**
	 * <p>Getter for the field <code>recipients</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<NodeRef> getRecipients() {
		return recipients;
	}

	/**
	 * <p>Setter for the field <code>recipients</code>.</p>
	 *
	 * @param recipients a {@link java.util.List} object
	 */
	public void setRecipients(List<NodeRef> recipients) {
		this.recipients = recipients;
	}
	
	/**
	 * <p>Getter for the field <code>nodeRecipients</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<NodeRef> getNodeRecipients() {
		return nodeRecipients;
	}
	
	/**
	 * <p>Setter for the field <code>nodeRecipients</code>.</p>
	 *
	 * @param nodeRecipients a {@link java.util.List} object
	 */
	public void setNodeRecipients(List<NodeRef> nodeRecipients) {
		this.nodeRecipients = nodeRecipients;
	}

	/**
	 * <p>Getter for the field <code>initialsDirection</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getInitialsDirection() {
		return initialsDirection;
	}

	/**
	 * <p>Setter for the field <code>initialsDirection</code>.</p>
	 *
	 * @param initialsDirection a {@link java.lang.String} object
	 */
	public void setInitialsDirection(String initialsDirection) {
		this.initialsDirection = initialsDirection;
	}

	/**
	 * <p>Getter for the field <code>signatureWidth</code>.</p>
	 *
	 * @return a int
	 */
	public int getSignatureWidth() {
		return signatureWidth;
	}

	/**
	 * <p>Setter for the field <code>signatureWidth</code>.</p>
	 *
	 * @param signatureWidth a int
	 */
	public void setSignatureWidth(int signatureWidth) {
		this.signatureWidth = signatureWidth;
	}

	/**
	 * <p>Getter for the field <code>signatureHeight</code>.</p>
	 *
	 * @return a int
	 */
	public int getSignatureHeight() {
		return signatureHeight;
	}

	/**
	 * <p>Setter for the field <code>signatureHeight</code>.</p>
	 *
	 * @param signatureHeight a int
	 */
	public void setSignatureHeight(int signatureHeight) {
		this.signatureHeight = signatureHeight;
	}

	/**
	 * <p>Getter for the field <code>signatureDirection</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getSignatureDirection() {
		return signatureDirection;
	}

	/**
	 * <p>Setter for the field <code>signatureDirection</code>.</p>
	 *
	 * @param signatureDirection a {@link java.lang.String} object
	 */
	public void setSignatureDirection(String signatureDirection) {
		this.signatureDirection = signatureDirection;
	}

	/**
	 * <p>Getter for the field <code>signatureGap</code>.</p>
	 *
	 * @return a int
	 */
	public int getSignatureGap() {
		return signatureGap;
	}

	/**
	 * <p>Setter for the field <code>signatureGap</code>.</p>
	 *
	 * @param signatureGap a int
	 */
	public void setSignatureGap(int signatureGap) {
		this.signatureGap = signatureGap;
	}

	/**
	 * <p>Getter for the field <code>signatureFromLeftRatio</code>.</p>
	 *
	 * @return a int
	 */
	public int getSignatureFromLeftRatio() {
		return signatureFromLeftRatio;
	}

	/**
	 * <p>Setter for the field <code>signatureFromLeftRatio</code>.</p>
	 *
	 * @param signatureFromLeftRatio a int
	 */
	public void setSignatureFromLeftRatio(int signatureFromLeftRatio) {
		this.signatureFromLeftRatio = signatureFromLeftRatio;
	}

	/**
	 * <p>Getter for the field <code>signatureFromBottomRatio</code>.</p>
	 *
	 * @return a int
	 */
	public int getSignatureFromBottomRatio() {
		return signatureFromBottomRatio;
	}

	/**
	 * <p>Setter for the field <code>signatureFromBottomRatio</code>.</p>
	 *
	 * @param signatureFromBottomRatio a int
	 */
	public void setSignatureFromBottomRatio(int signatureFromBottomRatio) {
		this.signatureFromBottomRatio = signatureFromBottomRatio;
	}

	/**
	 * <p>Getter for the field <code>initialsWidth</code>.</p>
	 *
	 * @return a int
	 */
	public int getInitialsWidth() {
		return initialsWidth;
	}

	/**
	 * <p>Setter for the field <code>initialsWidth</code>.</p>
	 *
	 * @param initialsWidth a int
	 */
	public void setInitialsWidth(int initialsWidth) {
		this.initialsWidth = initialsWidth;
	}

	/**
	 * <p>Getter for the field <code>initialsHeight</code>.</p>
	 *
	 * @return a int
	 */
	public int getInitialsHeight() {
		return initialsHeight;
	}

	/**
	 * <p>Setter for the field <code>initialsHeight</code>.</p>
	 *
	 * @param initialsHeight a int
	 */
	public void setInitialsHeight(int initialsHeight) {
		this.initialsHeight = initialsHeight;
	}

	/**
	 * <p>Getter for the field <code>initialsGap</code>.</p>
	 *
	 * @return a int
	 */
	public int getInitialsGap() {
		return initialsGap;
	}

	/**
	 * <p>Setter for the field <code>initialsGap</code>.</p>
	 *
	 * @param initialsGap a int
	 */
	public void setInitialsGap(int initialsGap) {
		this.initialsGap = initialsGap;
	}

	/**
	 * <p>Getter for the field <code>initialsFromLeftRatio</code>.</p>
	 *
	 * @return a int
	 */
	public int getInitialsFromLeftRatio() {
		return initialsFromLeftRatio;
	}

	/**
	 * <p>Setter for the field <code>initialsFromLeftRatio</code>.</p>
	 *
	 * @param initialsFromLeftRatio a int
	 */
	public void setInitialsFromLeftRatio(int initialsFromLeftRatio) {
		this.initialsFromLeftRatio = initialsFromLeftRatio;
	}

	/**
	 * <p>Getter for the field <code>initialsFromBottomRatio</code>.</p>
	 *
	 * @return a int
	 */
	public int getInitialsFromBottomRatio() {
		return initialsFromBottomRatio;
	}

	/**
	 * <p>Setter for the field <code>initialsFromBottomRatio</code>.</p>
	 *
	 * @param initialsFromBottomRatio a int
	 */
	public void setInitialsFromBottomRatio(int initialsFromBottomRatio) {
		this.initialsFromBottomRatio = initialsFromBottomRatio;
	}

	/** {@inheritDoc} */
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
