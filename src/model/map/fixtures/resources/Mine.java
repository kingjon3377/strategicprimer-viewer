package model.map.fixtures.resources;

import model.map.HasKind;
import model.map.IFixture;
import model.map.fixtures.towns.TownStatus;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A mine---a source of mineral resources.
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
public class Mine implements HarvestableFixture, HasKind {
	/**
	 * The status of the mine.
	 */
	private final TownStatus status;
	/**
	 * ID number.
	 */
	private final int id;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * What the mine produces.
	 */
	private final String kind;

	/**
	 * Constructor.
	 *
	 * @param mineral what mineral this produces
	 * @param stat    the status of the mine
	 * @param idNum   the ID number.
	 */
	public Mine(final String mineral, final TownStatus stat, final int idNum) {
		kind = mineral;
		status = stat;
		id = idNum;
	}

	/**
	 * Clone the object.
	 * @param zero ignored; there isn't any sensitive information
	 * @return a copy of this mine
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Mine copy(final boolean zero) {
		final Mine retval = new Mine(kind, status, id);
		retval.image = image;
		return retval;
	}

	/**
	 * What kind of mine this is, ie what it produces.
	 * @return what the mine produces
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * The status of the mine.
	 * @return the status of the mine
	 */
	public TownStatus getStatus() {
		return status;
	}

	/**
	 * The default icon filename.
	 * @return the name of an image to represent the mine
	 */
	@Override
	public String getDefaultImage() {
		return "mine.png";
	}

	/**
	 * "[status] mine of [kind]".
	 * @return a string representation of the mine
	 */
	@Override
	public String toString() {
		return status + " mine of " + kind;
	}

	/**
	 * An object is equal iff it is a Mine with the same kind, status, and ID.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Mine)
										 && kind.equals(((Mine) obj).kind)
										 && (status == ((Mine) obj).status) &&
										 (id == ((Mine) obj).id));
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
	 * If we ignore ID, a fixture is equal if it is a Mine with the same kind and status.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof Mine) && kind.equals(((Mine) fix).kind)
					   && (status == ((Mine) fix).status);
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
	 * The plural of Mine is Mines.
	 * @return a string describing all mines as a class
	 */
	@Override
	public String plural() {
		return "Mines";
	}

	/**
	 * A short description of the mine.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return status.toString() + ' ' + kind + " mine";
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * TODO: should perhaps be variable and loaded from XML
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		if (TownStatus.Active == status) {
			return 15;
		} else {
			return 25;
		}
	}
}
