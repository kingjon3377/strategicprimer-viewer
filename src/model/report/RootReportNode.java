package model.report;

import javax.swing.tree.TreeNode;

import org.eclipse.jdt.annotation.Nullable;

import model.map.Point;
import util.NullCleaner;

/**
 * The root of a node hierarchy.
 *
 * @author Jonathan Lovelace
 *
 */
public class RootReportNode extends AbstractReportNode {
	/**
	 * Constructor.
	 *
	 * @param point the point, if any, in the map that this represents something on
	 * @param title the title text
	 */
	public RootReportNode(@Nullable final Point point, final String title) {
		super(point, title);
	}

	/**
	 * @return the HTML representation of the tree of nodes.
	 */
	@Override
	public String produce() {
		return NullCleaner.assertNotNull(produce(new StringBuilder(size()))
				.toString());
	}

	/**
	 * @param builder a StringBuilder
	 * @return it, with this node's HTML representation appended.
	 */
	@Override
	public StringBuilder produce(final StringBuilder builder) {
		builder.append("<html>\n<head><title>").append(getText())
				.append("</title></head>\n<body>");
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof AbstractReportNode) {
				((AbstractReportNode) child).produce(builder);
			}
		}
		builder.append("</body>\n</html>\n");
		return builder;
	}

	/**
	 * @return approximately how long the HTML representation of this node will
	 *         be.
	 */
	@Override
	public int size() {
		int retval = 72 + getText().length();
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode child = getChildAt(i);
			if (child instanceof AbstractReportNode) {
				retval += ((AbstractReportNode) child).size();
			}
		}
		return retval;
	}

	/**
	 * @param obj a node
	 * @return whether it equals this one
	 */
	@Override
	protected boolean equalsImpl(final IReportNode obj) {
		return obj instanceof RootReportNode && getText().equals(obj.getText())
				&& children().equals(obj.children());
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	protected int hashCodeImpl() {
		return getText().hashCode() /* | children.hashCode() */;
	}
}
