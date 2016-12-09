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
public class Minotaur implements MobileFixture, HasMutableImage, UnitMember {
	/**
	 * ID number.
	 */
	private final int id;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * @param idNum the ID number.
	 */
	public Minotaur(final int idNum) {
		id = idNum;
	}

	/**
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
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Minotaur)
										 && (id == ((TileFixture) obj).getID()));
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

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
		return fix instanceof Minotaur;
	}

	/**
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * @return a string describing all minotaurs as a class
	 */
	@Override
	public String plural() {
		return "Minotaurs";
	}

	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "a minotaur";
	}

	/**
	 * @param obj     another UnitMember
	 * @param ostream a stream to report an explanation on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether that member equals this one
	 */
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
}
