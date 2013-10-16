package view.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The streams equivalent of /dev/null.
 *
 * @author Jonathan Lovelace
 *
 */
public class NullStream extends OutputStream {
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

}
