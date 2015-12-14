package model.map.fixtures.resources;

import org.eclipse.jdt.annotation.Nullable;

import model.map.HasKind;
import model.map.IFixture;
import model.map.TileFixture;
import util.NullCleaner;

/**
 * A field or meadow. If in forest, should increase a unit's vision slightly
 * when the unit is on it.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public class Meadow implements HarvestableFixture, HasKind {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Which season the field is in.
	 */
	private final FieldStatus status;
	/**
	 * Whether this is a field. If not, it's a meadow.
	 */
	private final boolean field;
	/**
	 * Whether it's under cultivation.
	 */
	private final boolean cultivated;
	/**
	 * Kind of grass or grain growing there.
	 */
	private String kind;

	/**
	 * Constructor.
	 *
	 * @param grain the kind of grass or grain growing in the field or meadow
	 * @param fld whether this is a field (as opposed to a meadow)
	 * @param cult whether it's under cultivation
	 * @param idNum the ID number.
	 * @param stat the status of the field, i.e. what season it's in
	 */
	public Meadow(final String grain, final boolean fld, final boolean cult,
			final int idNum, final FieldStatus stat) {
		kind = grain;
		field = fld;
		cultivated = cult;
		id = idNum;
		status = stat;
	}

	/**
	 * @return a copy of this meadow
	 * @param zero ignored; there's no sensitive information
	 */
	@Override
	public Meadow copy(final boolean zero) {
		final Meadow retval = new Meadow(kind, field, cultivated, id, status);
		retval.setImage(image);
		return retval;
	}
	/**
	 * @return the kind of grass or grain growing in the meadow or field
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * @return if this is a cultivated field or meadow
	 */
	public boolean isCultivated() {
		return cultivated;
	}

	/**
	 * @return true if this is a field, false if it's a meadow
	 */
	public boolean isField() {
		return field;
	}

	/**
	 * @return the status of the field, i.e. what season it's in
	 */
	public FieldStatus getStatus() {
		return status;
	}

	/**
	 * TODO: This should be more granular based on the kind of field.
	 *
	 * @return the name of an image to represent the field or meadow
	 */
	@Override
	public String getDefaultImage() {
		if (field) {
			return "field.png"; // NOPMD
		} else {
			return "meadow.png";
		}
	}

	/**
	 * @return a String representation of the field or meadow
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		if (field) {
			if (!cultivated) {
				builder.append("Wild or abandoned ");
			}
			builder.append(kind);
			builder.append(" field");
		} else {
			builder.append(kind);
			builder.append(" meadow");
		}
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * TODO: Should probably depend.
	 *
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 15;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof Meadow && equalsImpl((Meadow) obj);
	}
	/**
	 * @param obj a Meadow
	 * @return whether it equals this one
	 */
	private boolean equalsImpl(final Meadow obj) {
		return kind.equals(obj.kind) && field == obj.field
				&& cultivated == obj.cultivated && id == obj.id;
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
	 *
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
		return fix instanceof Meadow && kind.equals(((Meadow) fix).kind)
				&& field == ((Meadow) fix).field
				&& cultivated == ((Meadow) fix).cultivated
				&& status == ((Meadow) fix).status;
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
	 * @return a string describing all meadows as a class
	 */
	@Override
	public String plural() {
		return "Fields and meadows";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}
}
