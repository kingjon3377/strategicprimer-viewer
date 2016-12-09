package model.map.fixtures.mobile;

import java.io.IOException;
import java.util.Formatter;
import model.map.HasMutableImage;
import model.map.HasMutableKind;
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
public class Centaur
		implements MobileFixture, HasMutableImage, HasMutableKind, UnitMember {
	/**
	 * ID number.
	 */
	private final int id;
	/**
	 * What kind of centaur.
	 */
	private String kind;
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
	 * @return the kind of centaur
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * @param nKind the new kind
	 */
	@Override
	public final void setKind(final String nKind) {
		kind = nKind;
	}

	/**
	 * @return a String representation of the centaur
	 */
	@Override
	public String toString() {
		return kind + " centaur";
	}

	/**
	 * @return the name of an image to represent the centaur
	 */
	@Override
	public String getDefaultImage() {
		return "centaur.png";
	}

	/**
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
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof Centaur) && ((Centaur) fix).kind.equals(kind);
	}

	/**
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
	 * @return a string describing all centaurs as a class
	 */
	@Override
	public String plural() {
		return "Centaurs";
	}

	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}
}
