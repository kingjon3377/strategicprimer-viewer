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
		super(text);
	}
	/**
	 * @return an HTML representation of the node.
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
		builder.append(getText());
		for (int i = 0; i < getChildCount(); i++) {
			final TreeNode node = getChildAt(i);
			if (node instanceof AbstractReportNode) {
				((AbstractReportNode) node).produce(builder);
			}
		}
		return builder;
	}
	/**
	 * @return approximately how long the HTML representation of this node will be.
	 */
	@Override
	public int size() {
		int retval = getText().length();
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
	protected boolean equalsImpl(final AbstractReportNode obj) {
		return obj instanceof ComplexReportNode && getText().equals(obj.getText()) && children().equals(obj.children());
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	protected int hashCodeImpl() {
		return getText().hashCode() /*| children().hashCode()*/;
	}
}
