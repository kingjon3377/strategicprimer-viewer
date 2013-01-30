package controller.map.formatexceptions;

/**
 * A custom exception for cases where a tag has a property it doesn't support.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class UnsupportedPropertyException extends SPFormatException {
	/**
	 * @param tag the current tag
	 * @param parameter the unsupported parameter
	 * @param line the line where this occurred
	 */
	public UnsupportedPropertyException(final String tag,
			final String parameter, final int line) {
		super("Unsupported property " + parameter + " in tag " + tag, line);
		context = tag;
		param = parameter;
	}

	/**
	 * The current tag.
	 */
	private final String context;
	/**
	 * The unsupported parameter.
	 */
	private final String param;

	/**
	 * @return the current tag
	 */
	public final String getTag() {
		return context;
	}

	/**
	 * @return the unsupported parameter
	 */
	public final String getParam() {
		return param;
	}
}
