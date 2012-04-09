package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.CacheFixture;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
/**
 * A Node to represent a cache on a tile.
 * @author Jonathan Lovelace
 *
 */
public class CacheNode extends AbstractFixtureNode<CacheFixture> {
	/**
	 * Constructor.
	 */
	public CacheNode() {
		super(CacheFixture.class);
	}
	/**
	 * @param players ignored
	 * @return the cache this Node represents
	 * @throws SPFormatException if missing essential properties.
	 */
	@Override
	public CacheFixture produce(final PlayerCollection players)
			throws SPFormatException {
		return new CacheFixture(getProperty("kind"), getProperty("contents"));
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
			throw new SPFormatException("Cache shouldn't have children", getLine());
		} else if (!hasProperty("kind") || !hasProperty("contents")) {
			throw new SPFormatException(
					"Cache must have \"kind\" and \"contents\" properties",
					getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, "kind", "contents");
	}
	/**
	 * @return a String representation of the node
	 */
	@Override
	public String toString() {
		return "CacheNode";
	}
}
