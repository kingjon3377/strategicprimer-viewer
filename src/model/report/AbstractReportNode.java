package model.report;

import javax.swing.tree.DefaultMutableTreeNode;
/**
 * A superclass for report-nodes.
 * @author Jonathan Lovelace
 *
 */
public abstract class AbstractReportNode extends DefaultMutableTreeNode implements Comparable<AbstractReportNode> {
	/**
	 * @return the HTML representation of the node.
	 */
	public abstract String produce();
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
				|| (obj instanceof AbstractReportNode && produce().equals(
						((AbstractReportNode) obj).produce()));
	}
	/**
	 * Must be implemented by subclasses.
	 * @return nothing; always throws
	 */
	@Override
	public int hashCode() {
		throw new IllegalStateException("Must be implemented by subclass");
	}
}
