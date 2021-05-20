package fr.becpg.artworks.compare.pdfcompare;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class Utilities {

    public static MemoryUsageSetting getMemorySettings(final int bytes) throws IOException {
        return MemoryUsageSetting.setupMixed(bytes).setTempDir(FileUtils.createTempDir("PdfBox").toFile());
    }

    static class NamedThreadFactory implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NamedThreadFactory(final String name) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = name + "-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement());
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    public static ExecutorService blockingExecutor(final String name, int threads, int queueCapacity) {
        if (Environment.useParallelProcessing()) {
            return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MINUTES,
                    new LinkedBlockingQueue<>(queueCapacity), new NamedThreadFactory(name), new BlockingHandler());
        } else {
            return new InThreadExecutorService();
        }
    }

    public static void shutdownAndAwaitTermination(final ExecutorService executor, final String executorName) {
        if (executor != null) {
            executor.shutdown();
            try {
                final int timeout = 15;
                final TimeUnit unit = TimeUnit.MINUTES;
                if (!executor.awaitTermination(timeout, unit)) {
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void await(final CountDownLatch latch, final String latchName) {
        try {
            final int timeout = Environment.getOverallTimeout();
            final TimeUnit unit = TimeUnit.MINUTES;
            if (!latch.await(timeout, unit)) {
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static int getNumberOfPages(final Path document) throws IOException {
        try (InputStream documentIS = Files.newInputStream(document)) {
            return getNumberOfPages(documentIS);
        }
    }

    private static int getNumberOfPages(final InputStream documentIS) throws IOException {
        try (PDDocument pdDocument = PDDocument.load(documentIS, Utilities.getMemorySettings(Environment.getDocumentCacheSize()))) {
            return pdDocument.getNumberOfPages();
        }
    }

    public static ImageWithDimension renderPage(final Path document, final int page) throws IOException {
        try (InputStream documentIS = Files.newInputStream(document)) {
            return renderPage(documentIS, page);
        }
    }

    public static ImageWithDimension renderPage(final InputStream documentIS, final int page) throws IOException {
        try (PDDocument pdDocument = PDDocument.load(documentIS, Utilities.getMemorySettings(Environment.getDocumentCacheSize()))) {
            if (page >= pdDocument.getNumberOfPages()) {
                throw new IllegalArgumentException("Page out of range. Last page is: " + pdDocument.getNumberOfPages());
            }
            pdDocument.setResourceCache(new ResourceCacheWithLimitedImages());
            PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
            return PdfComparator.renderPageAsImage(pdDocument, pdfRenderer, page);
        }
    }
}
