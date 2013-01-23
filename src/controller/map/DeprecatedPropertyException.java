package controller.map;

/**
 * A custom exception for cases where one property is deprecated in favor of
 * another.
 *
 * @author Jonathan Lovelace
 *
 */
public class DeprecatedPropertyException extends SPFormatException {
	/**
	 * The current tag.
	 */
	private final String context;

	/**
	 * @return the current tag.
	 */
	public final String getTag() {
		return context;
	}

	/**
	 * The old property.
	 */
	private final String old;

	/**
	 * @return the old name for the property.
	 */
	public final String getOld() {
		return old;
	}

	/**
	 * The preferred form.
	 */
	private final String preferred;

	/**
	 * @return the preferred orm.
	 */
	public final String getPreferred() {
		return preferred;
	}

	/**
	 * @param tag the current tag
	 * @param deprecated the old form
	 * @param newForm the preferred form
	 * @param line where this occurred
	 */
	public DeprecatedPropertyException(final String tag,
			final String deprecated, final String newForm, final int line) {
		super("Use of the property '" + deprecated + "' in tag '" + tag
				+ "' is deprecated; use '" + newForm + "' instead", line);
		context = tag;
		old = deprecated;
		preferred = newForm;
	}
}
