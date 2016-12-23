package model.map.fixtures.mobile;

import java.util.Formatter;
import model.map.HasKind;
import model.map.HasMutableImage;
import model.map.IFixture;
import model.map.fixtures.UnitMember;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A fairy. TODO: should probably be a unit, or something.
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
public class Fairy implements MobileFixture, HasMutableImage, HasKind, UnitMember {
	/**
	 * ID number.
	 */
	private final int id;
	/**
	 * What kind of fairy (great, lesser, snow ...).
	 */
	private final String kind;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Constructor.
	 *
	 * @param fKind the kind of fairy
	 * @param idNum the ID number.
	 */
	public Fairy(final String fKind, final int idNum) {
		kind = fKind;
		id = idNum;
	}

	/**
	 * Clone a Fairy.
	 * @param zero ignored, as a fairy has no sensitive information
	 * @return a copy of this fairy
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Fairy copy(final boolean zero) {
		final Fairy retval = new Fairy(kind, id);
		retval.image = image;
		return retval;
	}

	/**
	 * The kind of fairy.
	 * @return the kind of fairy
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * Delegates to shortDesc().
	 * @return a String representation of the fairy
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
		return "fairy.png";
	}

	/**
	 * An object is equal iff it is a Fairy of the same kind with the same ID.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) ||
					   ((obj instanceof Fairy) && kind.equals(((Fairy) obj).kind) &&
								(id == ((Fairy) obj).id));
	}

	/**
	 * Use the ID number for hashing.
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
	 * If we ignore ID, a fixture is equal iff it is a Fairy of the same kind.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof Fairy) && ((Fairy) fix).kind.equals(kind);
	}

	/**
	 * A fixture is a subset iff it is equal.
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
			if (obj instanceof Fairy) {
				return areObjectsEqual(ostream, kind, ((Fairy) obj).kind,
						"%s\tDifferent kinds of fairy for ID #%d%n", context,
						Integer.valueOf(id));
			} else {
				ostream.format("%s\tFor ID #%d, different kinds of members%n", context,
						Integer.valueOf(id));
				return false;
			}
		} else {
			ostream.format("%sCalled with different IDs, #%d and #%d%n", context,
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
	 * The plural of Fairy is Fairies.
	 * @return a string describing all fairies as a class
	 */
	@Override
	public String plural() {
		return "Fairies";
	}

	/**
	 * "[kind] fairy".
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return kind + " fairy";
	}
}
