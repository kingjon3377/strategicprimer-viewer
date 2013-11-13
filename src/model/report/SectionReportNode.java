package model.report;

import javax.swing.tree.TreeNode;

/**
 * A node representing a section, with a header.
 *
 * @author Jonathan Lovelace
 *
 */
public class SectionReportNode extends AbstractReportNode {
	/**
	 * Constructor.
	 *
	 * @param lvl the header level
	 * @param header the header text
	 */
	public SectionReportNode(final int lvl, final String header) {
		super(header);
		setLevel(lvl);
	}

	/**
	 * @return the HTML representation of the node
	 */
	@Override
	public String produce() {
		final String retval = produce(new StringBuilder(size())).toString();
		assert retval != null;
		return retval;
	}

	/**
	 * @param builder a StringBuilder
	 * @return it, with this node's HTML representation appended.
	 */
	@Override
	public StringBuilder produce(final StringBuilder builder) {
		builder.append("<h").append(level).append('>').append(getText())
				.append("</h").append(level).append(">\n");
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof AbstractReportNode) {
				((AbstractReportNode) child).produce(builder);
			}
		}
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
				retval += ((AbstractReportNode) child).size();
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
	 * @param obj an object
	 * @return whether it's the same as this
	 */
	@Override
	protected boolean equalsImpl(final IReportNode obj) {
		return obj instanceof SectionReportNode
				&& level == ((SectionReportNode) obj).getLevel()
				&& getText().equals(obj.getText())
				&& children().equals(obj.children());
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	protected int hashCodeImpl() {
		return level + getText().hashCode() /* | children.hashCode() */;
	}
}
