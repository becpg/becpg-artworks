package fr.becpg.artworks.compare.pdfcompare;

public class Environment {

    private static final int OVERALL_TIMEOUT = 15;
	private static final boolean USE_PARALLEL_PROCESSING = true;
	private static final int MAX_IMAGE_SIZE = 100000;
	private static final int SWAP_CACHE_SIZE = 100;
	private static final int MERGE_CACHE_SIZE = 100;
	private static final int NUMBER_OF_IMAGES_TO_CACHE = 30;

	private Environment() {}


    public static int getNrOfImagesToCache() {
        return NUMBER_OF_IMAGES_TO_CACHE;
    }

    public static int getMergeCacheSize() {
        return MERGE_CACHE_SIZE;
    }

    public static int getSwapCacheSize() {
        return SWAP_CACHE_SIZE;
    }

    public static int getDocumentCacheSize() {
        return 200 / 2;
    }

    public static int getMaxImageSize() {
        return MAX_IMAGE_SIZE;
    }

    public static int getOverallTimeout() {
        return OVERALL_TIMEOUT;
    }

    public static boolean useParallelProcessing() {
        return USE_PARALLEL_PROCESSING;
    }
}
