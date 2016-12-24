package model.map.fixtures.resources;

import model.map.HasKind;
import model.map.IFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A cache (of vegetables, or a hidden treasure, or ...) on a tile.
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
 * @author Jonathan Lovelace
 */
public class CacheFixture implements HarvestableFixture, HasKind {
	/**
	 * The contents of this cache. TODO: Should be turned into objects (serialized as
	 * children) as part of the general Resource framework.
	 */
	private final String contents;
	/**
	 * ID number.
	 */
	private final int id;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * What kind of things this is a cache of.
	 *
	 * TODO: Should perhaps be enumerated, so we can make images more granular.
	 */
	private final String kind;

	/**
	 * Constructor.
	 *
	 * @param category what kind of things this is a cache of
	 * @param cont     what this cache contains
	 * @param idNum    the ID number.
	 */
	public CacheFixture(final String category, final String cont,
						final int idNum) {
		kind = category;
		contents = cont;
		id = idNum;
	}

	/**
	 * Clone the CacheFixture.
	 * @param zero ignored, as there's no sensitive data
	 * @return a copy of this cache
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public CacheFixture copy(final boolean zero) {
		final CacheFixture retval = new CacheFixture(kind, contents, id);
		retval.image = image;
		return retval;
	}

	/**
	 * The kind of things in the cache.
	 * @return what kind of things this is a cache of
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * The specific contents of the cache.
	 * @return the contents of this cache
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * TODO: should be more granular.
	 *
	 * @return the name of an image to represent the cache
	 */
	@Override
	public String getDefaultImage() {
		return "cache.png";
	}

	/**
	 * "a cache of such-and-such containing such-and-such" is the general pattern.
	 * @return a string representation of the cache
	 */
	@Override
	public String toString() {
		return String.format("a cache of %s containing %s", kind, contents);
	}

	/**
	 * Test an object for equality with us.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof CacheFixture)
										 && equalsImpl((CacheFixture) obj));
	}

	/**
	 * The implementation of the test for equality once we know the object is a
	 * CacheFixture.
	 * @param obj a cache-fixture
	 * @return whether it's equal to this one
	 */
	private boolean equalsImpl(final CacheFixture obj) {
		return kind.equals(obj.kind) && contents.equals(obj.contents)
					   && (id == obj.id);
	}

	/**
	 * We use our ID number for our hash value.
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * The ID number for the cache.
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * Test for equality ignoring ID number.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof CacheFixture)
					   && kind.equals(((CacheFixture) fix).kind)
					   && contents.equals(((CacheFixture) fix).contents);
	}

	/**
	 * The per-instance icon filename.
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * Set the per-instance icon filename.
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * The plural of Cache is Caches.
	 * @return a string describing all caches as a class
	 */
	@Override
	public String plural() {
		return "Caches";
	}

	/**
	 * "a cache of such-and-such" is the pattern here.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "a cache of " + kind;
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		return 25;
	}
}
