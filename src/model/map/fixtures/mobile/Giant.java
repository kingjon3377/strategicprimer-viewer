package model.map.fixtures.mobile;

import model.map.HasImage;
import model.map.HasKind;
import model.map.IFixture;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;
import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;

/**
 * A giant. TODO: should probably be a unit, or something.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class Giant implements MobileFixture, HasImage, HasKind, UnitMember {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * What kind of giant. (Usually blank, at least at first.)
	 */
	private String kind;

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
	 * @param zero ignored, as a giant has no sensitive information
	 * @return a copy of this giant
	 */
	@Override
	public Giant copy(final boolean zero) {
		final Giant retval = new Giant(kind, id);
		retval.setImage(image);
		return retval;
	}

	/**
	 * @return the kind of giant
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * @return a String representation of the giant
	 */
	@Override
	public String toString() {
		if (kind.isEmpty()) {
			return "giant"; // NOPMD
		} else {
			return kind + " giant";
		}
	}

	/**
	 * @return the name of an image to represent the fairy
	 */
	@Override
	public String getDefaultImage() {
		return "giant.png";
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 40;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Giant)
				                         && ((Giant) obj).kind.equals(kind)
				                         && (id == ((Giant) obj).getID()));
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * @param fix A TileFixture to compare to
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}

	/**
	 * ID number.
	 */
	private final int id; // NOPMD

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
		return (fix instanceof Giant) && ((Giant) fix).kind.equals(kind);
	}

	/**
	 * @param obj     another UnitMember
	 * @param ostream a stream to report an explanation on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether that member equals this one
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Appendable ostream,
	                        final String context) throws IOException {
		if (obj.getID() == id) {
			if (obj instanceof Giant) {
				if (kind.equals(((Giant) obj).getKind())) {
					return true;
				} else {
					ostream.append(context);
					ostream.append("\tDifferent kinds of giant for ID #");
					ostream.append(Integer.toString(id));
					ostream.append('\n');
					return false;
				}
			} else {
				ostream.append(context);
				ostream.append("\tFor ID #");
				ostream.append(Integer.toString(id));
				ostream.append(", different kinds of members");
				return false;
			}
		} else {
			ostream.append(context);
			ostream.append("\tCalled with different IDs, #");
			ostream.append(Integer.toString(id));
			ostream.append(" and #");
			ostream.append(Integer.toString(obj.getID()));
			ostream.append('\n');
			return false;
		}
	}

	/**
	 * @param nKind the new kind
	 */
	@Override
	public final void setKind(final String nKind) {
		kind = nKind;
	}

	/**
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * @return a string describing all giants as a class
	 */
	@Override
	public String plural() {
		return "Giants";
	}

	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}
}
