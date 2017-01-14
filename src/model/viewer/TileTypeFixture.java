package model.viewer;

import java.util.logging.Level;
import java.util.logging.Logger;
import model.map.HasImage;
import model.map.IFixture;
import model.map.TileFixture;
import model.map.TileType;
import org.eclipse.jdt.annotation.Nullable;
import util.TypesafeLogger;

/**
 * A fake "TileFixture" to represent the tile's terrain type, so it can be copied via
 * drag-and-drop like a fixture.
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
public final class TileTypeFixture implements TileFixture, HasImage {
	/**
	 * A logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(TileTypeFixture.class);
	/**
	 * The TileType this wraps.
	 */
	private final TileType tileType;

	/**
	 * Constructor.
	 *
	 * @param terrain The TileType this wraps.
	 */
	public TileTypeFixture(final TileType terrain) {
		tileType = terrain;
	}

	/**
	 * Clone the object.
	 * @param zero ignored, as this has no state other than the terrain
	 * @return a copy of this fixture
	 * @deprecated This class should only ever be in a FixtureListModel, and copying a
	 * tile's terrain type should be handled specially anyway, so this method should
	 * never
	 * be called.
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Deprecated
	@Override
	public TileTypeFixture copy(final boolean zero) {
		LOGGER.log(Level.WARNING, "TileTypeFixture#copy() called",
				new Exception("dummy"));
		return new TileTypeFixture(tileType);
	}

	/**
	 * Compare to another fixture. Fires a warning, since this should never be called.
	 * @param fix another TileFixture
	 * @return the result of a comparison
	 * @deprecated This class should only ever be in a FixtureListModel, so this method
	 * should never be called.
	 */
	@Deprecated
	@Override
	public int compareTo(final TileFixture fix) {
		LOGGER.warning("TileTypeFixture#compareTo() called");
		return TileFixture.super.compareTo(fix);
	}

	/**
	 * A dummy "ID number".
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
	 * Whether this equals another fixture ignoring ID. Should never be called.
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
	 * The tile type this wraps.
	 * @return the TileType this wraps.
	 */
	public TileType getTileType() {
		return tileType;
	}

	/**
	 * An object is equal iff it is a TileTypeFixture wrapping the same tile type.
	 * @param obj an object
	 * @return whether it's the same as this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof TileTypeFixture) &&
										 (((TileTypeFixture) obj).tileType == tileType));
	}

	/**
	 * Use the tile type's hash code as ours.
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return tileType.hashCode();
	}

	/**
	 * A simple toString().
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "Terrain: " + tileType;
	}

	/**
	 * There are now actually images in the repository for each tile type; they are not
	 * suitable for using as tile images, but are suitable for use in fixture lists. They
	 * are all public domain, found on either OpenClipArt or Pixabay and then adjusted to
	 * a square aspect ratio. (Except for 'mountain', which has been in the repository
	 * for a long time because it's used by the Mountain tile fixture.
	 *
	 * @return a "filename" for an image to represent the object.
	 */
	@Override
	public String getDefaultImage() {
		return tileType.toXML() + ".png";
	}

	/**
	 * We don't allow per-instance icons for these.
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return "";
	}

	/**
	 * This method should never be called, and so returns a String saying so.
	 * @return a String indicating that this method shouldn't be called
	 */
	@Override
	public String plural() {
		LOGGER.warning("TileTypeFixture#plural() called");
		return "You shouldn't see this text; report this";
	}

	/**
	 * This method should never be called, and so returns a String saying so.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		LOGGER.warning("TileTypeFixture#shortDesc() called");
		return "You shouldn't see this text; report this";
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		return 0;
	}
}
