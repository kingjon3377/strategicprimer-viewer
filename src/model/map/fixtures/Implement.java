package model.map.fixtures;

import java.util.Formatter;
import model.map.HasKind;
import model.map.HasMutableImage;
import model.map.IFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A piece of equipment.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 *         TODO: more members
 */
public class Implement implements UnitMember, FortressMember, HasKind, HasMutableImage {
	/**
	 * The ID # of the implement.
	 */
	private final int id;
	/**
	 * The "kind" of the implement.
	 */
	private final String kind;
	/**
	 * The image to use for the implement.
	 */
	private String image = "";

	/**
	 * Constructor.
	 * @param implKind the "kind" of implement
	 * @param idNum    an ID # for the implement
	 */
	public Implement(final String implKind, final int idNum) {
		id = idNum;
		kind = implKind;
	}

	/**
	 * The ID number.
	 * @return the ID # of the implement
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * If we ignore ID, a fixture is equal if it is an Implement of the same kind.
	 * @param fix a fixture
	 * @return whether it equals this one except for ID
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof Implement) && kind.equals(((Implement) fix).kind);
	}

	/** A fixture is a subset iff it is equal.
	 * @param obj     a fixture
	 * @param ostream the stream to report errors to
	 * @param context the context to report before errors
	 * @return whether it's a subset of (i.e. equal to) this one
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean isSubset(final IFixture obj, final Formatter ostream,
							final String context) {
		if (obj.getID() == id) {
			if (obj instanceof Implement) {
				return areObjectsEqual(ostream, kind, ((Implement) obj).kind,
						"%s\tIn Implement ID #%d%n: Kinds differ",
						context, Integer.valueOf(id));
			} else {
				ostream.format("%s\tDifferent fixture types given for ID #%d%n", context,
						Integer.valueOf(id));
				return false;
			}
		} else {
			ostream.format("%s\tIDs differ%n", context);
			return false;
		}
	}

	/**
	 * Clone the implement.
	 * @param zero whether to "zero out" sensitive information
	 * @return a copy of this Implement
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Implement copy(final boolean zero) {
		return new Implement(kind, id);
	}

	/**
	 * The kind of equipment.
	 * @return the "kind" of implement
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * The default icon filename.
	 * @return the filename of an image to use for implements by default
	 */
	@Override
	public String getDefaultImage() {
		return "implement.png";
	}

	/**
	 * The per-instance icon filename.
	 * @return the filename of an image to use for this implement
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * Set the per-instance icon filename.
	 * @param img the filename of an image to use for this implement
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * An object is equal iff it is an Object of the same kind and ID.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) ||
					   ((obj instanceof Implement) && (((Implement) obj).id == id) &&
								((Implement) obj).kind.equals(kind));
	}

	/**
	 * Use ID for hashing.
	 * @return a hash value for this object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * A simple toString().
	 * @return a String representation of the implement
	 */
	@Override
	public String toString() {
		return "An implement of kind " + kind;
	}
}
