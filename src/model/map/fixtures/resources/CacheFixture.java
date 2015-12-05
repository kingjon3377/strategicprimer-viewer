package model.map.fixtures.resources;

import org.eclipse.jdt.annotation.Nullable;

import model.map.HasKind;
import model.map.IFixture;
import model.map.TileFixture;
import util.NullCleaner;

/**
 * A cache (of vegetables, or a hidden treasure, or ...) on a tile.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public class CacheFixture implements HarvestableFixture, HasKind {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * What kind of things this is a cache of.
	 *
	 * TODO: Should perhaps be enumerated, so we can make images more granular.
	 */
	private String kind;
	/**
	 * The contents of this cache. TODO: Should be turned into objects
	 * (serialized as children) as part of the general Resource framework.
	 */
	private final String contents;

	/**
	 * Constructor.
	 *
	 * @param category what kind of things this is a cache of
	 * @param cont what this cache contains
	 * @param idNum the ID number.
	 */
	public CacheFixture(final String category, final String cont,
			final int idNum) {
		kind = category;
		contents = cont;
		id = idNum;
	}

	/**
	 * @return a copy of this cache
	 * @param zero ignored, as there's no sensitive data
	 */
	@Override
	public CacheFixture copy(final boolean zero) {
		CacheFixture retval = new CacheFixture(kind, contents, id);
		retval.setImage(image);
		return retval;
	}
	/**
	 * @return what kind of things this is a cache of
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
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
	 * @return a string representation of the cache
	 */
	@Override
	public String toString() {
		return NullCleaner.assertNotNull(new StringBuilder(24 + kind.length()
				+ contents.length()).append("a cache of ").append(kind)
				.append(" containing ").append(contents).toString());
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 22;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof CacheFixture
				&& equalsImpl((CacheFixture) obj);
	}
	/**
	 * @param obj a cache-fixture
	 * @return whether it's equal to this one
	 */
	private boolean equalsImpl(final CacheFixture obj) {
		return kind.equals(obj.kind) && contents.equals(obj.contents)
				&& id == obj.getID();
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * @param fix A TileFixture to compare to
	 *
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}

		/**
	 * ID number.
	 */
	private final int id; // NOPMD

	/**
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof CacheFixture
				&& kind.equals(((CacheFixture) fix).kind)
				&& contents.equals(((CacheFixture) fix).contents);
	}

	/**
	 * @param nKind the new kind
	 */
	@Override
	public final void setKind(final String nKind) {
		kind = nKind;
	}

	/**
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * @return a string describing all caches as a class
	 */
	@Override
	public String plural() {
		return "Caches";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "a cache of " + getKind();
	}
}
