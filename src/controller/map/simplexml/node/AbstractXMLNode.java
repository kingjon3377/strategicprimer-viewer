package controller.map.simplexml.node;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import controller.map.NeedsExtraCanonicalization;
import controller.map.SPFormatException;

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
	private final Set<AbstractXMLNode> children = new HashSet<AbstractXMLNode>();
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
	 * 
	 * @return an iterator over the children.
	 */
	@Override
	public final Iterator<AbstractXMLNode> iterator() {
		return children.iterator();
	}

	/**
	 * Check that the data is legal---no tiles outside the map, for example.
	 * 
	 * 
	 * @throws SPFormatException
	 *             if the data isn't legal.
	 */
	public abstract void checkNode() throws SPFormatException;

	/**
	 * 
	 * @return the line the tag was on
	 */
	public int getLine() {
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
	 * 
	 * 
	 * @throws SPFormatException
	 *             on format errors uncovered in this process
	 */
	public final void canonicalize() throws SPFormatException {
		final List<AbstractXMLNode> nodesToRemove = new LinkedList<AbstractXMLNode>();
		final List<AbstractXMLNode> nodesToAdd = new LinkedList<AbstractXMLNode>();
		for (final AbstractXMLNode node : children) {
			if (node instanceof NeedsExtraCanonicalization) {
				((NeedsExtraCanonicalization) node).canonicalizeImpl();
			}
			node.canonicalize();
			if (node instanceof SkippableNode) {
				nodesToAdd.addAll(node.children);
				nodesToRemove.add(node);
			}
		}
		children.removeAll(nodesToRemove);
		children.addAll(nodesToAdd);
	}

	/**
	 * Move all children to another Node.
	 * 
	 * @param dest
	 *            the destination Node.
	 */
	protected final void moveChildrenTo(final AbstractXMLNode dest) {
		dest.children.addAll(children);
		children.clear();
	}
}
