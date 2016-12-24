package model.map.fixtures.mobile;

import java.util.Formatter;
import model.map.HasKind;
import model.map.HasMutableImage;
import model.map.IFixture;
import model.map.fixtures.UnitMember;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A centaur. TODO: Should probably be a kind of unit instead, or something ...
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
public class Centaur implements MobileFixture, HasMutableImage, HasKind, UnitMember {
	/**
	 * ID number.
	 */
	private final int id;
	/**
	 * What kind of centaur.
	 */
	private final String kind;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Constructor.
	 *
	 * @param centKind the kind of centaur
	 * @param idNum    the ID number.
	 */
	public Centaur(final String centKind, final int idNum) {
		kind = centKind;
		id = idNum;
	}

	/**
	 * Clone the Centaur object.
	 * @param zero ignored, as a centaur has no sensitive information
	 * @return a copy of this centaur
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Centaur copy(final boolean zero) {
		final Centaur retval = new Centaur(kind, id);
		retval.image = image;
		return retval;
	}

	/**
	 * The kind of centaur.
	 * @return the kind of centaur
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * "such-and-such-kind-of centaur" is the pattern.
	 * @return a String representation of the centaur
	 */
	@Override
	public String toString() {
		return kind + " centaur";
	}

	/**
	 * The default icon filename.
	 * @return the name of an image to represent the centaur
	 */
	@Override
	public String getDefaultImage() {
		return "centaur.png";
	}

	/**
	 * Test for equality with us.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Centaur)
										 && ((Centaur) obj).kind.equals(kind)
										 && (((Centaur) obj).id == id));
	}

	/**
	 * We use our ID number as our hash value.
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
	 * Test for equality ignoring ID, which means any Centaur of the same kind counts.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof Centaur) && ((Centaur) fix).kind.equals(kind);
	}

	/**
	 * An object is a "strict subset" of this one if it equal to us.
	 * @param obj     another UnitMember
	 * @param ostream a stream to report an explanation on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether that member equals this one
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean isSubset(final IFixture obj, final Formatter ostream,
							final String context) {
		if (obj.getID() == id) {
			if (obj instanceof Centaur) {
				return areObjectsEqual(ostream, kind, ((Centaur) obj).kind,
						"%s\tDifferent kinds of centaur for ID #%d%n", context,
						Integer.valueOf(id));
			} else {
				ostream.format("%s\tFor ID #%d, different kinds of members%n", context,
						Integer.valueOf(id));
				return false;
			}
		} else {
			ostream.format("%s\tCalled with different IDs, #%d and #%d%n", context,
					Integer.valueOf(id), Integer.valueOf(obj.getID()));
			return false;
		}
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
	 * Set a per-instance icon filename.
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * The plural of Centaur is Centaurs.
	 * @return a string describing all centaurs as a class
	 */
	@Override
	public String plural() {
		return "Centaurs";
	}

	/**
	 * "such-and-such-kind-of centaur" is the pattern.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		return 20;
	}
}
