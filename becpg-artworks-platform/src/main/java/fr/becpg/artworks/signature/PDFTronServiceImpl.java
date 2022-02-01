package fr.becpg.artworks.signature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;

import com.pdftron.pdf.Action;
import com.pdftron.pdf.DigitalSignatureField;
import com.pdftron.pdf.Highlights;
import com.pdftron.pdf.Image;
import com.pdftron.pdf.Rect;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.Page;
import com.pdftron.pdf.TextSearch;
import com.pdftron.pdf.TextSearchResult;
import com.pdftron.pdf.annots.SignatureWidget;
import com.pdftron.pdf.annots.TextWidget;
import com.pdftron.sdf.SDFDoc;

import fr.becpg.artworks.signature.model.SignatureModel;

/**
 * 
 * @author matthieu
 *
 */
public class PDFTronServiceImpl implements SignatureService{
	
	private String annotationViewerLicenseKey;
	//TODO use alfresco keystore instead
	private String signatureCertificatKeyPath;
	private String signatureCertificatKey;
	private String signatureLocationInfo;
	private String signatureReasonInfo;
	private String signatureContactInfo;
	
	private String signatureAnnotationFieldName;
	
	@Override
	public String checkoutDocument(NodeRef nodeRef) {
		return prepareForSignature(nodeRef, false);
	}
	
	@Override
	public void checkinDocument(NodeRef nodeRef) {
		//TODO sign document and checkin
		   	
//		   PDFDoc doc = new PDFDoc(docpath);
//		   Page page1 = doc.getPage(1);
//	
//		   // Create a text field that we can lock using the field permissions feature.
//		   TextWidget annot1 = TextWidget.create(doc, new Rect(50, 550, 350, 600), "asdf_test_field");
//		   page1.annotPushBack(annot1);
//	
//		   /* Create a new signature form field in the PDFDoc. The name argument is optional; leaving it empty causes it to be auto-generated. However, you may need the name for later. Acrobat doesn't show digsigfield in side panel if it's without a widget. Using a Rect with 0 width and 0 height, or setting the NoPrint/Invisible flags makes it invisible. */
//		   DigitalSignatureField certificationSigField = doc.createDigitalSignatureField(signatureAnnotationFieldName);
//		   SignatureWidget widgetAnnot = SignatureWidget.create(doc, new Rect(0, 100, 200, 150), certificationSigField);
//		   page1.annotPushBack(widgetAnnot);
//	
//		   // (OPTIONAL) Add an appearance to the signature field.
//		  // Image img = Image.create(doc, appearance_image_path);
//		   //widgetAnnot.createSignatureAppearance(img);
//	
//		   // Prepare the document locking permission level to be applied upon document certification.
//		   certificationSigField.setDocumentPermissions(DigitalSignatureField.DocumentPermissions.e_annotating_formfilling_signing_allowed);
//	
//		   // Prepare to lock the text field that we created earlier.
//		   String[] fields_to_lock = {"asdf_test_field"};
//		   certificationSigField.setFieldPermissions(DigitalSignatureField.FieldPermissions.e_include, fields_to_lock);
//	
//		   certificationSigField.certifyOnNextSave(signatureCertificatKeyPath, signatureCertificatKey);
//	
//		   // (OPTIONAL) Add more information to the signature dictionary.
//		   certificationSigField.setLocation(signatureLocationInfo);
//		   certificationSigField.setReason(signatureReasonInfo);
//		   certificationSigField.setContactInfo(signatureContactInfo);
//	
//		   // Save the PDFDoc. Once the method below is called, PDFNet will also sign the document using the information provided.
//		   doc.save(outpath, SDFDoc.SaveMode.NO_FLAGS, null);
		
	}
	
	@Override
	public void cancelDocument(NodeRef nodeRef) {
		//TODO Cancel checkout
		
	}
	
	@Override
	public String getDocumentView(NodeRef nodeRef, String userId, String returnUrl) {
		return null;
	}
	
	@Override
	public String prepareForSignature(NodeRef nodeRef, boolean notifyByMail) {
		
		//Checkout document 
		//Find signature name and add signature annotation
		//Lock document for signature
		//Return the annotation viewer url of the checkout document
		
		return "/artworks-viewer?nodeRef="+nodeRef.toString()+"&mode=sign";
		
//		PDFDoc doc = new PDFDoc(filename);
//		TextSearch txt_search = new TextSearch();
//		int mode = TextSearch.e_whole_word | TextSearch.e_page_stop;
//		String pattern = "";
//
//		//use regular expression to find credit card number
//		mode |= TextSearch.e_reg_expression | TextSearch.e_highlight;
//		txt_search.setMode(mode);
//		String new_pattern = "\\d{4}-\\d{4}-\\d{4}-\\d{4}"; //or "(\\d{4}-){3}\\d{4}"
//		txt_search.setPattern(new_pattern);
//
//		//call Begin() method to initialize the text search.
//		txt_search.begin(doc, pattern, mode, -1, -1);
//		TextSearchResult result = txt_search.run();
//
//		if (result.getCode() == TextSearchResult.e_found) {
//		  //add a link annotation based on the location of the found instance
//		  Highlights hlts = result.getHighlights();
//		  hlts.begin(doc);
//		  while (hlts.hasNext()) {
//		    Page cur_page = doc.getPage(hlts.getCurrentPageNumber());
//		    double[] q = hlts.getCurrentQuads();
//		    int quad_count = q.length / 8;
//		    for (int i = 0; i < quad_count; ++i) {
//		      //assume each quad is an axis-aligned rectangle
//		      int offset = 8 * i;
//		      double x1 = Math.min(Math.min(Math.min(q[offset + 0], q[offset + 2]), q[offset + 4]), q[offset + 6]);
//		      double x2 = Math.max(Math.max(Math.max(q[offset + 0], q[offset + 2]), q[offset + 4]), q[offset + 6]);
//		      double y1 = Math.min(Math.min(Math.min(q[offset + 1], q[offset + 3]), q[offset + 5]), q[offset + 7]);
//		      double y2 = Math.max(Math.max(Math.max(q[offset + 1], q[offset + 3]), q[offset + 5]), q[offset + 7]);
//		      annots.Link hyper_link = annots.Link.create(doc, new Rect(x1, y1, x2, y2), Action.createURI(doc, "http://www.pdftron.com"));
//		      cur_page.annotPushBack(hyper_link);
//		    }
//		    hlts.next();
//		  }
//		}
//		return null;
	}
	
	
	


}
