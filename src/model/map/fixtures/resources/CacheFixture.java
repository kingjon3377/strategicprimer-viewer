package model.map.fixtures.resources;

import model.map.HasImage;
import model.map.HasKind;
import model.map.TileFixture;
import model.map.XMLWritableImpl;

/**
 * A cache (of vegetables, or a hidden treasure, or ...) on a tile.
 *
 * @author Jonathan Lovelace
 *
 */
public class CacheFixture extends XMLWritableImpl implements
		HarvestableFixture, HasImage, HasKind {
	/**
	 * What kind of things this is a cache of. TODO: Should perhaps be
	 * enumerated, so we can make images more granular.
	 */
	private final String kind;
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
	 * @param fileName the file this was loaded from
	 */
	public CacheFixture(final String category, final String cont,
			final int idNum, final String fileName) {
		super(fileName);
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
	 * @return an XML representation of the cache.
	 * @deprecated Replaced by SPIntermediateRepresentation-based output
	 */
	@Override
	@Deprecated
	public String toXML() {
		return new StringBuilder("<cache kind=\"").append(kind)
				.append("\" contents=\"").append(contents).append("\" id=\"")
				.append(id).append("\" />").toString();
	}

	/**
	 * TODO: should be more granular.
	 *
	 * @return the name of an image to represent the cache
	 */
	@Override
	public String getImage() {
		return "cache.png";
	}

	/**
	 * @return a string representation of the cache
	 */
	@Override
	public String toString() {
		return new StringBuilder("a cache of ").append(kind)
				.append(" containing ").append(contents).toString();
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
	public boolean equals(final Object obj) {
		return this == obj || (obj instanceof CacheFixture
				&& kind.equals(((CacheFixture) obj).kind)
				&& contents.equals(((CacheFixture) obj).contents)
				&& id == ((TileFixture) obj).getID());
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
	public boolean equalsIgnoringID(final TileFixture fix) {
		return fix instanceof CacheFixture
				&& kind.equals(((CacheFixture) fix).kind)
				&& contents.equals(((CacheFixture) fix).contents);
	}

	/**
	 * @return a clone of this object
	 */
	@Override
	public TileFixture deepCopy() {
		return new CacheFixture(getKind(), getContents(), getID(), getFile());
	}
}
