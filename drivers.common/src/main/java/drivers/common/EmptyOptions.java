package drivers.common;

import java.util.Map;
import java.util.Iterator;
import java.util.Collections;

/**
 * A simple {@link SPOptions} implementation for drivers that don't accept options.
 */
public final class EmptyOptions implements SPOptions {
    private EmptyOptions() {
    }

    public static final EmptyOptions EMPTY_OPTIONS = new EmptyOptions();

    @Override
    public SPOptions copy() {
        return this;
    }

    @Override
    public String getArgument(final String option) {
        return "false";
    }

    @Override
    public boolean hasOption(final String option) {
        return false;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return Collections.emptyIterator();
    }
}
