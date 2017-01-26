package model.map.fixtures.terrain;

import model.map.HasMutableImage;
import model.map.IFixture;
import model.map.TerrainFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A mountain on the map---or at least a fixture representing mountainous terrain.
 *
 * TODO: can we get rid of this class?
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
public final class Mountain implements TerrainFixture, HasMutableImage {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Even though Mountains have no state other than their image, we still copy because
	 * they might eventually.
	 *
	 * @param zero ignored, as a mountain has no state (other than its image)
	 * @return a copy of this mountain
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Mountain copy(final boolean zero) {
		final Mountain retval = new Mountain();
		retval.image = image;
		return retval;
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the forest.
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "Mountain.";
	}

	/**
	 * The default icon filename.
	 * @return the name of an image to represent the mountain.
	 */
	@Override
	public String getDefaultImage() {
		return "mountain.png";
	}

	/**
	 * An object is equal iff it is a Mountain.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || (obj instanceof Mountain);
	}

	/**
	 * Since Mountains have no state, use a constant hash value.
	 * @return a hash value for the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public int hashCode() {
		return 1;
	}

	/**
	 * A dummy (and invalid) ID number.
	 * @return an ID number
	 */
	@Override
	public int getID() {
		return -1;
	}

	/**
	 * A fixture is equal iff it is a Mountain.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return equals(fix);
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
	 * The plural of Mountain is Mountains.
	 * @return a string describing all mountains as a class
	 */
	@Override
	public String plural() {
		return "Mountains";
	}

	/**
	 * "a mountain".
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "a mountain";
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		return 5;
	}
}
