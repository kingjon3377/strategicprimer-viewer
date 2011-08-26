package controller.map.simplexml;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A class representing an XML tag and its descendants---except not necessarily
 * a node actually *in* the XML tree.
 * 
 * @author Jonathan Lovelace
 * 
 */
public abstract class AbstractXMLNode implements Iterable<AbstractXMLNode> {

	/**
	 * The children of this node.
	 */
	private final Set<AbstractXMLNode> children = new CopyOnWriteArraySet<AbstractXMLNode>();
	/**
	 * The line number the tag was on.
	 */
	private int line;

	/**
	 * Add a child.
	 * 
	 * @param child
	 *            the child to add.
	 */
	public final void addChild(final AbstractXMLNode child) {
		children.add(child);
	}

	/**
	 * @return an iterator over the children.
	 */
	@Override
	public final Iterator<AbstractXMLNode> iterator() {
		return children.iterator();
	}

	/**
	 * Check that the data is legal---no tiles outside the map, for example.
	 * 
	 * @throws SPFormatException
	 *             if the data isn't legal.
	 */
	public abstract void checkNode() throws SPFormatException;

	/**
	 * @return the line the tag was on
	 */
	protected int getLine() {
		return line;
	}

	/**
	 * @param tagLine
	 *            the line the tag is on
	 */
	protected void setLine(final int tagLine) {
		line = tagLine;
	}
	/**
	 * Canonicalize the tree. For now, that just means removing each
	 * SkippableNode after adding its contents to its parent, but any other
	 * actions that need to be taken after setting up the tree but before
	 * validity-checking should go here. We do this in a separate method because
	 * validity-checking should be side-effect-free.
	 */
	public final void canonicalize() {
		for (AbstractXMLNode node : children) {
			node.canonicalize();
			if (node instanceof SkippableNode) {
				children.addAll(node.children);
				children.remove(node);
			} 
		}
	}
}
