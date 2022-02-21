package lovelace.util;

import java.nio.file.Path;

/**
 * An exception to indicate a file that was supposed to be opened was not present.
 *
 * @deprecated Use JDK-standard exceptions instead.
 */
@Deprecated
public class MissingFileException extends Exception {
	private static final long serialVersionUID = 1L;
	public MissingFileException(final Path filename, final Throwable cause) {
		super(String.format("File %s not found", filename.toString()), cause);
	}

	public MissingFileException(final Path filename) {
		super(String.format("File %s not found", filename.toString()));
	}
}
