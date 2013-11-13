package model.report;

import javax.swing.tree.MutableTreeNode;

/**
 * An interface for report nodes.
 * @author Jonathan Lovelace
 */
public interface IReportNode extends Comparable<IReportNode>, MutableTreeNode {
	/**
	 * @return the HTML representation of the node.
	 */
	String produce();
	/**
	 * @param builder a string builder
	 * @return that builder, with an HTML representation of the node added.
	 */
	StringBuilder produce(final StringBuilder builder);
	/**
	 * @return an approximation of how large the HTML produced by this node will
	 *         be.
	 */
	int size();
	/**
	 * @return the text of the node, usually the header.
	 */
	String getText();
	/**
	 * @param txt the new text for the node
	 */
	void setText(final String txt);
}
