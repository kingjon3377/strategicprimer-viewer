package model.map.fixtures;

import java.util.Formatter;
import model.map.HasMutableImage;
import model.map.HasMutableKind;
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
public class Implement
		implements UnitMember, FortressMember, HasMutableKind, HasMutableImage {
	/**
	 * The ID # of the implement.
	 */
	private final int id;
	/**
	 * The "kind" of the implement.
	 */
	private String kind;
	/**
	 * The image to use for the implement.
	 */
	private String image = "";

	/**
	 * @param implKind the "kind" of implement
	 * @param idNum    an ID # for the implement
	 */
	public Implement(final String implKind, final int idNum) {
		id = idNum;
		kind = implKind;
	}

	/**
	 * @return the ID # of the implement
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * @param fix a fixture
	 * @return whether it equals this one except for ID
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof Implement) && kind.equals(((Implement) fix).kind);
	}

	/**
	 * @param obj     a fixture
	 * @param ostream the stream to report errors to
	 * @param context the context to report before errors
	 * @return whether it's a subset of (i.e. equal to) this one
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean isSubset(final IFixture obj, final Formatter ostream,
							final String context) {
		if (obj.getID() != id) {
			ostream.format("%s\tIDs differ%n", context);
			return false;
		} else if (obj instanceof Implement) {
			return areObjectsEqual(ostream, kind, ((Implement) obj).kind,
					"%s\tIn Implement ID #%d%n: Kinds differ",
					context, Integer.valueOf(id));
		} else {
			ostream.format("%s\tDifferent fixture types given for ID #%d%n", context,
					Integer.valueOf(id));
			return false;
		}
	}

	/**
	 * @param zero whether to "zero out" sensitive information
	 * @return a copy of this Implement
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Implement copy(final boolean zero) {
		return new Implement(kind, id);
	}

	/**
	 * @return the "kind" of implement
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * @param nKind the new "kind" for the implement
	 */
	@Override
	public void setKind(final String nKind) {
		kind = nKind;
	}

	/**
	 * @return the filename of an image to use for implements by default
	 */
	@Override
	public String getDefaultImage() {
		return "implement.png";
	}

	/**
	 * @return the filename of an image to use for this implement
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * @param img the filename of an image to use for this implement
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
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
	 * @return a hash value for this object
	 */
	@Override
	public int hashCode() {
		return id | kind.hashCode();
	}

	/**
	 * @return a String representation of the implement
	 */
	@Override
	public String toString() {
		return "An implement of kind " + kind;
	}
}
