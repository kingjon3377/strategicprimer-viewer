package lovelace.util;

import java.util.Iterator;

/**
 * A wrapper around an {@link Iterator} to let it be used in for-each loops. XML
 * parsing in particular always seems to hand me an iterator.
 *
 * TODO: Add stream()
 */
public final class IteratorWrapper<Element> implements Iterable<Element> {
    private final Iterator<Element> wrapped;

    public IteratorWrapper(final Iterator<Element> wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Iterator<Element> iterator() {
        return wrapped;
    }
}
