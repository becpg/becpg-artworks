/*
 * Copyright 2016 Malte Finsterwalder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.becpg.artworks.compare.pdfcompare;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PdfComparator<T extends CompareResult> {

    public static final int DPI = 300;
    private static final int EXTRA_RGB = new Color(0, 160, 0).getRGB();
    private static final int MISSING_RGB = new Color(220, 0, 0).getRGB();
    public static final int MARKER_WIDTH = 20;
    private final Exclusions exclusions = new Exclusions();
    private InputStreamSupplier expectedStreamSupplier;
    private InputStreamSupplier actualStreamSupplier;
    private ExecutorService drawExecutor = Utilities.blockingExecutor("Draw", 1, 50);
    private ExecutorService parrallelDrawExecutor = Utilities.blockingExecutor("ParallelDraw", 2, 4);
    private ExecutorService diffExecutor = Utilities.blockingExecutor("Diff", 1, 2);
    private final T compareResult;
    private static final int TIMEOUT = 3;
    private static final TimeUnit UNIT = TimeUnit.MINUTES;

    private PdfComparator(T compareResult) {
        Objects.requireNonNull(compareResult, "compareResult is null");
        this.compareResult = compareResult;
    }

    @SuppressWarnings("unchecked")
	public PdfComparator(final File expectedFile, final File actualFile) {
        this(expectedFile, actualFile, (T) new CompareResult());
    }

    public PdfComparator(final File expectedFile, final File actualFile, final T compareResult) {
        this(compareResult);
        Objects.requireNonNull(expectedFile, "expectedFile is null");
        Objects.requireNonNull(actualFile, "actualFile is null");
        if (!expectedFile.equals(actualFile)) {
            this.expectedStreamSupplier = () -> new FileInputStream(expectedFile);
            this.actualStreamSupplier = () -> new FileInputStream(actualFile);
        }
    }

    public T compare() throws IOException {
        try {
            if (expectedStreamSupplier == null || actualStreamSupplier == null) {
                return compareResult;
            }
            try (final InputStream expectedStream = expectedStreamSupplier.get()) {
                try (final InputStream actualStream = actualStreamSupplier.get()) {
                    try (PDDocument expectedDocument = PDDocument
                            .load(expectedStream, Utilities.getMemorySettings(Environment.getDocumentCacheSize()))) {
                        expectedDocument.setResourceCache(new ResourceCacheWithLimitedImages());
                        PDFRenderer expectedPdfRenderer = new PDFRenderer(expectedDocument);
                        try (PDDocument actualDocument = PDDocument
                                .load(actualStream, Utilities.getMemorySettings(Environment.getDocumentCacheSize()))) {
                            actualDocument.setResourceCache(new ResourceCacheWithLimitedImages());
                            PDFRenderer actualPdfRenderer = new PDFRenderer(actualDocument);
                            final int minPageCount = Math.min(expectedDocument.getNumberOfPages(), actualDocument.getNumberOfPages());
                            CountDownLatch latch = new CountDownLatch(minPageCount);
                            for (int pageIndex = 0; pageIndex < minPageCount; pageIndex++) {
                                drawImage(latch, pageIndex, expectedDocument, actualDocument, expectedPdfRenderer, actualPdfRenderer);
                            }
                            Utilities.await(latch, "FullCompare");
                            Utilities.shutdownAndAwaitTermination(drawExecutor, "Draw");
                            Utilities.shutdownAndAwaitTermination(parrallelDrawExecutor, "Parallel Draw");
                            Utilities.shutdownAndAwaitTermination(diffExecutor, "Diff");
                            if (expectedDocument.getNumberOfPages() > minPageCount) {
                                addExtraPages(expectedDocument, expectedPdfRenderer, minPageCount, MISSING_RGB, true);
                            } else if (actualDocument.getNumberOfPages() > minPageCount) {
                                addExtraPages(actualDocument, actualPdfRenderer, minPageCount, EXTRA_RGB, false);
                            }
                        }
                    }
                } catch (NoSuchFileException ex) {
                    addSingleDocumentToResult(expectedStream, MISSING_RGB);
                }
            } catch (NoSuchFileException ex) {
                try (final InputStream actualStream = actualStreamSupplier.get()) {
                    addSingleDocumentToResult(actualStream, EXTRA_RGB);
                } catch (NoSuchFileException innerEx) {
                    compareResult.noPagesFound();
                }
            }
        } finally {
            compareResult.done();
        }
        return compareResult;
    }

    private void drawImage(final CountDownLatch latch, final int pageIndex,
            final PDDocument expectedDocument, final PDDocument actualDocument,
            final PDFRenderer expectedPdfRenderer, final PDFRenderer actualPdfRenderer) {
        drawExecutor.execute(() -> {
            try {
                final Future<ImageWithDimension> expectedImageFuture = parrallelDrawExecutor
                        .submit(() -> renderPageAsImage(expectedDocument, expectedPdfRenderer, pageIndex));
                final Future<ImageWithDimension> actualImageFuture = parrallelDrawExecutor
                        .submit(() -> renderPageAsImage(actualDocument, actualPdfRenderer, pageIndex));
                final ImageWithDimension expectedImage = getImage(expectedImageFuture, pageIndex, "expected document");
                final ImageWithDimension actualImage = getImage(actualImageFuture, pageIndex, "actual document");
                final DiffImage diffImage = new DiffImage(expectedImage, actualImage, pageIndex, exclusions, compareResult);
                diffExecutor.execute(() -> {
                    diffImage.diffImages();
                });
            } finally {
                latch.countDown();
            }
        });
    }

    private ImageWithDimension getImage(final Future<ImageWithDimension> imageFuture, final int pageIndex, final String type) {
        try {
            return imageFuture.get(TIMEOUT, UNIT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException e2) {
        }
        return null;
    }

    private void addSingleDocumentToResult(InputStream expectedPdfIS, int markerColor) throws IOException {
        try (PDDocument expectedDocument = PDDocument.load(expectedPdfIS)) {
            PDFRenderer expectedPdfRenderer = new PDFRenderer(expectedDocument);
            addExtraPages(expectedDocument, expectedPdfRenderer, 0, markerColor, true);
        }
    }

    private void addExtraPages(final PDDocument document, final PDFRenderer pdfRenderer, final int minPageCount,
            final int color, final boolean expected) throws IOException {
        for (int pageIndex = minPageCount; pageIndex < document.getNumberOfPages(); pageIndex++) {
            ImageWithDimension image = renderPageAsImage(document, pdfRenderer, pageIndex);
            final DataBuffer dataBuffer = image.bufferedImage.getRaster().getDataBuffer();
            for (int i = 0; i < image.bufferedImage.getWidth() * MARKER_WIDTH; i++) {
                dataBuffer.setElem(i, color);
            }
            for (int i = 0; i < image.bufferedImage.getHeight(); i++) {
                for (int j = 0; j < MARKER_WIDTH; j++) {
                    dataBuffer.setElem(i * image.bufferedImage.getWidth() + j, color);
                }
            }
            if (expected) {
                compareResult.addPage(true, false, pageIndex, image, blank(image), image);
            } else {
                compareResult.addPage(true, false, pageIndex, blank(image), image, image);
            }
        }
    }

    private static ImageWithDimension blank(final ImageWithDimension image) {
        return new ImageWithDimension(new BufferedImage(image.bufferedImage.getWidth(), image.bufferedImage.getHeight(), image.bufferedImage.getType()), image.width, image.height);
    }

    public static ImageWithDimension renderPageAsImage(final PDDocument document, final PDFRenderer expectedPdfRenderer, final int pageIndex)
            throws IOException {
        final BufferedImage bufferedImage = expectedPdfRenderer.renderImageWithDPI(pageIndex, DPI);
        final PDRectangle mediaBox = document.getPage(pageIndex).getMediaBox();
        return new ImageWithDimension(bufferedImage, mediaBox.getWidth(), mediaBox.getHeight());
    }

    @FunctionalInterface private interface InputStreamSupplier {

        InputStream get() throws IOException;
    }
}
