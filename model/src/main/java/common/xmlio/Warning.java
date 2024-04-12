package common.xmlio;

import common.idreg.DuplicateIDException;

import java.util.function.Consumer;

import lovelace.util.LovelaceLogger;

/**
 * A slightly-customizable warning-handling interface.
 *
 * TODO: Move these to lovelace.util.common (figure out how to make the
 * filtering in {@link #WARN} work first ...
 */
public final class Warning {
	/**
	 * Don't do anything with warnings.
	 */
	public static final Warning IGNORE = new Warning(x -> {
	});

	/**
	 * Log each warning, but let them pass.
	 */
	public static final Warning WARN = new Warning(Warning::warn);

	/**
	 * Treat warnings as errors.
	 */
	public static final Warning DIE = new Warning(Warning::die);

	/**
	 * The default warning handler. This is provided so that it can in
	 * theory be changed later in one place rather than everywhere.
	 */
	private static Warning defaultHandler = WARN;

	public static Warning getDefaultHandler() {
		return defaultHandler;
	}

	public static void setDefaultHandler(final Warning handler) {
		defaultHandler = handler;
	}

	private final Consumer<Throwable> impl;

	/**
	 * Handle a warning, such as if a particular map-format construct is
	 * deprecated.
	 */
	public void handle(final Throwable warning) {
		impl.accept(warning);
	}

	public Warning(final Consumer<Throwable> impl) {
		this.impl = impl;
	}

	@SuppressWarnings("BooleanParameter") // Dummy parameter to distinguish signatures
	public Warning(final Consumer<String> handler, final boolean ignored) {
		impl = t -> {
			if (t instanceof SPFormatException) {
				handler.accept("SP format warning: " + t.getMessage());
			} else {
				handler.accept("Warning: " + t.getMessage());
			}
		};
	}

	private static void warn(final Throwable warning) {
		if (warning instanceof SPFormatException || warning instanceof DuplicateIDException) {
			LovelaceLogger.warning(warning.getMessage());
		} else {
			LovelaceLogger.warning(warning, "Warning: ");
		}
	}

	private static void die(final Throwable warning) {
		throw new RuntimeException(warning);
	}

	@Override
	public String toString() {
		return "Warning";
	}
}

