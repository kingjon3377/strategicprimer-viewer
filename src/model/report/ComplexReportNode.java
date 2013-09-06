package model.report;

import javax.swing.tree.TreeNode;

/**
 * A node for cases slightly more complex than a {@link SimpleReportNode}
 * covers: the text here isn't really a header, and no wrapping children as a
 * list, but we can *have* children. For example, when a report needs to have
 * multiple lists, each with its own header.
 *
 * @author Jonathan Lovelace
 *
 */
public class ComplexReportNode extends AbstractReportNode {
	/**
	 * @param text the main text of the node
	 */
	public ComplexReportNode(final String text) {
		setText(text);
	}
	/**
	 * @return an HTML representation of the node.
	 */
	@Override
	public String produce() {
		final StringBuilder builder = new StringBuilder(getText());
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode node = getChildAt(i);
			if (node instanceof AbstractReportNode) {
				builder.append(((AbstractReportNode) node).produce());
			}
		}
		return builder.toString();
	}

}
