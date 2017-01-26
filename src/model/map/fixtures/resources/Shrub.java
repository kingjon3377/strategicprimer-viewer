package model.map.fixtures.resources;

import model.map.HasKind;
import model.map.IFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A TileFixture to represent shrubs, or their aquatic equivalents, on a tile.
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
public class Shrub implements HarvestableFixture, HasKind {
	/**
	 * ID number.
	 */
	private final int id;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * A description of what kind of shrub this is.
	 */
	private final String description;

	/**
	 * Constructor.
	 *
	 * @param desc  a description of the shrub.
	 * @param idNum the ID number.
	 */
	public Shrub(final String desc, final int idNum) {
		description = desc;
		id = idNum;
	}

	/**
	 * Clone the object.
	 * @param zero ignored, as there's no sensitive data
	 * @return a copy of this shrub
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Shrub copy(final boolean zero) {
		final Shrub retval = new Shrub(description, id);
		retval.image = image;
		return retval;
	}

	/**
	 * The kind of shrub.
	 * @return a description of the shrub
	 */
	@Override
	public String getKind() {
		return description;
	}

	/**
	 * The default icon filename.
	 * @return the name of an image to represent the shrub.
	 */
	@Override
	public String getDefaultImage() {
		return "shrub.png";
	}

	/**
	 * We use the kind of shrub as toString() as well.
	 * @return the description of the shrub
	 */
	@Override
	public String toString() {
		return description;
	}

	/**
	 * An object is equal iff it is a Shrub with the same kind and ID.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Shrub) && equalsImpl((Shrub) obj));
	}

	/**
	 * A Shrub is equal iff it has the same kind and ID.
	 * @param obj a shrub
	 * @return whether it's equal to this one
	 */
	private boolean equalsImpl(final Shrub obj) {
		return description.equals(obj.description) && (id == obj.id);
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
	 * If we ignore ID, a fixture is equal iff it is a Shrub with the same kind.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings({"CastToConcreteClass", "InstanceofInterfaces"})
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof Shrub) && description.equals(((Shrub) fix).description);
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
	 * The plural of Shrub is Shrubs.
	 * @return a string describing all shrubs as a class
	 */
	@Override
	public String plural() {
		return "Shrubs";
	}

	/**
	 * The short description is the shrub's kind.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return description;
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * TODO: Should this vary, either loading from XML or by kind?
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		return 15;
	}
}
