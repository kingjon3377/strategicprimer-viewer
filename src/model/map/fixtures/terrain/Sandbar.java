package model.map.fixtures.terrain;

import org.eclipse.jdt.annotation.Nullable;

import model.map.HasImage;
import model.map.IFixture;
import model.map.TerrainFixture;
import model.map.TileFixture;

/**
 * A sandbar on the map.
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public class Sandbar implements TerrainFixture, HasImage {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * @param idNum the ID number.
	 */
	public Sandbar(final int idNum) {
		id = idNum;
	}

	/**
	 * @return a String representation of the sandbar.
	 */
	@Override
	public String toString() {
		return "Sandbar";
	}

	/**
	 * @return the name o an image to represent the sandbar.
	 */
	@Override
	public String getDefaultImage() {
		return "sandbar.png";
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 5;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof Sandbar
				&& id == ((TileFixture) obj).getID();
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
		return fix instanceof Sandbar;
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
	 * @return a string describing all sandbars as a class
	 */
	@Override
	public String plural() {
		return "Sandbars";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "a sandbar";
	}
	/**
	 * @return a copy of this sandbar
	 * @param zero ignored, as a sandbar has no sensitive information
	 */
	@Override
	public Sandbar copy(final boolean zero) {
		Sandbar retval = new Sandbar(id);
		retval.setImage(image);
		return retval;
	}
}
