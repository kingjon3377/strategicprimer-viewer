package model.report;

import javax.swing.tree.TreeNode;

/**
 * A node for a section consisting only of a list. This is a common case, and
 * we'd otherwise end up with a section node containing only a list.
 *
 * @author Jonathan Lovelace
 *
 */
public class SectionListReportNode extends AbstractReportNode {
	/**
	 * Constructor.
	 * @param lvl the header level
	 * @param header the header text
	 */
	public SectionListReportNode(final int lvl, final String header) {
		this(lvl, header, "");
	}

	/**
	 * Constructor.
	 *
	 * @param lvl the header level
	 * @param header the header text
	 * @param subtext the sub-header text
	 */
	public SectionListReportNode(final int lvl, final String header, final String subtext) {
		setLevel(lvl);
		setText(header);
		subheader = subtext;
	}
	/**
	 * An optional sub-header. Since this only comes up once at present, we only expose it in the constructor.
	 */
	private final String subheader;
	/**
	 * @return the HTML representation of the node
	 */
	@Override
	public String produce() {
		final StringBuilder builder = new StringBuilder();
		builder.append("<h").append(level).append('>').append(getText())
				.append("</h").append(level).append(">\n");
		if (!subheader.isEmpty()) {
			builder.append("<p>").append(subheader).append("</p>\n");
		}
		if (getChildCount() != 0) {
			builder.append("<ul>\n");
			for (int i = 0; i < getChildCount(); i++) {
				final TreeNode child = getChildAt(i);
				if (child instanceof AbstractReportNode) {
					builder.append("<li>");
					builder.append(((AbstractReportNode) child).produce());
					builder.append("</li>\n");
				}
			}
			builder.append("</ul>\n");
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
