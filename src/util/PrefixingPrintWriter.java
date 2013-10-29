package util;

import java.io.PrintWriter;
import java.io.Writer;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A PrintWriter that prefixes every line printed via printLine() with a
 * specified string.
 *
 * @author Jonathan Lovelace
 *
 */
public class PrefixingPrintWriter extends PrintWriter {
	/**
	 * @param outs the stream we wrap. Probably a StringWriter
	 * @param prefix what we want each line prefixed with.
	 */
	public PrefixingPrintWriter(final Writer outs, final String prefix) {
		super(outs);
		pref = prefix;
	}

	/**
	 * What the caller wanted each printLine prefixed with.
	 */
	private final String pref;

	/**
	 * Write a line, prefixed with the specified prefix.
	 *
	 * @param str the line
	 */
	@Override
	public void println(@Nullable final String str) {
		super.print(pref);
		super.println(str);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(40);
		builder.append("PrefixingPrintWriter with prefix '");
		builder.append(pref);
		builder.append('\'');
		final String retval = builder.toString();
		assert retval != null;
		return retval;
	}
}
