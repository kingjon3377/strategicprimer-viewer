package view.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * The streams equivalent of /dev/null.
 *
 * TODO: Now move this out of 'view'.
 *
 * @author Jonathan Lovelace
 *
 */
public class NullStream extends OutputStream {
	/**
	 * A bit-bucket to send subset output to.
	 */
	public static final PrintWriter BIT_BUCKET = new PrintWriter(
			new OutputStreamWriter(new NullStream()));
	/**
	 * Do nothing when anything is written.
	 *
	 * @param byt ignored
	 * @throws IOException never
	 */
	@Override
	public void write(final int byt) throws IOException {
		// Do nothing.
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "NullStream";
	}
}
