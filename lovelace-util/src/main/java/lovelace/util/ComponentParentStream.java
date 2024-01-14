package lovelace.util;

import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A stream of a component's parent, its parent, and so on until a component's parent is null.
 */
public final class ComponentParentStream implements Iterable<Component> {
    private static final class ComponentParentIterator implements Iterator<Component> {
        private @Nullable Component current;

        protected ComponentParentIterator(final @NotNull Component widget) {
            current = widget;
        }

        @Override
        public boolean hasNext() {
	        return !Objects.isNull(current);
        }

        @Override
        public @NotNull Component next() {
            final @Nullable Component retval = current;
	        if (Objects.isNull(current)) {
                throw new NoSuchElementException("Last parent reached");
            } else {
                current = retval.getParent();
		        if (Objects.isNull(current) && retval instanceof final JPopupMenu menu) {
                    current = menu.getInvoker();
		        } else if (Objects.isNull(current) && retval instanceof final JMenu menu) {
                    current = menu.getPopupMenu();
                }
                return retval;
            }
        }
    }

    private final @NotNull Component widget;

    public ComponentParentStream(final @NotNull Component component) {
        widget = component;
    }

    @Override
    public Iterator<Component> iterator() {
        return new ComponentParentIterator(widget);
    }

    public Stream<Component> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
