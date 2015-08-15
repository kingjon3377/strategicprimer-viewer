package model.viewer;

import org.eclipse.jdt.annotation.Nullable;

import model.map.HasImage;
import model.map.IFixture;
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
	 * The name of an image to use for this particular fixture.
	 *
	 * FIXME: Maybe don't allow this? It doesn't really make sense for a
	 * TileTypeFixture.
	 */
	private String image = "";

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
	 * @return a copy of this fixture
	 * @param zero
	 *            ignored, as this has no state other than the terrain
	 * @deprecated This class should only ever be in a FixtureListModel, and
	 *             copying a tile's terrain type should be handled specially
	 *             anyway, so this method should never be called.
	 */
	@Deprecated
	@Override
	public TileTypeFixture copy(final boolean zero) {
		TileTypeFixture retval = new TileTypeFixture(ttype);
		retval.setImage(image);
		return retval;
	}
	/**
	 * @param obj another TileFixture
	 * @return the result of a comparison
	 * @deprecated This class should only ever be in a FixtureListModel, so this
	 *             method should never be called.
	 */
	@Deprecated
	@Override
	public int compareTo(@Nullable final TileFixture obj) {
		if (obj == null) {
			throw new IllegalArgumentException("Compared to null fixture");
		}
		return obj.getZValue() - getZValue();
	}

	/**
	 * @return a Z-value
	 * @deprecated This class should only ever be in a FixtureListModel, so this
	 *             method should never be called.
	 */
	@Deprecated
	@Override
	public int getZValue() {
		return Integer.MIN_VALUE;
	}

	/**
	 * @return an "ID".
	 * @deprecated This class should only ever be in a FixtureListModel, so this
	 *             method should never be called.
	 */
	@Deprecated
	@Override
	public int getID() {
		return -1;
	}

	/**
	 * @param fix another fixture
	 * @return whether it equals this one
	 * @deprecated This class should only ever be in a FixtureListModel, so this
	 *             method should never be called.
	 */
	@Override
	@Deprecated
	public boolean equalsIgnoringID(final IFixture fix) {
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
		return this == obj || obj instanceof TileTypeFixture
				&& ((TileTypeFixture) obj).ttype == ttype;
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
	 * There are now actually images in the repository for each tile type; they
	 * are not suitable for using as tile images, but are suitable for use in
	 * fixture lists. They are all public domain, found on either OpenClipArt or
	 * Pixabay and then adjusted to a square aspect ratio. (Except for
	 * 'mountain', which has been in the repository for a long time because it's
	 * used by the Mountain tile fixture.
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
	 * @return a String indicating that this method shouldn't be called
	 */
	@Override
	public String plural() {
		return "You shouldn't see this text; report this";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "You shouldn't see this text; report this";
	}
}
