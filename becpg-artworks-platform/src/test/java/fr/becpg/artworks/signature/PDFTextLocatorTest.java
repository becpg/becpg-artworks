package fr.becpg.artworks.signature;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.junit.Test;

/**
 * Unit tests of the key search of {@link PDFTextLocator}.
 */
public class PDFTextLocatorTest {

	private static final String LINE = "Exemple fichier PDF";

	@Test
	public void findsKeyAtLineStart() {
		assertEquals(0, PDFTextLocator.indexOfKey("Exemple", positionsOf(LINE)));
	}

	@Test
	public void findsKeyAtLineEnd() {
		assertEquals(16, PDFTextLocator.indexOfKey("PDF", positionsOf(LINE)));
	}

	@Test
	public void findsFirstOccurrenceAfterPartialMatch() {
		assertEquals(3, PDFTextLocator.indexOfKey("ple", positionsOf("plaple ple")));
	}

	@Test
	public void returnsMinusOneWhenKeyIsMissing() {
		assertEquals(-1, PDFTextLocator.indexOfKey("TEST", positionsOf(LINE)));
	}

	@Test
	public void returnsMinusOneWhenKeyIsCutByLineEnd() {
		assertEquals(-1, PDFTextLocator.indexOfKey("PDFA", positionsOf(LINE)));
	}

	private static List<TextPosition> positionsOf(String text) {
		List<TextPosition> positions = new ArrayList<>(text.length());
		for (char character : text.toCharArray()) {
			positions.add(new TextPosition(0, 0, 0, new Matrix(), 0, 0, 0, 0, 0, String.valueOf(character),
					new int[] { character }, null, 0, 0));
		}
		return positions;
	}
}
