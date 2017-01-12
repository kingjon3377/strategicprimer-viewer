package model.map.fixtures.terrain;

import model.map.HasMutableImage;
import model.map.IFixture;
import model.map.TerrainFixture;
import model.map.TileFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An oasis on the map.
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
public class Oasis implements TerrainFixture, HasMutableImage {
	/**
	 * ID number.
	 */
	private final int id;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Constructor.
	 * @param idNum the ID number.
	 */
	public Oasis(final int idNum) {
		id = idNum;
	}

	/**
	 * Clone the object.
	 * @param zero ignored, as an oasis has no sensitive information
	 * @return a copy of this oasis
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Oasis copy(final boolean zero) {
		final Oasis retval = new Oasis(id);
		retval.image = image;
		return retval;
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the oasis.
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "Oasis";
	}

	/**
	 * The default icon filename.
	 * @return the name of an image to represent the oasis.
	 */
	@Override
	public String getDefaultImage() {
		return "oasis.png";
	}

	/**
	 * An object is equal iff it is an Oasis with the same ID.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) ||
					   ((obj instanceof Oasis) && (id == ((TileFixture) obj).getID()));
	}

	/**
	 * Use the ID for hashing.
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * The ID number.
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * If we ignore ID, a fixture is equal iff it is an Oasis.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof Oasis;
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
	 * The plural of Oasis is Oases.
	 * @return a string describing all oases as a class
	 */
	@Override
	public String plural() {
		return "Oases";
	}

	/**
	 * "an oasis".
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "an oasis";
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		return 15;
	}
}
