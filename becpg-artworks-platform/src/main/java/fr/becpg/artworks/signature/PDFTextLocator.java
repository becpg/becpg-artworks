package fr.becpg.artworks.signature;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class PDFTextLocator extends PDFTextStripper {

	public PDFTextLocator() throws IOException {
		super();
	}

	private String keyString;
	private float x1 = -1;
	private float x2 = -1;
	private float y1 = -1;
	private float y2 = -1;

	public static float[] getCoordinates(PDDocument document, String phrase, int page) throws IOException {
		PDFTextLocator stripper = new PDFTextLocator();
		stripper.keyString = phrase;
		stripper.setSortByPosition(true);
		stripper.setStartPage(page + 1);
		stripper.setEndPage(page + 1);
		stripper.writeText(document, new OutputStreamWriter(new ByteArrayOutputStream()));
		stripper.y1 = document.getPage(page).getMediaBox().getHeight() - stripper.y1;
		stripper.y2 = document.getPage(page).getMediaBox().getHeight() - stripper.y2;
		return new float[] { stripper.x1, stripper.x2, stripper.y1, stripper.y2 };
	}

	@Override
	protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
		if (string.contains(keyString)) {
			TextPosition text = textPositions.get(0);
			if (x1 == -1) {
				x1 = textPositions.get(getXBegin(keyString, textPositions)).getX();
				x2 = textPositions.get(getXEnd(keyString, textPositions)).getEndX();
				y1 = text.getY() + text.getHeight();
				y2 = text.getY();
			}
		}
	}
	
	private int getXBegin(String keyString, List<TextPosition> textPositions) {
		
		char[] keyArray = keyString.toCharArray();
		
		int firstX = 0;
		
		for (int i = 0; i < textPositions.size(); i++) {
			int k = 0;
			for (int j = i; j < i + keyArray.length; j++) {
				if (textPositions.get(j).getUnicode().toCharArray()[0] == keyArray[k]) {
					k++;
					continue;
				}
				break;
			}
			
			if (k == keyArray.length) {
				firstX = i;
				break;
			}
		}
		
		return firstX;
	}
	
	private int getXEnd(String keyString, List<TextPosition> textPositions) {
		
		char[] keyArray = keyString.toCharArray();
		
		int lastX = 0;
		
		for (int i = 0; i < textPositions.size(); i++) {
			int k = 0;
			for (int j = i; j < i + keyArray.length; j++) {
				if (textPositions.get(j).getUnicode().toCharArray()[0] == keyArray[k]) {
					k++;
					continue;
				}
				break;
			}
			
			if (k == keyArray.length) {
				lastX = i + k - 1;
				break;
			}
		}
		
		return lastX;
	}
}
