package fr.becpg.artworks.compare.pdfcompare;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

public class DiffImage {

    /*package*/ static final int MARKER_RGB = color(230, 0, 230);
    private final ImageWithDimension expectedImage;
    private final ImageWithDimension actualImage;
    private final int page;
    private final Exclusions exclusions;
    private DataBuffer expectedBuffer;
    private DataBuffer actualBuffer;
    private int expectedImageWidth;
    private int expectedImageHeight;
    private int actualImageWidth;
    private int actualImageHeight;
    private int resultImageWidth;
    private int resultImageHeight;
    private boolean differencesFound = false;
    private boolean differenceInExclusion = false;
    private BufferedImage resultImage;
    private int diffAreaX1, diffAreaX2;
    private final ResultCollector compareResult;

    public DiffImage(final ImageWithDimension expectedImage, final ImageWithDimension actualImage, final int page,
            final Exclusions exclusions, final ResultCollector compareResult) {
        this.expectedImage = expectedImage;
        this.actualImage = actualImage;
        this.page = page;
        this.exclusions = exclusions;
        this.compareResult = compareResult;
    }

    public boolean differs() {
        return differencesFound;
    }

    public boolean differenceInExclusion() {
        return differenceInExclusion;
    }

    public BufferedImage getImage() {
        return resultImage;
    }

    public void diffImages() {
        BufferedImage expectBuffImage = this.expectedImage.bufferedImage;
        BufferedImage actualBuffImage = this.actualImage.bufferedImage;
        expectedBuffer = expectBuffImage.getRaster().getDataBuffer();
        actualBuffer = actualBuffImage.getRaster().getDataBuffer();

        expectedImageWidth = expectBuffImage.getWidth();
        expectedImageHeight = expectBuffImage.getHeight();
        actualImageWidth = actualBuffImage.getWidth();
        actualImageHeight = actualBuffImage.getHeight();

        resultImageWidth = Math.max(expectedImageWidth, actualImageWidth);
        resultImageHeight = Math.max(expectedImageHeight, actualImageHeight);
        resultImage = new BufferedImage(resultImageWidth, resultImageHeight, actualBuffImage.getType());
        DataBuffer resultBuffer = resultImage.getRaster().getDataBuffer();

        int expectedElement;
        int actualElement;
        final PageExclusions pageExclusions = exclusions.forPage(page + 1);

        for (int y = 0; y < resultImageHeight; y++) {
            final int expectedLineOffset = y * expectedImageWidth;
            final int actualLineOffset = y * actualImageWidth;
            final int resultLineOffset = y * resultImageWidth;
            for (int x = 0; x < resultImageWidth; x++) {
                expectedElement = getExpectedElement(x, y, expectedLineOffset);
                actualElement = getActualElement(x, y, actualLineOffset);
                int element = getElement(expectedElement, actualElement);
                if (pageExclusions.contains(x, y)) {
                    element = ImageTools.fadeExclusion(element);
                    if (expectedElement != actualElement) {
                        differenceInExclusion = true;
                    }
                } else {
                    if (expectedElement != actualElement) {
                        extendDiffArea(y, x);
                        differencesFound = true;
                        mark(resultBuffer, x, y, resultImageWidth, MARKER_RGB);
                    }
                }
                resultBuffer.setElem(x + resultLineOffset, element);
            }
        }
        final float maxWidth = Math.max(expectedImage.width, actualImage.width);
        final float maxHeight = Math.max(expectedImage.height, actualImage.height);
        compareResult.addPage(differencesFound, differenceInExclusion, page, expectedImage, actualImage, new ImageWithDimension(resultImage, maxWidth, maxHeight));
    }

    private void extendDiffArea(final int y, final int x) {
        if (!differencesFound) {
            diffAreaX1 = x;
        }
        diffAreaX1 = Math.min(diffAreaX1, x);
        diffAreaX2 = Math.max(diffAreaX2, x);
    }

    private int getElement(final int expectedElement, final int actualElement) {
        if (expectedElement != actualElement) {
            int expectedDarkness = calcCombinedIntensity(expectedElement);
            int actualDarkness = calcCombinedIntensity(actualElement);
            if (expectedDarkness > actualDarkness) {
                return color(levelIntensity(expectedDarkness, 210), 0, 0);
            } else {
                return color(0, levelIntensity(actualDarkness, 180), 0);
            }
        } else {
            return ImageTools.fadeElement(expectedElement);
        }
    }

    private int getExpectedElement(final int x, final int y, final int expectedLineOffset) {
        if (x < expectedImageWidth && y < expectedImageHeight) {
            return expectedBuffer.getElem(x + expectedLineOffset);
        }
        return 0;
    }

    private int getActualElement(final int x, final int y, final int actualLineOffset) {
        if (x < actualImageWidth && y < actualImageHeight) {
            return actualBuffer.getElem(x + actualLineOffset);
        }
        return 0;
    }

    /**
     * Levels the color intensity to at least 50 and at most maxIntensity.
     *
     * @param darkness     color component to level
     * @param maxIntensity highest possible intensity cut off
     * @return A value that is at least 50 and at most maxIntensity
     */
    private static int levelIntensity(final int darkness, final int maxIntensity) {
        return Math.min(maxIntensity, Math.max(50, darkness));
    }

    /**
     * Calculate the combined intensity of a pixel and normalizes it to a value of at most 255.
     *
     * @param element
     * @return
     */
    private static int calcCombinedIntensity(final int element) {
        final Color color = new Color(element);
        return Math.min(255, (color.getRed() + color.getGreen() + color.getRed()) / 3);
    }

    private static void mark(final DataBuffer image, final int x, final int y, final int imageWidth, final int markerRGB) {
        final int yOffset = y * imageWidth;
        for (int i = 0; i < PdfComparator.MARKER_WIDTH; i++) {
            image.setElem(x + i * imageWidth, markerRGB);
            image.setElem(i + yOffset, markerRGB);
        }
    }

    public static int color(final int r, final int g, final int b) {
        return new Color(r, g, b).getRGB();
    }

    @Override
    public String toString() {
        return "DiffImage{" +
                "page=" + page +
                '}';
    }
}
