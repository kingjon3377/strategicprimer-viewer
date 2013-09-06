package model.report;

import javax.swing.tree.TreeNode;

/**
 * A node representing a section, with a header.
 * @author Jonathan Lovelace
 *
 */
public class SectionReportNode extends AbstractReportNode {
	/**
	 * Constructor.
	 * @param lvl the header level
	 * @param header the header text
	 */
	public SectionReportNode(final int lvl, final String header) {
		setLevel(lvl);
		setText(header);
	}
	/**
	 * @return the HTML representation of the node
	 */
	@Override
	public String produce() {
		final StringBuilder builder = new StringBuilder();
		builder.append("<h").append(level).append('>').append(getText())
				.append("</h").append(level).append(">\n");
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof AbstractReportNode) {
				builder.append(((AbstractReportNode) child).produce());
			}
		}
		return builder.toString();
	}
	/**
	 * The header level.
	 */
	private int level;
	/**
	 * @param lvl the new header level
	 */
	public final void setLevel(final int lvl) {
		level = lvl;
	}
	/**
	 * @return the header level
	 */
	@Override
	public final int getLevel() {
		return level;
	}
}
