package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A Set implementation that's just an ArrayList with Set semantics.
 * @author Jonathan Lovelace
 *
 * @param <T> the type of thing stored in the set
 */
public class ArraySet<T> implements Set<T> { // NOPMD
	/**
	 * The backing array.
	 */
	private final List<T> impl = new ArrayList<T>();
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
	public boolean contains(final Object obj) {
		return impl.contains(obj);
	}
	/**
	 * @return an iterator over the set
	 */
	@Override
	public Iterator<T> iterator() {
		return impl.iterator();
	}
	/**
	 * @return an array view of the set
	 */
	@Override
	public Object[] toArray() {
		return impl.toArray();
	}
	/**
	 * @param <TYPE> the type
	 * @param array an array of that type
	 * @return the contents of the set in that array
	 */
	@Override
	public <TYPE> TYPE[] toArray(final TYPE[] array) {
		return impl.toArray(array);
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
	public boolean remove(final Object obj) {
		return impl.remove(obj);
	}
	/**
	 * @param coll a collection
	 * @return whether the set contains all its elements
	 */
	@Override
	public boolean containsAll(final Collection<?> coll) {
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
		for (T obj : coll) {
			if (add(obj)) {
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
	public boolean retainAll(final Collection<?> coll) {
		return impl.retainAll(coll);
	}
	/**
	 * @param coll a collection
	 * @return the result of removing everything in it
	 */
	@Override
	public boolean removeAll(final Collection<?> coll) {
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
	 * @return a hash-value for the set.
	 */
	@Override
	public int hashCode() {
		return impl.hashCode();
	}
	/**
	 * Another object is equal to this one if it is a set that contains the same elements.
	 * @param obj another object
	 * @return whether it's equal to this
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public boolean equals(final Object obj) {
		return this == obj || (obj instanceof Set && ((Set) obj).containsAll(this)
				&& containsAll((Set) obj));
	}
	/**
	 * TODO: Should perhaps include data on its contents.
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ArraySet";
	}
}
