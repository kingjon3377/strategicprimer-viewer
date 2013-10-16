package util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A Set implementation that's just an ArrayList with Set semantics.
 *
 * @author Jonathan Lovelace
 *
 * @param <T> the type of thing stored in the set
 */
public class ArraySet<T> implements Set<T>, Serializable { // NOPMD
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The backing array.
	 */
	private final List<T> impl = new ArrayList<>();

	/**
	 * @return the size of the set
	 */
	@Override
	public int size() {
		return impl.size();
	}

	/**
	 * @return whether the set is empty
	 */
	@Override
	public boolean isEmpty() {
		return impl.isEmpty();
	}

	/**
	 * @param obj an object
	 * @return whether we contain it
	 */
	@Override
	public boolean contains(@Nullable final Object obj) {
		return impl.contains(obj);
	}

	/**
	 * @return an iterator over the set
	 */
	@Override
	public Iterator<T> iterator() {
		final Iterator<T> retval = impl.iterator();
		assert retval != null;
		return retval;
	}

	/**
	 * @return an array view of the set
	 */
	@Override
	public Object[] toArray() {
		final Object[] retval = impl.toArray();
		assert retval != null;
		return retval;
	}

	/**
	 * @param <TYPE> the type
	 * @param array an array of that type
	 * @return the contents of the set in that array
	 */
	@Override
	public <TYPE> TYPE[] toArray(final TYPE[] array) {
		final TYPE[] retval = impl.toArray(array);
		assert retval != null;
		return retval;
	}

	/**
	 * @param elem an element
	 * @return the result of adding it to the set.
	 */
	@Override
	public boolean add(final T elem) {
		if (contains(elem)) {
			return false; // NOPMD
		} else {
			impl.add(elem);
			return true;
		}
	}

	/**
	 * @param obj an object
	 * @return the result of removing it from the set
	 */
	@Override
	public boolean remove(@Nullable final Object obj) {
		return impl.remove(obj);
	}

	/**
	 * @param coll a collection
	 * @return whether the set contains all its elements
	 */
	@Override
	public boolean containsAll(@Nullable final Collection<?> coll) {
		return impl.containsAll(coll);
	}

	/**
	 * @param coll a collection
	 * @return the result of addin gall of them
	 *
	 * @see java.util.Set#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(final Collection<? extends T> coll) {
		boolean retval = false;
		for (final T obj : coll) {
			if (obj == null) {
				continue;
			} else if (add(obj)) {
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * @param coll a collection
	 * @return the result of removing everything not in it
	 */
	@Override
	public boolean retainAll(@Nullable final Collection<?> coll) {
		return impl.retainAll(coll);
	}

	/**
	 * @param coll a collection
	 * @return the result of removing everything in it
	 */
	@Override
	public boolean removeAll(@Nullable final Collection<?> coll) {
		return impl.removeAll(coll);
	}

	/**
	 * Empty the set.
	 */
	@Override
	public void clear() {
		impl.clear();
	}

	/**
	 * TODO: Keep a running-total value for this, modified in add() and
	 * remove(), rather than calculating it every time hashCode is called.
	 *
	 * @return a hash-value for the set.
	 */
	@Override
	public int hashCode() {
		int retval = 0;
		for (final T item : this) {
			if (item != null) {
				retval += item.hashCode();
			}
		}
		return retval;
	}

	/**
	 * Another object is equal to this one if it is a set that contains the same
	 * elements.
	 *
	 * @param obj another object
	 * @return whether it's equal to this
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj
				|| (obj instanceof Set && ((Set) obj).containsAll(this) && containsAll((Set) obj));
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ArraySet " + impl.toString();
	}
}
