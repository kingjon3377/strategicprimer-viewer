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
	 *
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
	public SectionListReportNode(final int lvl, final String header,
			final String subtext) {
		super(header);
		setLevel(lvl);
		subheader = subtext;
	}

	/**
	 * An optional sub-header. Since this only comes up once at present, we only
	 * expose it in the constructor.
	 */
	private final String subheader;

	/**
	 * @return the HTML representation of the node
	 */
	@Override
	public String produce() {
		return produce(new StringBuilder(size())).toString();
	}

	/**
	 * @param builder a StringBuilder
	 * @return it, with this node's HTML representation appended.
	 */
	@Override
	public StringBuilder produce(final StringBuilder builder) {
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
		return builder;
	}

	/**
	 * @return approximately how long the HTML representation of this node will
	 *         be.
	 */
	@Override
	public int size() {
		int retval = 32 + getText().length() + subheader.length();
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof AbstractReportNode) {
				retval += ((AbstractReportNode) child).size() + 10;
			}
		}
		return retval;
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
	public final int getHeaderLevel() {
		return level;
	}

	/**
	 * @param obj a node
	 * @return whether it equals this one
	 */
	@Override
	protected boolean equalsImpl(final AbstractReportNode obj) {
		return obj instanceof SectionListReportNode
				&& ((SectionListReportNode) obj).getHeaderLevel() == level
				&& getText().equals(obj.getText())
				&& children().equals(obj.children());
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	protected int hashCodeImpl() {
		return level + getText().hashCode() /* | children().hashCode() */;
	}
}
