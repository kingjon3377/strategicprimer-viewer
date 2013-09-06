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

}
