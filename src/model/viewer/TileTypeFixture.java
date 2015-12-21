package model.viewer;

import model.map.HasImage;
import model.map.IFixture;
import model.map.TileFixture;
import model.map.TileType;
import org.eclipse.jdt.annotation.Nullable;
import util.TypesafeLogger;

import java.util.logging.Logger;

/**
 * A fake "TileFixture" to represent the tile's terrain type, so it can be copied via
 * drag-and-drop like a fixture.
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
public final class TileTypeFixture implements TileFixture, HasImage {
	/**
	 * A logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(TileTypeFixture.class);
	/**
	 * The TileType this wraps.
	 */
	private final TileType ttype;

	/**
	 * Constructor.
	 *
	 * @param terrain The TileType this wraps.
	 */
	public TileTypeFixture(final TileType terrain) {
		ttype = terrain;
	}

	/**
	 * @param zero ignored, as this has no state other than the terrain
	 * @return a copy of this fixture
	 * @deprecated This class should only ever be in a FixtureListModel, and copying a
	 * tile's terrain type should be handled specially anyway, so this method should
	 * never
	 * be called.
	 */
	@Deprecated
	@Override
	public TileTypeFixture copy(final boolean zero) {
		LOGGER.warning("TileTypeFixture#copy() called");
		return new TileTypeFixture(ttype);
	}

	/**
	 * @param obj another TileFixture
	 * @return the result of a comparison
	 * @deprecated This class should only ever be in a FixtureListModel, so this method
	 * should never be called.
	 */
	@Deprecated
	@Override
	public int compareTo(final TileFixture obj) {
		LOGGER.warning("TileTypeFixture#compareTo() called");
		return obj.getZValue() - getZValue();
	}

	/**
	 * @return a Z-value
	 * @deprecated This class should only ever be in a FixtureListModel, so this method
	 * should never be called.
	 */
	@Deprecated
	@Override
	public int getZValue() {
		LOGGER.warning("TileTypeFixture#getZValue() called");
		return Integer.MIN_VALUE;
	}

	/**
	 * @return an "ID".
	 * @deprecated This class should only ever be in a FixtureListModel, so this method
	 * should never be called.
	 */
	@Deprecated
	@Override
	public int getID() {
		LOGGER.warning("TileTypeFixture#getID() called");
		return -1;
	}

	/**
	 * @param fix another fixture
	 * @return whether it equals this one
	 * @deprecated This class should only ever be in a FixtureListModel, so this method
	 * should never be called.
	 */
	@Override
	@Deprecated
	public boolean equalsIgnoringID(final IFixture fix) {
		LOGGER.warning("TileTypeFixture#equalsIgnoringID() called");
		return equals(fix);
	}

	/**
	 * @return the TileType this wraps.
	 */
	public TileType getTileType() {
		return ttype;
	}

	/**
	 * @param obj an object
	 * @return whether it's the same as this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof TileTypeFixture)
				                         && (((TileTypeFixture) obj).ttype == ttype));
	}

	/**
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return ttype.hashCode();
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "Terrain: " + ttype.toString();
	}

	/**
	 * There are now actually images in the repository for each tile type; they are not
	 * suitable for using as tile images, but are suitable for use in fixture lists. They
	 * are all public domain, found on either OpenClipArt or Pixabay and then adjusted to
	 * a square aspect ratio. (Except for 'mountain', which has been in the repository
	 * for
	 * a long time because it's used by the Mountain tile fixture.
	 *
	 * @return a "filename" for an image to represent the object.
	 */
	@Override
	public String getDefaultImage() {
		return ttype.toXML() + ".png";
	}

	/**
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		LOGGER.severe("TileTypeFixture#setImage() called");
	}

	/**
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return "";
	}

	/**
	 * @return a String indicating that this method shouldn't be called
	 */
	@Override
	public String plural() {
		LOGGER.warning("TileTypeFixture#plural() called");
		return "You shouldn't see this text; report this";
	}

	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		LOGGER.warning("TileTypeFixture#shortDesc() called");
		return "You shouldn't see this text; report this";
	}
}
