package controller.map.simplexml.node;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A class representing an XML tag and its descendants---except not necessarily
 * a node actually *in* the XML tree.
 * 
 * @author Jonathan Lovelace
 * 
 */
@Deprecated
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
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * 
	 * @throws SPFormatException
	 *             if the data isn't legal.
	 */
	public abstract void checkNode(Warning warner, IDFactory idFactory) throws SPFormatException;

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
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             on format errors uncovered in this process
	 */
	public final void canonicalize(final Warning warner) throws SPFormatException {
		final List<AbstractXMLNode> nodesToAdd = new LinkedList<AbstractXMLNode>();
		final List<AbstractXMLNode> nodesToKeep = new LinkedList<AbstractXMLNode>();
		for (final AbstractXMLNode node : children) {
			node.canonicalize(warner);
			if (node instanceof SkippableNode) {
				nodesToAdd.addAll(node.children);
			} else {
				nodesToKeep.add(node);
			}
		}
		children.clear();
		children.addAll(nodesToKeep);
		children.addAll(nodesToAdd);
	}

//	/**
//	 * @param obj an object
//	 * @return whether it's equal to this one
//	 */
//	@Override
//	public boolean equals(final Object obj) {
//		return (obj instanceof AbstractXMLNode
//				&& getClass().equals(obj.getClass())
//				&& children.equals(((AbstractXMLNode) obj).children) && line == ((AbstractXMLNode) obj).line);
//	}
//	/**
//	 * @return a hash value for the object
//	 */
//	@Override
//	public int hashCode() {
//		return children.hashCode() | getClass().hashCode() | line;
//	}
	/**
	 * Pretty-print the node.
	 * @param depth how deep we are in the tree
	 * @return the pretty-printed version of the node
	 */
	public final String prettyPrint(final int depth) {
		final StringBuilder sbuild = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			sbuild.append('\t');
		}
		sbuild.append(toString());
		sbuild.append('\n');
		for (AbstractXMLNode node : children) {
			sbuild.append(node.prettyPrint(depth + 1));
		}
		return sbuild.toString();
	}
	
}
