package controller.map.simplexml;

import java.util.Iterator;

/**
 * A node at the root of the hierarchy. Its only child should be a MapNode.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class RootNode extends AbstractXMLNode {
	/**
	 * Check whether the tree is valid. Since we can't check whether it has more
	 * than one child, we only verify that it has at least one, which is a
	 * MapNode.
	 * 
	 * @throws SPFormatException
	 *             if it isn't.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (!iterator().hasNext() || !(iterator().next() instanceof MapNode)) {
			throw new SPFormatException("<map> must be top-level tag", 0);
		}
	}

	/**
	 * @return the map node, which should be our only child.
	 * @throws SPFormatException
	 *             if we don't have a child or it isn't a MapNode.
	 */
	public MapNode getMapNode() throws SPFormatException {
		final Iterator<AbstractXMLNode> iterator = iterator();
		if (iterator.hasNext()) {
			final AbstractXMLNode child = iterator.next();
			if (child instanceof MapNode) {
				return (MapNode) child;
			} else {
				throw new SPFormatException("First top-level tag isn't <map>", 0);
			}
		} else {
			throw new SPFormatException("No top-level tag", 0);
		}
	}
}
