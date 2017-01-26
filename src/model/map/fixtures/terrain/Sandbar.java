package model.map.fixtures.terrain;

import model.map.HasMutableImage;
import model.map.IFixture;
import model.map.TerrainFixture;
import model.map.TileFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A sandbar on the map.
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
public class Sandbar implements TerrainFixture, HasMutableImage {
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
	public Sandbar(final int idNum) {
		id = idNum;
	}

	/**
	 * Trivial toString().
	 * @return a String representation of the sandbar.
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "Sandbar";
	}

	/**
	 * The default icon filename.
	 * @return the name o an image to represent the sandbar.
	 */
	@Override
	public String getDefaultImage() {
		return "sandbar.png";
	}

	/**
	 * An object is equal iff it is a Sandbar with the same ID.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) ||
					   ((obj instanceof Sandbar) && (id == ((TileFixture) obj).getID()));
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
	 * If we ignore ID, all Sandbars are equal.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings("InstanceofInterfaces")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof Sandbar;
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
	 * The plural of Sandbar is Sandbars.
	 * @return a string describing all sandbars as a class
	 */
	@Override
	public String plural() {
		return "Sandbars";
	}

	/**
	 * A short description.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "a sandbar";
	}

	/**
	 * Clone the object.
	 * @param zero ignored, as a sandbar has no sensitive information
	 * @return a copy of this sandbar
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Sandbar copy(final boolean zero) {
		final Sandbar retval = new Sandbar(id);
		retval.image = image;
		return retval;
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		return 18;
	}
}
