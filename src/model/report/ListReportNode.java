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
		setText(text);
	}
	/**
	 * @return the HTML representation of the node.
	 */
	@Override
	public String produce() {
		final StringBuilder builder = new StringBuilder(getText());
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
}
