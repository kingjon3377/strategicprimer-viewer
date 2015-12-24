package model.map.fixtures.terrain;

import model.map.HasImage;
import model.map.IFixture;
import model.map.TerrainFixture;
import model.map.TileFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A mountain on the map---or at least a fixture representing mountainous terrain.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class Mountain implements TerrainFixture, HasImage {
	/**
	 * Even though Mountains have no state other than their image, we still copy because
	 * they might eventually.
	 *
	 * @param zero ignored, as a mountain has no state (other than its image)
	 * @return a copy of this mountain
	 */
	@Override
	public Mountain copy(final boolean zero) {
		final Mountain retval = new Mountain();
		retval.setImage(image);
		return retval;
	}

	/**
	 * @return a String representation of the forest.
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "Mountain.";
	}

	/**
	 * @return the name of an image to represent the mountain.
	 */
	@Override
	public String getDefaultImage() {
		return "mountain.png";
	}

	/**
	 * The z-value given to a mountain.
	 */
	private static final int Z_VALUE = 10;

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return Z_VALUE;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || (obj instanceof Mountain);
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return 1;
	}

	/**
	 * @param fix A TileFixture to compare to
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}

	/**
	 * @return an ID number
	 */
	@Override
	public int getID() {
		return -1;
	}

	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return equals(fix);
	}

	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

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
	 * @return a string describing all mountains as a class
	 */
	@Override
	public String plural() {
		return "Mountain";
	}

	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "a mountain";
	}
}
