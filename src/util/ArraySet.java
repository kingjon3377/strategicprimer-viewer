package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A Set implementation that's just an ArrayList with Set semantics.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <U> the type of thing stored in the set
 * @author Jonathan Lovelace
 */
public final class ArraySet<U> implements Set<U> {
	/**
	 * The backing array.
	 */
	private final List<U> impl = new ArrayList<>();
	/**
	 * The running total of the hash code.
	 */
	private int hash = 0;

	/**
	 * Tests whether two sets are equal according to the Set interface's documentation.
	 * @param firstSet  one set
	 * @param secondSet another set
	 * @return whether they are equal according to the Set contract.
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	private static boolean areSetsEqual(final Set<?> firstSet, final Set<?> secondSet) {
		return firstSet.containsAll(secondSet) && secondSet.containsAll(firstSet);
	}

	/**
	 * The size of the set is the size of its implementing list.
	 * @return the size of the set
	 */
	@Override
	public int size() {
		return impl.size();
	}

	/**
	 * The set is empty iff its implementing array is empty.
	 * @return whether the set is empty
	 */
	@Override
	public boolean isEmpty() {
		return impl.isEmpty();
	}

	/**
	 * The set contains an object iff the implementing list contains it.
	 * @param obj an object
	 * @return whether we contain it
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public boolean contains(@Nullable final Object obj) {
		return impl.contains(obj);
	}

	/**
	 * The iterator over the set is the iterator over the implementing list.
	 * @return an iterator over the set
	 */
	@Override
	public Iterator<U> iterator() {
		return NullCleaner.assertNotNull(impl.iterator());
	}

	/**
	 * The implementing list's array-view is used.
	 * @return an array view of the set
	 */
	@Override
	public Object[] toArray() {
		return NullCleaner.assertNotNull(impl.toArray());
	}

	/**
	 * The implementing list's array-view is used.
	 * @param <T>   the type
	 * @param array an array of that type
	 * @return the contents of the set in that array
	 */
	@SuppressWarnings(
			{"SuspiciousToArrayCall", "ParameterNameDiffersFromOverriddenParameter"})
	@Override
	public <T> T[] toArray(final T @Nullable [] array) {
		return impl.toArray(array);
	}

	/**
	 * An element is added iff it was not already in the set; in that case, we also make
	 * sure its presence is reflected in the hash-code.
	 * @param elem an element
	 * @return the result of adding it to the set.
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public boolean add(final U elem) {
		if (contains(elem)) {
			return false;
		} else {
			impl.add(elem);
			hash += Objects.hashCode(elem);
			return true;
		}
	}

	/**
	 * Removal is delegated to the implementing list, but we update the hash-code if the
	 * set changed.
	 * @param obj an object
	 * @return the result of removing it from the set
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public boolean remove(@Nullable final Object obj) {
		final boolean retval = impl.remove(obj);
		if (retval && (obj != null)) {
			hash -= obj.hashCode();
		}
		return retval;
	}

	/**
	 * Elements are in the set iff they are in the implementing list.
	 * @param coll a collection
	 * @return whether the set contains all its elements
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public boolean containsAll(@Nullable final Collection<?> coll) {
		return impl.containsAll(NullCleaner.assertNotNull(coll));
	}

	/**
	 * Element addition is delegated to the implementing list.
	 * @param coll a collection
	 * @return the result of adding all of them
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public boolean addAll(@Nullable final Collection<? extends U> coll) {
		boolean retval = false;
		//noinspection ConstantConditions
		if (coll != null) {
			for (final U obj : coll) {
				if (add(obj)) {
					retval = true;
				}
			}
		}
		return retval;
	}

	/**
	 * This operation is delegated to the implementing list, but we recalculate the
	 * hash-code if the set changed.
	 * @param coll a collection
	 * @return the result of removing everything not in it
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public boolean retainAll(@Nullable final Collection<?> coll) {
		final boolean retval = impl.retainAll(coll);
		if (retval) {
			hash = impl.stream().mapToInt(Object::hashCode).sum();
		}
		return retval;
	}

	/**
	 * This operation is delegated to the implementing list, but we recalculate the
	 * hash-code if the set changed.
	 * @param coll a collection
	 * @return the result of removing everything in it
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public boolean removeAll(@Nullable final Collection<?> coll) {
		final boolean retval = impl.removeAll(coll);
		if (retval) {
			hash = impl.stream().mapToInt(Object::hashCode).sum();
		}
		return retval;
	}

	/**
	 * Empty the set.
	 */
	@Override
	public void clear() {
		impl.clear();
		hash = 0;
	}

	/**
	 * Note that since this is a cached value, computed by adding to the cached value
	 * whenever something is added to the set and subtracting whenever something is
	 * removed, and only recomputed on removeAll and retainAll operations, if a member's
	 * hash value changes after it is added, this will no longer be correct according to
	 * the Set contract. But having a hash code that changes is usually going to cause
	 * bugs with a Set anyway.
	 *
	 * @return a hash-value for the set.
	 */
	@Override
	public int hashCode() {
		return hash;
	}

	/**
	 * Another object is equal to this one if it is a set that contains the same
	 * elements.
	 *
	 * @param obj another object
	 * @return whether it's equal to this
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Set)
										 && areSetsEqual(this, (Set<?>) obj));
	}

	/**
	 * A not-quite-trivial delegation to the implementing list.
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ArraySet " + impl;
	}
}
