package model.map.fixtures.mobile;

import java.io.IOException;
import java.util.Formatter;
import model.map.HasMutableImage;
import model.map.HasMutableKind;
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
public class Dragon
		implements MobileFixture, HasMutableImage, HasMutableKind, UnitMember {
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
	private String kind;

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
	 * @return the kind of dragon
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
		return (fix instanceof Dragon) && ((Dragon) fix).kind.equals(kind);
	}

	/**
	 * @param obj     another UnitMember
	 * @param ostream a stream to report an explanation on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether that member equals this one
	 * @throws IOException on I/O error writing output to the stream
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean isSubset(final IFixture obj, final Formatter ostream,
							final String context) throws IOException {
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
	 * @return a string describing all dragons as a class
	 */
	@Override
	public String plural() {
		return "Dragons";
	}

	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}
}
