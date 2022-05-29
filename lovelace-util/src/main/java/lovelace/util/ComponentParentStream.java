package lovelace.util;

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
			return current != null;
		}

		@Override
		public @NotNull Component next() {
			final @Nullable Component retval = current;
			if (current == null) {
				throw new NoSuchElementException("Last parent reached");
			} else {
				current = retval.getParent();
				if (current == null && retval instanceof JPopupMenu menu) {
					current = menu.getInvoker();
				} else if (current == null && retval instanceof JMenu menu) {
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
