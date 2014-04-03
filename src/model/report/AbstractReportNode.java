package model.report;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A superclass for report-nodes.
 *
 * @author Jonathan Lovelace
 *
 */
public abstract class AbstractReportNode extends DefaultMutableTreeNode
		implements IReportNode {
	/**
	 * The (usually header) text. May be empty, but not null.
	 */
	private String text;

	/**
	 * Constructor.
	 *
	 * @param txt the (header) text.
	 */
	protected AbstractReportNode(final String txt) {
		super(txt);
		text = txt;
		setText(txt);
	}

	/**
	 * @return the HTML representation of the node.
	 */
	@Override
	public abstract String produce();

	/**
	 * @param builder a string builder
	 * @return that builder, with an HTML representation of the node added.
	 */
	@Override
	public abstract StringBuilder produce(final StringBuilder builder);

	/**
	 * @return an approximation of how large the HTML produced by this node will
	 *         be.
	 */
	@Override
	public abstract int size();

	/**
	 * @return the text of the node, usually the header.
	 */
	@Override
	public final String getText() {
		return text;
	}

	/**
	 * @param txt the new text for the node
	 */
	@Override
	public final void setText(final String txt) {
		text = txt;
		setUserObject(text);
	}

	/**
	 * @param obj an object to compare to.
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final IReportNode obj) {
		return produce().compareTo(obj.produce());
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof IReportNode
				&& equalsImpl((IReportNode) obj);
	}

	/**
	 * @param obj a node
	 * @return whether it's equal to this one.
	 */
	protected abstract boolean equalsImpl(final IReportNode obj);

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
	 *
	 * @param node the node to add
	 */
	@Override
	public void add(@Nullable final MutableTreeNode node) {
		if (node != null && !(node instanceof EmptyReportNode)) {
			super.add(node);
		}
	}
}
