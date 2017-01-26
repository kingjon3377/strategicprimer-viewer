package model.map.fixtures.mobile;

import java.util.Formatter;
import model.map.HasKind;
import model.map.HasMutableImage;
import model.map.IFixture;
import model.map.fixtures.UnitMember;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A dragon. TODO: should probably be a unit, or something.
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
public class Dragon implements MobileFixture, HasMutableImage, HasKind, UnitMember {
	/**
	 * ID number.
	 */
	private final int id;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * What kind of dragon. (Usually blank, at least at first.)
	 */
	private final String kind;

	/**
	 * Constructor.
	 *
	 * @param dKind the kind of dragon
	 * @param idNum the ID number.
	 */
	public Dragon(final String dKind, final int idNum) {
		kind = dKind;
		id = idNum;
	}

	/**
	 * Clone the dragon.
	 * @param zero ignored, as a dragon has no sensitive information
	 * @return a copy of this dragon
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Dragon copy(final boolean zero) {
		final Dragon retval = new Dragon(kind, id);
		retval.image = image;
		return retval;
	}

	/**
	 * The kind of dragon.
	 * @return the kind of dragon
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * If the dragon has a kind, "such-and-such dragon"; otherwise, merely "dragon".
	 * @return a String representation of the dragon
	 */
	@Override
	public String toString() {
		if (kind.isEmpty()) {
			return "dragon";
		} else {
			return kind + " dragon";
		}
	}

	/**
	 * From OpenClipArt, public domain.
	 * {@link "https://openclipart.org/detail/166560/fire-dragon-by-olku"}
	 *
	 * @return the name of an image to represent the dragon
	 */
	@Override
	public String getDefaultImage() {
		return "dragon.png";
	}

	/**
	 * We are only equal to dragons with the same kind and ID.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Dragon)
										 && kind.equals(((Dragon) obj).kind) &&
										 (id == ((Dragon) obj).id));
	}

	/**
	 * The ID number is the hash value.
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
	 * All dragons of the same kind are equal if we ignore ID.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings({"CastToConcreteClass", "InstanceofInterfaces"})
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof Dragon) && ((Dragon) fix).kind.equals(kind);
	}

	/**
	 * A fixture is a subset iff it is equal.
	 * @param obj     another UnitMember
	 * @param ostream a stream to report an explanation on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether that member equals this one
	 */
	@SuppressWarnings({"CastToConcreteClass", "InstanceofInterfaces"})
	@Override
	public boolean isSubset(final IFixture obj, final Formatter ostream,
							final String context) {
		if (obj.getID() == id) {
			if (obj instanceof Dragon) {
				return areObjectsEqual(ostream, kind, ((Dragon) obj).kind,
						"%s\tDifferent kinds of dragon for ID #%d%n", context,
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
	 * Set the per-instance icon filename.
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * The plural of Dragon is Dragons.
	 * @return a string describing all dragons as a class
	 */
	@Override
	public String plural() {
		return "Dragons";
	}

	/**
	 * We delegate to toString().
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
		return 30;
	}
}
