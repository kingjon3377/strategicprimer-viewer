package model.map.fixtures;

import model.map.HasImage;
import model.map.HasKind;
import model.map.IFixture;
import model.map.TileFixture;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A TileFixture to represent the basic rock beneath the tile, possibly exposed.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
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
public class Ground implements TileFixture, HasImage, HasKind {
	/**
	 * The kind of ground.
	 */
	private String kind;
	/**
	 * Whether the ground is exposed.
	 */
	private final boolean exposed;

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
	 * @return a description of the grond
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * @return the name of an image to represent the ground.
	 */
	@Override
	public String getDefaultImage() {
		if (exposed) {
			return "expground.png"; // NOPMD
		} else {
			return "blank.png";
		}
	}

	/**
	 * TODO: Should perhaps depend on whether it's exposed or not.
	 *
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 0;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Ground)
				                         && kind.equals(((Ground) obj).kind)
				                         && (exposed == ((Ground) obj).exposed));
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		if (exposed) {
			return kind.hashCode() << 1; // NOPMD
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
			return "Exposed ground of kind " + kind; // NOPMD
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
