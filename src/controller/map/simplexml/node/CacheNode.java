package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.resources.CacheFixture;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to represent a cache on a tile.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
 */
@Deprecated
public class CacheNode extends AbstractFixtureNode<CacheFixture> {
	/**
	 * The tag.
	 */
	private static final String TAG = "cache";
	/**
	 * The name of the property saying what's in the cache.
	 */
	private static final String CONTENTS_PROPERTY = "contents";
	/**
	 * The name of the property saying what kind of thing is in the cache.
	 */
	private static final String KIND_PROPERTY = "kind";

	/**
	 * Constructor.
	 */
	public CacheNode() {
		super(CacheFixture.class);
	}

	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the cache this Node represents
	 * @throws SPFormatException if missing essential properties.
	 */
	@Override
	public CacheFixture produce(final PlayerCollection players,
			final Warning warner) throws SPFormatException {
		final CacheFixture fix = new CacheFixture(getProperty(KIND_PROPERTY),
				getProperty(CONTENTS_PROPERTY),
				Integer.parseInt(getProperty("id")), getProperty("file"));
		return fix;
	}

	/**
	 * Check a node for validity. A Cache is valid if it has "kind" and
	 * "contents" properties and no children.
	 *
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if the node is invalid.
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren(TAG);
		demandProperty(TAG, KIND_PROPERTY, warner, false, false);
		demandProperty(TAG, CONTENTS_PROPERTY, warner, false, false);
		registerOrCreateID(TAG, idFactory, warner);
	}

	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, CONTENTS_PROPERTY,
				"id");
	}

	/**
	 * @return a String representation of the node
	 */
	@Override
	public String toString() {
		return "CacheNode";
	}
}
