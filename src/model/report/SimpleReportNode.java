package model.report;

/**
 * A simple node representing plain text. Any children are ignored!
 *
 * @author Jonathan Lovelace
 *
 */
public class SimpleReportNode extends AbstractReportNode {
	/**
	 * @param texts a number of strings to concatenate and make the text of the
	 *        node.
	 */
	public SimpleReportNode(final String... texts) {
		super(concat(texts));
	}

	/**
	 * @param strings a number of strings
	 * @return them all concatenated together
	 */
	private static String concat(final String... strings) {
		int len = 2; // We build in a little tolerance just in case.
		for (final String string : strings) {
			len += string.length();
		}
		final StringBuilder builder = new StringBuilder(len);
		for (final String string : strings) {
			builder.append(string);
		}
		final String retval = builder.toString();
		assert retval != null;
		return retval;
	}

	/**
	 * @return the HTML representation of the node, its text.
	 */
	@Override
	public String produce() {
		return getText();
	}

	/**
	 * @param builder a StringBuilder
	 * @return it, with this node's HTML representation appended.
	 */
	@Override
	public StringBuilder produce(final StringBuilder builder) {
		final StringBuilder retval = builder.append(getText());
		assert retval != null;
		return retval;
	}

	/**
	 * @return the size of the HTML representation of the node.
	 */
	@Override
	public int size() {
		return getText().length();
	}

	/**
	 * @param obj a node
	 * @return whether it equals this one
	 */
	@Override
	protected boolean equalsImpl(final AbstractReportNode obj) {
		return obj instanceof SimpleReportNode
				&& getText().equals(obj.getText());
	}

	/**
	 * @return a hash code for the object
	 */
	@Override
	protected int hashCodeImpl() {
		return getText().hashCode();
	}

}
