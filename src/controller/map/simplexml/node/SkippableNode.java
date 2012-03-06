package controller.map.simplexml.node;

import model.map.PlayerCollection;
import util.Warning;
import controller.map.SPFormatException;

/**
 * A Node for tags we'd rather skip. This is an AbstractChildNode because
 * NodeFactory needs to be able to return one; the type parameter doesn't matter
 * because produce() should never get called on a SkippableNode anyway. Instead,
 * use the iterator it contains to move all its children to its parent, then
 * remove it from its parent.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class SkippableNode extends AbstractChildNode<SkippableNode> {
	/**
	 * Required for reflection.
	 */
	public SkippableNode() {
		super(SkippableNode.class);
	}
	/**
	 * Constructor. Warns if the tag isn't the one kind we routinely expect to
	 * see in every map; tags we expect to see in future map formats are added
	 * as SkippableNodes too, but if we see them in a map we should warn about
	 * that.
	 * 
	 * @param tag
	 *            the text of the tag name.
	 * @param line
	 *            the line of the file the tag occurs.
	 */
	public SkippableNode(final String tag, final int line) {
		super(SkippableNode.class);
		if (!"row".equals(tag)) {
			Warning.warn(new SPFormatException("Unexpected tag " + tag
					+ ": probably a more recent map format than viewer.", line));
		}
	}

	/**
	 * Throws an exception, because you should move all children from this to
	 * its parent and then remove this node instead.
	 * 
	 * @param players
	 *            ignored
	 * @return nothing
	 * @throws SPFormatException
	 *             never
	 */
	@Deprecated
	@Override
	public SkippableNode produce(final PlayerCollection players)
			throws SPFormatException {
		throw new IllegalStateException("SkippableNodes should be skipped.");
	}

	/**
	 * Always throw an exception (but not this one), because the existence of a
	 * SkippableNode in the node-tree at the point that we're checking the data
	 * for validity is a sign that something's wrong.
	 * 
	 * 
	 * @throws SPFormatException
	 *             never
	 */
	@Override
	public void checkNode() throws SPFormatException {
		throw new IllegalStateException("SkippableNodes should be skipped.");
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SkippableNode";
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return "index".equals(property);
	}

}
