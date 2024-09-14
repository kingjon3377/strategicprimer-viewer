package lovelace.util;

import java.io.PrintStream;

import org.jetbrains.annotations.NotNull;

// FIXME: Include source class in log messages (or is this valuable?)
public final class LovelaceLogger {
	private LovelaceLogger() {
		// Do not instantiate.
	}

	private static PrintStream writer = System.out;

	public static void setWriter(final @NotNull PrintStream newWriter) {
		writer = newWriter;
	}

	public enum Level {
		TRACE,
		DEBUG,
		INFO,
		WARNING,
		ERROR
	}

	private static Level level = Level.INFO;

	@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
	public static void setLevel(final @NotNull Level newLevel) {
		level = newLevel;
	}

	// For the sake of being able to change it temporarily, then put it back.
	public static @NotNull Level getLevel() {
		return level;
	}

	// TODO: We'd like some sort of "tagging" mechanism ("we want messages about X but not Y"), separate from priority
	@SuppressWarnings("TypeMayBeWeakened")
	private static void log(final @NotNull Level messageLevel, final @NotNull String format, final Object... args) {
		if (messageLevel.compareTo(level) >= 0) {
			writer.print(messageLevel);
			writer.print(": ");
			writer.printf(format, args);
			writer.println();
		}
	}

	// TODO: We'd like a way of saying "log this, but *don't* show a stack trace", while still passing in the exception
	@SuppressWarnings("TypeMayBeWeakened")
	private static void log(final @NotNull Throwable exception, final @NotNull Level messageLevel,
	                        final @NotNull String format, final Object... args) {
		if (messageLevel.compareTo(level) >= 0) {
			writer.print(messageLevel);
			writer.print(": ");
			writer.printf(format, args);
			writer.println();
			exception.printStackTrace(writer);
		}
	}

	public static void trace(final @NotNull String format, final Object... args) {
		log(Level.TRACE, format, args);
	}

	public static void trace(final @NotNull Throwable exception, final @NotNull String format, final Object... args) {
		log(exception, Level.TRACE, format, args);
	}

	public static void debug(final @NotNull String format, final Object... args) {
		log(Level.DEBUG, format, args);
	}

	public static void debug(final @NotNull Throwable exception, final @NotNull String format, final Object... args) {
		log(exception, Level.DEBUG, format, args);
	}

	public static void info(final @NotNull String format, final Object... args) {
		log(Level.INFO, format, args);
	}

	public static void info(final @NotNull Throwable exception, final @NotNull String format, final Object... args) {
		log(exception, Level.INFO, format, args);
	}

	public static void warning(final @NotNull String format, final Object... args) {
		log(Level.WARNING, format, args);
	}

	public static void warning(final @NotNull Throwable exception, final @NotNull String format, final Object... args) {
		log(exception, Level.WARNING, format, args);
	}

	public static void error(final @NotNull String format, final Object... args) {
		log(Level.ERROR, format, args);
	}

	public static void error(final @NotNull Throwable exception, final @NotNull String format, final Object... args) {
		log(exception, Level.ERROR, format, args);
	}
}
