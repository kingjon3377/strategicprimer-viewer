package model.report;
/**
 * A simple node representing plain text. Any children are ignored!
 * @author Jonathan Lovelace
 *
 */
public class SimpleReportNode extends AbstractReportNode {
	/**
	 * @param text the text of the node
	 */
	public SimpleReportNode(final String text) {
		setText(text);
	}
	/**
	 * @return the HTML representation of the node, its text.
	 */
	@Override
	public String produce() {
		return getText();
	}
	/**
	 * @param obj a node
	 * @return whether it equals this one
	 */
	@Override
	protected boolean equalsImpl(final AbstractReportNode obj) {
		return obj instanceof SimpleReportNode && getText().equals(obj.getText());
	}
	/**
	 * @return a hash code for the object
	 */
	@Override
	protected int hashCodeImpl() {
		return getText().hashCode();
	}

}
