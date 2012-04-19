package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * A cache (of vegetables, or a hidden treasure, or ...) on a tile.
 * @author Jonathan Lovelace
 *
 */
public class CacheFixture implements TileFixture, HasImage {
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
	 * @param category what kind of things this is a cache of
	 * @param cont what this cache contains
	 */
	public CacheFixture(final String category, final String cont) {
		kind = category;
		contents = cont;
	}
	/**
	 * @return what kind of things this is a cache of
	 */
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
	 */
	@Override
	public String toXML() {
		return new StringBuilder("<cache kind=\"").append(kind)
				.append("\" contents=\"").append(contents).append("\" />")
				.toString();
	}
	/**
	 * TODO: should be more granular.
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
		return obj instanceof CacheFixture
				&& kind.equals(((CacheFixture) obj).kind)
				&& contents.equals(((CacheFixture) obj).contents);
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return kind.hashCode() | contents.hashCode();
	}
	/**
	 * @param fix
	 *            A TileFixture to compare to
	 * 
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}
}
