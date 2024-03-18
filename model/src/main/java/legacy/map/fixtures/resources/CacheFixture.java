package legacy.map.fixtures.resources;

import legacy.map.IFixture;

/**
 * A cache (of vegetables, or a hidden treasure, or ...) on a tile.
 */
public class CacheFixture implements HarvestableFixture {
	public CacheFixture(final String kind, final String contents, final int id) {
		this.kind = kind;
		this.contents = contents;
		this.id = id;
	}

	/**
	 * What kind of things this is a cache of.
	 *
	 * TODO: Should perhaps be enumerated, so we can make images more granular.
	 */
	private final String kind;

	/**
	 * What kind of things this is a cache of.
	 *
	 * TODO: Should perhaps be enumerated, so we can make images more granular.
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * The contents of this cache.
	 *
	 * TODO: Should be turned into objects (serialized as children) as part
	 * of the general Resource framework.
	 */
	private final String contents;

	/**
	 * The contents of this cache.
	 *
	 * TODO: Should be turned into objects (serialized as children) as part
	 * of the general Resource framework.
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * ID number
	 */
	private final int id;

	/**
	 * ID number
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * The filename of an image to use as an icon for this instance.
	 */
	private String image = "";

	/**
	 * The filename of an image to use as an icon for this instance.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * Set the filename of an image to use as an icon for this instance.
	 */
	@Override
	public void setImage(final String image) {
		this.image = image;
	}

	/**
	 * Clone the object.
	 */
	@Override
	public CacheFixture copy(final CopyBehavior zero) {
		final CacheFixture retval = new CacheFixture(kind, contents, id);
		retval.setImage(image);
		return retval;
	}

	/**
	 * The filename of the image to use as the default icon for caches.
	 *
	 * TODO: Should be more granular
	 */
	@Override
	public String getDefaultImage() {
		return "cache.png";
	}

	@Override
	public String toString() {
		return "a cache of %s containing %s".formatted(kind, contents);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof final CacheFixture that) {
			return that.getId() == id &&
					kind.equals(that.getKind()) &&
					contents.equals(that.getContents());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		if (fixture instanceof final CacheFixture that) {
			return that.getKind().equals(kind) &&
					that.getContents().equals(contents);
		} else {
			return false;
		}
	}

	@Override
	public String getPlural() {
		return "Caches";
	}

	@Override
	public String getShortDescription() {
		return "a cache of " + kind;
	}

	/**
	 * TODO: Make variable (loaded from XML) or otherwise more granular?
	 */
	@Override
	public int getDC() {
		return 25;
	}

	/**
	 * Caches are *moved* to player maps when discovered.
	 */
	@Override
	public boolean subsetShouldSkip() {
		return true;
	}
}
