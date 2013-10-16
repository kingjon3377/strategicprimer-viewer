package controller.map.formatexceptions;

/**
 * An exception for cases where a parameter is required (or, if this is merely
 * logged, recommended) but missing.
 *
 * @author Jonathan Lovelace
 *
 */
public class MissingPropertyException extends SPFormatException {

	/**
	 * @param tag the current tag
	 * @param parameter the missing parameter
	 * @param errorLine the line where this occurred
	 */
	public MissingPropertyException(final String tag, final String parameter,
			final int errorLine) {
		super("Missing parameter " + parameter + " in tag " + tag, errorLine);
		context = tag;
		param = parameter;
	}

	/**
	 * The current tag.
	 */
	private final String context;
	/**
	 * The missing parameter.
	 */
	private final String param;

	/**
	 * @return the current tag
	 */
	public final String getTag() {
		return context;
	}

	/**
	 * @return the missing parameter
	 */
	public final String getParam() {
		return param;
	}
}
