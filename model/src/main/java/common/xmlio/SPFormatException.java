package common.xmlio;

import org.jetbrains.annotations.Nullable;

import javax.xml.stream.Location;
import java.io.Serial;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * A custom exception for XML format errors.
 *
 * TODO: Take filename as well as location in the file?
 */
public abstract class SPFormatException extends Exception {
	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * The line of the XML file containing the mistake.
	 */
	private final int line;
	/**
	 * The column of the XML file where the mistake begins.
	 */
	private final int column;

	/**
	 * @param errorMessage The exception message to possibly show to the user.
	 * @param line         The line of the XML file containing the mistake.
	 * @param column       The column of the XML file where the mistake begins.
	 * @deprecated Use constructor taking Location if possible
	 */
	@Deprecated
	protected SPFormatException(final String errorMessage, final int line, final int column) {
		super("Incorrect SP XML at line %d, column %d: %s".formatted(line, column, errorMessage));
		this.line = line;
		this.column = column;
	}

	private static <Type> int nullSafeCall(final @Nullable Type obj, final ToIntFunction<Type> func) {
		if (Objects.isNull(obj)) {
			return -1;
		} else {
			return func.applyAsInt(obj);
		}
	}

	/**
	 * @param errorMessage The exception message to possibly show to the user.
	 * @param location     The line and column of the XML file where the mistake begins.
	 */
	protected SPFormatException(final String errorMessage, final @Nullable Location location) {
		super("Incorrect SP XML at line %d, column %d: %s".formatted(nullSafeCall(location, Location::getLineNumber),
				nullSafeCall(location, Location::getColumnNumber), errorMessage));
		if (Objects.isNull(location)) {
			line = -1;
			column = -1;
		} else {
			line = location.getLineNumber();
			column = location.getColumnNumber();
		}
	}

	protected SPFormatException(final String errorMessage, final Location location, final Throwable errorCause) {
		super("Incorrect SP XML at line %d, column %d: %s".formatted(location.getLineNumber(),
				location.getColumnNumber(), errorMessage), errorCause);
		line = location.getLineNumber();
		column = location.getColumnNumber();
	}

	public final int getLine() {
		return line;
	}

	public final int getColumn() {
		return column;
	}
}
