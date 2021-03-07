import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

/**
 * A {@link Set} backed by an {@link ArrayList}.
 */
public class ArraySet<Element> extends AbstractSet<Element> {
	/* The backing array. */
	private final ArrayList<Element> impl;

	public ArraySet() {
		impl = new ArrayList<>();
	}

	public ArraySet(Collection<Element> initial) {
		impl = new ArrayList<>(initial);
	}

	/* The size of the set. */
	@Override
	public int size() {
		return impl.size();
	}

	/* Delegate the iterator to the backing list. */
	@Override
	public Iterator<Element> iterator() {
		return impl.iterator();
	}

	/* Add an element, and return true, only if it is not already in the set. */
	@Override
	public boolean add(Element element) {
		if (impl.contains(element)) {
			return false;
		} else {
			impl.add(element);
			return true;
		}
	}

	/* Remove an element. Returns true if it was actually in the set. */
	@Override
	public boolean remove(Object element) {
		return impl.remove(element);
	}

	/* Remove all items from the set. */
	@Override
	public void clear() {
		impl.clear();
	}

	/* Sort the underlying array by {@link the given comparison function|comparing} */
	public void sort(Comparator<Element> comparing) {
		impl.sort(comparing);
	}
}
