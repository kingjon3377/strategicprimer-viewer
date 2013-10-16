package model.report;

import javax.swing.tree.TreeNode;

/**
 * A node representing a list.
 *
 * @author Jonathan Lovelace
 */
public class ListReportNode extends AbstractReportNode {
	/**
	 * @param text the header text
	 */
	public ListReportNode(final String text) {
		super(text);
	}

	/**
	 * @return the HTML representation of the node.
	 */
	@Override
	public String produce() {
		// Assume each child is half a K.
		final StringBuilder builder = new StringBuilder(getText().length() + 10
				+ getChildCount() * 512).append(getText());
		builder.append("\n<ul>\n");
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof AbstractReportNode) {
				builder.append("<li>");
				builder.append(((AbstractReportNode) child).produce());
				builder.append("</li>\n");
			}
		}
		builder.append("</ul>\n");
		return builder.toString();
	}

	/**
	 * @param builder a StringBuilder
	 * @return it, with this node's HTML representation appended.
	 */
	@Override
	public StringBuilder produce(final StringBuilder builder) {
		builder.append(getText());
		builder.append("\n<ul>\n");
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof AbstractReportNode) {
				builder.append("<li>");
				((AbstractReportNode) child).produce(builder);
				builder.append("</li>\n");
			}
		}
		builder.append("</ul>\n");
		return builder;
	}

	/**
	 * @return approximately how long the HTML representation of this node will
	 *         be.
	 */
	@Override
	public int size() {
		int retval = 16 + getText().length();
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof AbstractReportNode) {
				retval += ((AbstractReportNode) child).size() + 10;
			}
		}
		return retval;
	}

	/**
	 * @param obj a node
	 * @return whether it equals this one
	 */
	@Override
	protected boolean equalsImpl(final AbstractReportNode obj) {
		return obj instanceof ListReportNode && getText().equals(obj.getText())
				&& children().equals(obj.children());
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	protected int hashCodeImpl() {
		return getText().hashCode() /* | children().hashCode() */;
	}
}
