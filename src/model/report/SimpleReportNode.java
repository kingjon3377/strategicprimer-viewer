package model.report;

import org.eclipse.jdt.annotation.Nullable;

import model.map.Point;
import util.NullCleaner;

/**
 * A simple node representing plain text. Any children are ignored!
 *
 * @author Jonathan Lovelace
 *
 */
public class SimpleReportNode extends AbstractReportNode {
	/**
	 * @param point the point, if any, in the map that this represents something on
	 * @param texts a number of strings to concatenate and make the text of the
	 *        node.
	 */
	public SimpleReportNode(@Nullable final Point point, final String... texts) {
		super(point, concat(texts));
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
		return NullCleaner.assertNotNull(builder.toString());
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
		return NullCleaner.assertNotNull(builder.append(getText()));
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
	protected boolean equalsImpl(final IReportNode obj) {
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
