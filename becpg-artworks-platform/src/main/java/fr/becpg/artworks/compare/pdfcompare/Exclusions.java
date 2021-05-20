package fr.becpg.artworks.compare.pdfcompare;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Exclusions {

    private final Map<Integer, PageExclusions> exclusionsPerPage = new HashMap<>();
    private final PageExclusions exclusionsForAllPages = new PageExclusions();

    public Exclusions add(final Exclusion exclusion) {
        if (exclusion.page < 0) {
            exclusionsForAllPages.add(exclusion);
        } else {
            exclusionsPerPage.computeIfAbsent(exclusion.page, k -> new PageExclusions(exclusionsForAllPages)).add(exclusion);
        }
        return this;
    }

    public PageExclusions forPage(final int page) {
        return exclusionsPerPage.getOrDefault(page, exclusionsForAllPages);
    }

    public void forEach(final Consumer<Exclusion> exclusionConsumer) {
        exclusionsForAllPages.forEach(exclusionConsumer);
        exclusionsPerPage.values().forEach(pe -> pe.forEach(exclusionConsumer));
    }
}
