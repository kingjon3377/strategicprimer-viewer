package model.map.fixtures.mobile;

import java.util.Formatter;
import model.map.HasMutableImage;
import model.map.IFixture;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A minotaur. TODO: Should probably be a unit, or something.
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
public class Minotaur implements Immortal, HasMutableImage {
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
	public Minotaur(final int idNum) {
		id = idNum;
	}

	/**
	 * Clone the object.
	 * @param zero ignored, as a minotaur has no sensitive information
	 * @return a copy of this minotaur
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Minotaur copy(final boolean zero) {
		final Minotaur retval = new Minotaur(id);
		retval.image = image;
		return retval;
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the minotaur
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "minotaur";
	}

	/**
	 * Image comes from a Flickr user who asks to be credited as 'www.36peas.com'.
	 *
	 * @return the name of an image to represent the minotaur
	 */
	@Override
	public String getDefaultImage() {
		return "minotaur.png";
	}

	/**
	 * An object is equal iff it is a minotaur with the same ID.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Minotaur)
										 && (id == ((TileFixture) obj).getID()));
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
	 * If we ignore ID, a fixture is equal iff it is a Minotaur.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings("InstanceofInterfaces")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof Minotaur;
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
	 * The plural of Minotaur is Minotaurs.
	 * @return a string describing all minotaurs as a class
	 */
	@Override
	public String plural() {
		return "Minotaurs";
	}

	/**
	 * "a minotaur".
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "a minotaur";
	}

	/**
	 * A fixture is a subset if it is equal.
	 * @param obj     another UnitMember
	 * @param ostream a stream to report an explanation on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether that member equals this one
	 */
	@SuppressWarnings("InstanceofInterfaces")
	@Override
	public boolean isSubset(final IFixture obj, final Formatter ostream,
							final String context) {
		if (obj.getID() == id) {
			return isConditionTrue(ostream, obj instanceof Minotaur,
					"%s\tFor ID #%d, different kinds of members%n", context,
					Integer.valueOf(id));
		} else {
			ostream.format("%s\tCalled with different IDs, #%d and #%d%n", context,
					Integer.valueOf(id), Integer.valueOf(obj.getID()));
			return false;
		}
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		return 30;
	}
}
