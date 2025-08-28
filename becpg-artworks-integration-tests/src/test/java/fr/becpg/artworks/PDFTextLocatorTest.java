package fr.becpg.artworks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.artworks.signature.PDFTextLocator;

public class PDFTextLocatorTest {

    @Test
    public void testLocation() throws IOException {

        ClassPathResource resource = new ClassPathResource("becpg/repo/document/sample1.pdf");

        // load InputStream into PDFBox (PDFBox 3.x does not have PDDocument.load(InputStream))
        try (InputStream is = resource.getInputStream();
             PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(is))) {

            float[] coordinates = PDFTextLocator.getCoordinates(document, "Exemple", 0);

            assertNotNull(coordinates);

            assertEquals(70.86, coordinates[0], 0.001);
            assertEquals(117.5112, coordinates[1], 0.001);
            assertEquals(752.05426, coordinates[2], 0.001);
            assertEquals(760.0403, coordinates[3], 0.001);

            coordinates = PDFTextLocator.getCoordinates(document, "fichier", 0);

            assertNotNull(coordinates);
            assertEquals(120.868805, coordinates[0], 0.001);
            assertEquals(152.84401, coordinates[1], 0.001);
            assertEquals(752.05426, coordinates[2], 0.001);
            assertEquals(760.0403, coordinates[3], 0.001);

            coordinates = PDFTextLocator.getCoordinates(document, "PDF", 0);

            assertNotNull(coordinates);

            assertEquals(156.2016, coordinates[0], 0.001);
            assertEquals(180.192, coordinates[1], 0.001);
            assertEquals(752.05426, coordinates[2], 0.001);
            assertEquals(760.0403, coordinates[3], 0.001);

            coordinates = PDFTextLocator.getCoordinates(document, "TEST", 0);

            assertNotNull(coordinates);

            assertEquals(-1f, coordinates[0], 0);
            assertEquals(-1f, coordinates[1], 0);
            assertEquals(843.0, coordinates[2], 0);
            assertEquals(843.0, coordinates[3], 0);
        }
    }
}
