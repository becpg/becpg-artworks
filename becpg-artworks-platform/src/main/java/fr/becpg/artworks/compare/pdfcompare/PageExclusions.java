package fr.becpg.artworks.compare.pdfcompare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public class PageExclusions {

    private final Collection<Exclusion> exclusions = new ArrayList<>();
    private final PageExclusions delegate;

    public PageExclusions() {
        delegate = null;
    }

    public PageExclusions(final PageExclusions delegate) {
        this.delegate = delegate;
    }

    public void add(final Exclusion exclusion) {
        exclusions.add(exclusion);
    }

    public boolean contains(final int x, final int y) {
        for (Exclusion exclusion : exclusions) {
            if (exclusion.contains(x, y)) {
                return true;
            }
        }
        if (delegate != null) {
            return delegate.contains(x, y);
        }
        return false;
    }

    public void forEach(final Consumer<Exclusion> exclusionConsumer) {
        exclusions.forEach(exclusionConsumer);
    }
}
