package model.report;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
/**
 * A superclass for report-nodes.
 * @author Jonathan Lovelace
 *
 */
public abstract class AbstractReportNode extends DefaultMutableTreeNode implements Comparable<AbstractReportNode> {
	/**
	 * Constructor.
	 * @param txt the (header) text.
	 */
	protected AbstractReportNode(final String txt) {
		super(txt);
		setText(txt);
	}
	/**
	 * @return the HTML representation of the node.
	 */
	public abstract String produce();
	/**
	 * @return an approximation of how large the HTML produced by this node will be.
	 */
	public abstract int size();
	/**
	 * The (usually header) text. May be empty, but not null.
	 */
	private String text;
	/**
	 * @return the text of the node, usually the header.
	 */
	public final String getText() {
		return text;
	}
	/**
	 * @param txt the new text for the node
	 */
	public final void setText(final String txt) {
		text = txt;
		setUserObject(text);
	}
	/**
	 * @param obj an object to compare to.
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final AbstractReportNode obj) {
		return produce().compareTo(obj.produce());
	}
	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof AbstractReportNode && equalsImpl((AbstractReportNode) obj));
	}
	/**
	 * @param obj a node
	 * @return whether it's equal to this one.
	 */
	protected abstract boolean equalsImpl(final AbstractReportNode obj);
	/**
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return hashCodeImpl();
	}
	/**
	 * @return a hash code for the object
	 */
	protected abstract int hashCodeImpl();
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return getText();
	}
	/**
	 * Add a node. Do nothing if null, rather than crashing.
	 * @param node the node to add
	 */
	@Override
	public void add(final MutableTreeNode node) {
		if (node != null) {
			super.add(node);
		}
	}
}
