package fr.becpg.artworks.web.scripts.document;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.artworks.compare.CompareDocumentService;

public class CompareDocumentWebScript extends AbstractWebScript {

	private static final String FILENAME = "; filename=\"";
	private static final String PARAM_STORE_TYPE = "store_type";
	private static final String PARAM_STORE_ID = "store_id";
	private static final String PARAM_ID = "id";
	private static final String PARAM_ENTITIES = "entities";

	private static final String HEADER_USER_AGENT = "User-Agent";

	@Autowired
	private CompareDocumentService compareDocumentService;

	@Autowired
	private MimetypeService mimetypeService;

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		NodeRef actualNode = null;

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

		// retrieve the actual node
		if (templateArgs != null) {
			String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
			if (storeType != null && storeId != null && nodeId != null) {
				actualNode = new NodeRef(storeType, storeId, nodeId);
			}
		}

		String entity = req.getParameter(PARAM_ENTITIES);
		
		NodeRef expectedNode = new NodeRef(entity);

		String fileName = compareDocumentService.compare(expectedNode, actualNode, res.getOutputStream());

		if (fileName != null) {
			res.setContentType(mimetypeService.guessMimetype(fileName));
			setAttachment(req, res, fileName);
		}

	}

	public static void setAttachment(WebScriptRequest req, WebScriptResponse res, String attachFileName) {
		String headerValue = "attachment";
		if (attachFileName != null && attachFileName.length() > 0) {
			if (req == null) {
				headerValue += "; filename*=UTF-8''" + URLEncoder.encode(attachFileName) + FILENAME + filterNameForQuotedString(attachFileName)
						+ "\"";
			} else {
				String userAgent = req.getHeader(HEADER_USER_AGENT);
				boolean isLegacy = (null != userAgent) && (userAgent.contains("MSIE 8") || userAgent.contains("MSIE 7"));
				if (isLegacy) {
					headerValue += FILENAME + URLEncoder.encode(attachFileName);
				} else {
					headerValue += FILENAME + filterNameForQuotedString(attachFileName) + "\"; filename*=UTF-8''"
							+ URLEncoder.encode(attachFileName);
				}
			}
		}

		// set header based on filename - will force a Save As from the browse if it doesn't recognize it
		// this is better than the default response of the browser trying to display the contents
		res.setHeader("Content-Disposition", headerValue);
	}

	private static String filterNameForQuotedString(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (isValidQuotedStringHeaderParamChar(c)) {
				sb.append(c);
			} else {
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	private static boolean isValidQuotedStringHeaderParamChar(char c) {
		// see RFC2616 section 2.2: 
		// qdtext         = <any TEXT except <">>
		// TEXT           = <any OCTET except CTLs, but including LWS>
		// CTL            = <any US-ASCII control character (octets 0 - 31) and DEL (127)>
		// A CRLF is allowed in the definition of TEXT only as part of a header field continuation.
		// Note: we dis-allow header field continuation
		return (c < 256) // message header param fields must be ISO-8859-1. Lower 256 codepoints of Unicode represent ISO-8859-1
				&& (c != 127) // CTL - see RFC2616 section 2.2
				&& (c != '"') // <">
				&& (c > 31); // CTL - see RFC2616 section 2.2
	}

}
