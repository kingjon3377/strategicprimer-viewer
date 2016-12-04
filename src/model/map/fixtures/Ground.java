package model.map.fixtures;

import model.map.HasMutableImage;
import model.map.HasMutableKind;
import model.map.IFixture;
import model.map.TileFixture;
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
public class Ground implements TileFixture, HasMutableImage, HasMutableKind {
	/**
	 * The kind of ground.
	 */
	private String kind;
	/**
	 * Whether the ground is exposed.
	 */
	private boolean exposed;

	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Constructor.
	 *
	 * @param desc a description of the ground (the type of rock)
	 * @param exp  whether it's exposed. (If not, the tile should also include a grass or
	 *             forest Fixture ...)
	 */
	public Ground(final String desc, final boolean exp) {
		kind = desc;
		exposed = exp;
	}

	/**
	 * @param zero ignored, as there's no sensitive data
	 * @return a copy of this ground
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Ground copy(final boolean zero) {
		final Ground retval = new Ground(kind, exposed);
		retval.image = image;
		return retval;
	}

	/**
	 * @return whether the ground is exposed
	 */
	public boolean isExposed() {
		return exposed;
	}

	/**
	 * @param exp whether the ground is exposed
	 */
	public final void setExposed(final boolean exp) {
		exposed = exp;
	}

	/**
	 * @return a description of the ground
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
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) ||
					   ((obj instanceof Ground) && kind.equals(((Ground) obj).kind) &&
								(exposed == ((Ground) obj).exposed));
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		if (exposed) {
			return kind.hashCode() << 1;
		} else {
			return kind.hashCode();
		}
	}

	/**
	 * @return a String representation of the Ground.
	 */
	@Override
	public String toString() {
		if (exposed) {
			return "Exposed ground of kind " + kind;
		} else {
			return "Unexposed ground of kind " + kind;
		}
	}

	/**
	 * TODO: make this different between instances.
	 *
	 * @return an ID number.
	 */
	@Override
	public int getID() {
		return -1;
	}

	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return equals(fix);
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
	 * @return a string describing all Ground as a class
	 */
	@Override
	public String plural() {
		return "Ground";
	}

	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}
}
