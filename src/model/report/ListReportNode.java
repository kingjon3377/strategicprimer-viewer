package model.report;

import javax.swing.tree.TreeNode;

/**
 * A node representing a list.
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
	 * @param obj a node
	 * @return whether it equals this one
	 */
	@Override
	protected boolean equalsImpl(final AbstractReportNode obj) {
		return obj instanceof ListReportNode && getText().equals(obj.getText()) && children().equals(obj.children());
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	protected int hashCodeImpl() {
		return getText().hashCode() /*| children().hashCode()*/;
	}
}
