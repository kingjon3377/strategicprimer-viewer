package model.map.fixtures.resources;

import model.map.HasImage;
import model.map.HasKind;
import model.map.IFixture;
import model.map.TileFixture;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A cache (of vegetables, or a hidden treasure, or ...) on a tile.
 *
 * @author Jonathan Lovelace
 *
 */
public class CacheFixture implements HarvestableFixture, HasImage, HasKind {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * What kind of things this is a cache of.
	 *
	 * TODO: Should perhaps be enumerated, so we can make images more granular.
	 */
	private String kind;
	/**
	 * The contents of this cache. TODO: Should be turned into objects
	 * (serialized as children) as part of the general Resource framework.
	 */
	private final String contents;

	/**
	 * Constructor.
	 *
	 * @param category what kind of things this is a cache of
	 * @param cont what this cache contains
	 * @param idNum the ID number.
	 */
	public CacheFixture(final String category, final String cont,
			final int idNum) {
		super();
		kind = category;
		contents = cont;
		id = idNum;
	}

	/**
	 * @return what kind of things this is a cache of
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * @return the contents of this cache
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * TODO: should be more granular.
	 *
	 * @return the name of an image to represent the cache
	 */
	@Override
	public String getDefaultImage() {
		return "cache.png";
	}

	/**
	 * @return a string representation of the cache
	 */
	@Override
	public String toString() {
		return new StringBuilder(24 + kind.length() + contents.length())
				.append("a cache of ").append(kind).append(" containing ")
				.append(contents).toString();
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 22;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj
				|| (obj instanceof CacheFixture
						&& kind.equals(((CacheFixture) obj).kind)
						&& contents.equals(((CacheFixture) obj).contents) && id == ((TileFixture) obj)
						.getID());
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
		return fix instanceof CacheFixture
				&& kind.equals(((CacheFixture) fix).kind)
				&& contents.equals(((CacheFixture) fix).contents);
	}

	/**
	 * @param nKind the new kind
	 */
	@Override
	public final void setKind(final String nKind) {
		kind = nKind;
	}

	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

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
	 * @return a string describing all caches as a class
	 */
	@Override
	public String plural() {
		return "Caches";
	}
}
