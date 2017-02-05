package model.map.fixtures;

import model.map.HasMutableImage;
import model.map.IFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A TileFixture to represent the basic rock beneath the tile, possibly exposed.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class Ground implements MineralFixture, HasMutableImage {
	/**
	 * The kind of ground.
	 */
	private final String kind;
	/**
	 * Whether the ground is exposed.
	 */
	private boolean exposed;

	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * The ID number.
	 */
	private int id;

	/**
	 * Constructor.
	 *
	 * @param idNum the ID number
	 * @param desc a description of the ground (the type of rock)
	 * @param exp  whether it's exposed. (If not, the tile should also include a grass or
	 *             forest Fixture ...)
	 */
	public Ground(final int idNum, final String desc, final boolean exp) {
		id = idNum;
		kind = desc;
		exposed = exp;
	}

	/**
	 * Clone the Ground.
	 * @param zero ignored, as there's no sensitive data
	 * @return a copy of this ground
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Ground copy(final boolean zero) {
		final Ground retval = new Ground(id, kind, exposed);
		retval.image = image;
		return retval;
	}

	/**
	 * Whether the ground is exposed.
	 * @return whether the ground is exposed
	 */
	public boolean isExposed() {
		return exposed;
	}

	/**
	 * Set whether the ground is exposed.
	 * @param exp whether the ground is exposed
	 */
	public final void setExposed(final boolean exp) {
		exposed = exp;
	}

	/**
	 * The kind of ground.
	 * @return a description of the ground
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * The default icon filename. Depends on whether the ground is exposed or not.
	 * @return the name of an image to represent the ground.
	 */
	@Override
	public String getDefaultImage() {
		if (exposed) {
			return "expground.png";
		} else {
			return "blank.png";
		}
	}

	/**
	 * An object is equal if it is Ground of the same kind, either both or neither
	 * are exposed, and it has the same ID.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) ||
					   ((obj instanceof Ground) && kind.equals(((Ground) obj).kind) &&
								(exposed == ((Ground) obj).exposed) && id == ((Ground) obj).id);
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
	 * Delegates to shortDesc().
	 * @return a String representation of the Ground.
	 */
	@Override
	public String toString() {
		return shortDesc() + ", ID #" + id;
	}

	/**
	 * The ID number.
	 *
	 * @return an ID number.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * Set the ID for the object.
	 * @param idNum the new ID number
	 */
	public void setID(final int idNum) {
		id = idNum;
	}

	/**
	 * A fixture is equal, ignoring ID, if it is a Ground with equal kind and either
	 * both or neither are exposed.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings({"InstanceofInterfaces", "ObjectEquality"})
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (this == fix) ||
					   ((fix instanceof Ground) && kind.equals(((Ground) fix).kind) &&
								(exposed == ((Ground) fix).exposed));
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
	 * The plural of Ground is Ground, for our purposes.
	 * @return a string describing all Ground as a class
	 */
	@Override
	public String plural() {
		return "Ground";
	}

	/**
	 * A short description of the fixture.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		if (exposed) {
			return "Exposed ground of kind " + kind;
		} else {
			return "Unexposed ground of kind " + kind;
		}
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		if (exposed) {
			return 10;
		} else {
			return 40;
		}
	}
}
