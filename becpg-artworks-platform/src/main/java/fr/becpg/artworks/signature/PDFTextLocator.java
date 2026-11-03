package fr.becpg.artworks.signature;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * <p>PDFTextLocator class.</p>
 *
 * @author matthieu
 */
public class PDFTextLocator extends PDFTextStripper {

	/**
	 * <p>Constructor for PDFTextLocator.</p>
	 *
	 * @throws java.io.IOException if any.
	 */
	public PDFTextLocator() throws IOException {
		super();
	}

	private String keyString;
	private float x1 = -1;
	private float x2 = -1;
	private float y1 = -1;
	private float y2 = -1;

	/**
	 * <p>getCoordinates.</p>
	 *
	 * @param document a {@link org.apache.pdfbox.pdmodel.PDDocument} object
	 * @param phrase a {@link java.lang.String} object
	 * @param page a int
	 * @return an array of {@link float} objects
	 * @throws java.io.IOException if any.
	 */
	public static float[] getCoordinates(PDDocument document, String phrase, int page) throws IOException {
		PDFTextLocator stripper = new PDFTextLocator();
		stripper.keyString = phrase;
		stripper.setSortByPosition(true);
		stripper.setStartPage(page + 1);
		stripper.setEndPage(page + 1);
		stripper.writeText(document, new OutputStreamWriter(new ByteArrayOutputStream()));
		if (stripper.y1 != -1) {
			stripper.y1 = document.getPage(page).getMediaBox().getUpperRightY() - stripper.y1;
			stripper.y2 = document.getPage(page).getMediaBox().getUpperRightY() - stripper.y2;
		} else {
			stripper.y1 = document.getPage(page).getMediaBox().getHeight() - stripper.y1;
			stripper.y2 = document.getPage(page).getMediaBox().getHeight() - stripper.y2;
		}
		return new float[] { stripper.x1, stripper.x2, stripper.y1, stripper.y2 };
	}

	/** {@inheritDoc} */
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
		return Math.max(indexOfKey(keyString, textPositions), 0);
	}

	private int getXEnd(String keyString, List<TextPosition> textPositions) {
		int keyStart = indexOfKey(keyString, textPositions);
		return keyStart < 0 ? 0 : keyStart + keyString.length() - 1;
	}

	/**
	 * Finds the first text position where the key string starts, one character per position.
	 *
	 * @param keyString the searched string
	 * @param textPositions the text positions of the line, in reading order
	 * @return the index of the first position of the key, or -1 when the key is not found
	 */
	static int indexOfKey(String keyString, List<TextPosition> textPositions) {
		char[] keyArray = keyString.toCharArray();
		for (int start = 0; start <= textPositions.size() - keyArray.length; start++) {
			if (matchesAt(keyArray, textPositions, start)) {
				return start;
			}
		}
		return -1;
	}

	private static boolean matchesAt(char[] keyArray, List<TextPosition> textPositions, int start) {
		for (int k = 0; k < keyArray.length; k++) {
			if (textPositions.get(start + k).getUnicode().charAt(0) != keyArray[k]) {
				return false;
			}
		}
		return true;
	}
}
