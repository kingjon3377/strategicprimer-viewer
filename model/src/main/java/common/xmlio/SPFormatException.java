package common.xmlio;

import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import javax.xml.stream.Location;
import java.io.Serial;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.ToIntFunction;

/**
 * A custom exception for XML format errors.
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
	 * The XML file (if not reading from a stream) containing the mistake
	 */
	private final @Nullable Path file;

	/**
	 * @param errorMessage The exception message to possibly show to the user.
	 * @param line         The line of the XML file containing the mistake.
	 * @param column       The column of the XML file where the mistake begins.
	 * @deprecated Use constructor taking Location if possible
	 */
	@Deprecated
	protected SPFormatException(final String errorMessage, final @Nullable Path file, final int line,
	                            final int column) {
		super("Incorrect SP XML at line %d, column %d: %s".formatted(line, column, errorMessage));
		this.line = line;
		this.column = column;
		this.file = file;
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
	 * @param location     The file, line and column of the XML file where the mistake begins.
	 */
	protected SPFormatException(final String errorMessage, final @Nullable Pair<@Nullable Path, Location> location) {
		super("Incorrect SP XML in %s at line %d, column %d: %s".formatted(
				Optional.ofNullable(location).map(Pair::getValue0).map(Path::toString).orElse("an unknown file"),
				nullSafeCall(location, l -> l.getValue1().getLineNumber()),
				nullSafeCall(location, l -> l.getValue1().getColumnNumber()), errorMessage));
		if (Objects.isNull(location)) {
			line = -1;
			column = -1;
			file = null;
		} else {
			line = location.getValue1().getLineNumber();
			column = location.getValue1().getColumnNumber();
			file = location.getValue0();
		}
	}

	protected SPFormatException(final String errorMessage, final @Nullable Path file, final Location location,
	                            final Throwable errorCause) {
		super("Incorrect SP XML in %s at line %d, column %d: %s".formatted(
				Optional.ofNullable(file).map(Path::toString).orElse("an unknown file"),
				location.getLineNumber(), location.getColumnNumber(), errorMessage), errorCause);
		line = location.getLineNumber();
		column = location.getColumnNumber();
		this.file = file;
	}

	public final int getLine() {
		return line;
	}

	public final int getColumn() {
		return column;
	}

	public final @Nullable Path getFile() {
		return file;
	}
}
