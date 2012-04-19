package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.CacheFixture;
import util.EqualsAny;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
/**
 * A Node to represent a cache on a tile.
 * @author Jonathan Lovelace
 *
 */
@Deprecated
public class CacheNode extends AbstractFixtureNode<CacheFixture> {
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
	public CacheFixture produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		return new CacheFixture(getProperty(KIND_PROPERTY), getProperty(CONTENTS_PROPERTY));
	}
	
	/**
	 * Check a node for validity. A Cache is valid if it has "kind" and
	 * "contents" properties and no children.
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             if the node is invalid.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException("cache", iterator().next()
					.toString(), getLine());
		} else if (hasProperty(KIND_PROPERTY)) {
			if (!hasProperty(CONTENTS_PROPERTY)) {
				throw new MissingParameterException("cache", CONTENTS_PROPERTY, getLine());
			}
		} else {
			throw new MissingParameterException("cache", KIND_PROPERTY, getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, CONTENTS_PROPERTY);
	}
	/**
	 * @return a String representation of the node
	 */
	@Override
	public String toString() {
		return "CacheNode";
	}
}
