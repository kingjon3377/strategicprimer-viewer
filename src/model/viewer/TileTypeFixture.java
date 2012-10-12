package model.viewer;

import model.map.HasImage;
import model.map.TileFixture;
import model.map.TileType;

/**
 * A fake "TileFixture" to represent the tile's terrain type, so it can be
 * copied via drag-and-drop like a fixture.
 *
 * @author Jonathan Lovelace
 *
 */
public class TileTypeFixture implements TileFixture, HasImage {
	/**
	 * Constructor.
	 * @param terrain The TileType this wraps.
	 */
	public TileTypeFixture(final TileType terrain) {
		ttype = terrain;
	}
	/**
	 * The TileType this wraps.
	 */
	private final TileType ttype;
	/**
	 * @return the empty string
	 * @deprecated This class should only ever be in a FixtureListModel, so this method should never be called.
	 */
	@Deprecated
	@Override
	public String getFile() {
		return "";
	}
	/**
	 * @param file ignored
	 * @deprecated This class should only ever be in a FixtureListModel, so this method should never be called.
	 */
	@Deprecated
	@Override
	public void setFile(final String file) {
		// Do nothing
	}
	/**
	 * @param obj another TileFixture
	 * @return the result of a comparison
	 * @deprecated This class should only ever be in a FixtureListModel, so this method should never be called.
	 */
	@Deprecated
	@Override
	public int compareTo(final TileFixture obj) {
		return obj.getZValue() - getZValue();
	}
	/**
	 * @return a Z-value
	 * @deprecated This class should only ever be in a FixtureListModel, so this method should never be called.
	 */
	@Deprecated
	@Override
	public int getZValue() {
		return Integer.MIN_VALUE;
	}
	/**
	 * @return an "ID".
	 * @deprecated This class should only ever be in a FixtureListModel, so this method should never be called.
	 */
	@Deprecated
	@Override
	public int getID() {
		return -1;
	}
	/**
	 * @param fix another fixture
	 * @return whether it equals this one
	 * @deprecated This class should only ever be in a FixtureListModel, so this method should never be called.
	 */
	@Override
	@Deprecated
	public boolean equalsIgnoringID(final TileFixture fix) {
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
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof TileTypeFixture && ((TileTypeFixture) obj).ttype == ttype);
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
	 * @return a "filename" for an image to represent the object.
	 */
	@Override
	public String getImage() {
		return ttype.toXML() + ".png";
	}
}
