package model.map.fixtures.resources;

import org.eclipse.jdt.annotation.Nullable;

import model.map.HasKind;
import model.map.IFixture;
import model.map.TileFixture;
import util.NullCleaner;

/**
 * An orchard (fruit trees) or grove (other trees) on the map.
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
public class Grove implements HarvestableFixture, HasKind {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Whether this is a fruit orchard.
	 */
	private final boolean orchard;
	/**
	 * Whether it's wild (if false) or cultivated.
	 */
	private final boolean cultivated;
	/**
	 * Kind of tree.
	 */
	private String kind;

	/**
	 * Constructor.
	 *
	 * @param fruit whether the trees are fruit trees
	 * @param cultivatedGrove whether the trees are cultivated
	 * @param tree what kind of trees are in the grove
	 * @param idNum the ID number.
	 */
	public Grove(final boolean fruit, final boolean cultivatedGrove,
			final String tree, final int idNum) {
		orchard = fruit;
		cultivated = cultivatedGrove;
		kind = tree;
		id = idNum;
	}

	/**
	 * @return a copy of this grove
	 * @param zero ignored, as a grove has no sensitive information
	 */
	@Override
	public Grove copy(final boolean zero) {
		Grove retval = new Grove(orchard, cultivated, kind, id);
		retval.setImage(image);
		return retval;
	}
	/**
	 * @return true if this is an orchard, false otherwise
	 */
	public boolean isOrchard() {
		return orchard;
	}

	/**
	 * @return if this is a cultivated grove or orchard, false if it's a wild
	 *         one
	 */
	public boolean isCultivated() {
		return cultivated;
	}

	/**
	 * @return what kind of trees are in the grove
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * @return the name of an image to represent the grove or orchard
	 */
	@Override
	public String getDefaultImage() {
		if (orchard) {
			return "orchard.png"; // NOPMD
		} else {
			return "tree.png";
		}
	}

	/**
	 * @return a String representation of the grove or orchard
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(19 + kind.length());
		if (cultivated) {
			builder.append("Cultivated ");
		} else {
			builder.append("Wild ");
		}
		builder.append(kind);
		if (orchard) {
			builder.append(" orchard");
		} else {
			builder.append(" grove");
		}
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 35;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof Grove
				&& kind.equals(((Grove) obj).kind)
				&& orchard == ((Grove) obj).orchard
				&& cultivated == ((Grove) obj).cultivated
				&& id == ((Grove) obj).id;
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
	public int compareTo(@Nullable final TileFixture fix) {
		if (fix == null) {
			throw new IllegalArgumentException("Compared to null fixture");
		}
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
		return fix instanceof Grove && kind.equals(((Grove) fix).kind)
				&& orchard == ((Grove) fix).orchard
				&& cultivated == ((Grove) fix).cultivated;
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
	 * @return a string describing all groves and orchards as a class.
	 */
	@Override
	public String plural() {
		return "Groves and orchards";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}
}
