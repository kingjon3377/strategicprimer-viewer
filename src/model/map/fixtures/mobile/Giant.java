package model.map.fixtures.mobile;

import java.util.Formatter;
import model.map.HasKind;
import model.map.HasMutableImage;
import model.map.IFixture;
import model.map.fixtures.UnitMember;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A giant. TODO: should probably be a unit, or something.
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
public class Giant implements MobileFixture, HasMutableImage, HasKind, UnitMember {
	/**
	 * ID number.
	 */
	private final int id;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * What kind of giant. (Usually blank, at least at first.)
	 */
	private final String kind;

	/**
	 * Constructor.
	 *
	 * @param gKind the kind of giant
	 * @param idNum the ID number.
	 */
	public Giant(final String gKind, final int idNum) {
		kind = gKind;
		id = idNum;
	}

	/**
	 * Clone the giant.
	 * @param zero ignored, as a giant has no sensitive information
	 * @return a copy of this giant
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Giant copy(final boolean zero) {
		final Giant retval = new Giant(kind, id);
		retval.image = image;
		return retval;
	}

	/**
	 * What kind of giant.
	 * @return the kind of giant
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * Delegates to shortDesc().
	 * @return a String representation of the giant
	 */
	@Override
	public String toString() {
		return shortDesc();
	}

	/**
	 * The default icon filename.
	 * @return the name of an image to represent the fairy
	 */
	@Override
	public String getDefaultImage() {
		return "giant.png";
	}

	/**
	 * An object is equal if it is a giant of the same kind with the same ID.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) ||
					   ((obj instanceof Giant) && ((Giant) obj).kind.equals(kind) &&
								(id == ((Giant) obj).id));
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
	 * If we ignore ID, a fixture is equal if it is a Giant of the same kind.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof Giant) && ((Giant) fix).kind.equals(kind);
	}

	/**
	 * A fixture is a subset iff is equal.
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
			if (obj instanceof Giant) {
				return areObjectsEqual(ostream, kind, ((Giant) obj).kind,
						"%s\tDifferent kinds of giant for ID #%d%n",
						context, Integer.valueOf(id));
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
	 * Set the per-instance icon filename.
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * The plural of Giant is Giants.
	 * @return a string describing all giants as a class
	 */
	@Override
	public String plural() {
		return "Giants";
	}

	/**
	 * If no kind, "giant"; otherwise, "such-and-such giant".
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		if (kind.isEmpty()) {
			return "giant";
		} else {
			return kind + " giant";
		}
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * TODO: should this vary with kind?
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		return 28;
	}
}
