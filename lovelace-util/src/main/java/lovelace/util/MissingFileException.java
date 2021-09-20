package lovelace.util;

import java.nio.file.Path;

/**
 * An exception to indicate a file that was supposed to be opened was not present.
 *
 * @deprecated Use JDK-standard exceptions instead.
 */
@Deprecated
public class MissingFileException extends Exception {
	public MissingFileException(Path filename, Throwable cause) {
		super(String.format("File %s not found", filename.toString()), cause);
	}

	public MissingFileException(Path filename) {
		super(String.format("File %s not found", filename.toString()));
	}
}
