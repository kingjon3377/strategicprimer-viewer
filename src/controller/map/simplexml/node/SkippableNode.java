package controller.map.simplexml.node;

import model.viewer.PlayerCollection;
import controller.map.simplexml.AbstractChildNode;
import controller.map.simplexml.SPFormatException;

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
	 * Throws an exception, because you should move all children from this to
	 * its parent and then remove this node instead.
	 * @param players ignored
	 * @return nothing
	 * @throws SPFormatException
	 *             never
	 */
	@Deprecated
	@Override
	public SkippableNode produce(final PlayerCollection players) throws SPFormatException {
		throw new IllegalStateException("SkippableNodes should be skipped.");
	}

	/**
	 * Always throw an exception (but not this one), because the existence of a
	 * SkippableNode in the node-tree at the point that we're checking the data
	 * for validity is a sign that something's wrong.
	 * 
	 * @throws SPFormatException never
	 */
	@Override
	public void checkNode() throws SPFormatException {
		throw new IllegalStateException("SkippableNodes should be skipped.");
	}

}
