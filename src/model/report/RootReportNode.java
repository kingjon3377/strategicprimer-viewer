package model.report;

import javax.swing.tree.TreeNode;

/**
 * The root of a node hierarchy.
 * @author Jonathan Lovelace
 *
 */
public class RootReportNode extends AbstractReportNode {
	/**
	 * Constructor.
	 * @param title the title text
	 */
	public RootReportNode(final String title) {
		setText(title);
	}
	/**
	 * @return the HTML representation of the tree of nodes.
	 */
	@Override
	public String produce() {
		final StringBuilder builder = new StringBuilder("<html>\n");
		builder.append("<head><title>").append(getText()).append("</title></head>\n");
		builder.append("<body>");
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof AbstractReportNode) {
				builder.append(((AbstractReportNode) child).produce());
			}
		}
		builder.append("</body>\n</html>\n");
		return builder.toString();
	}

}
