package fr.becpg.artworks.compare.pdfcompare;

public interface ResultCollector {

    void addPage(boolean hasDifferences, boolean hasDifferenceInExclusion, int pageIndex,
            ImageWithDimension expectedImage, ImageWithDimension actualImage, ImageWithDimension diffImage);

    void noPagesFound();

    default void done() {}
}
