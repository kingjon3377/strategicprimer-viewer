package controller.map.formatexceptions;

/**
 * A custom exception for when a tag (or a Node) requuires a child and it isn't
 * there.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class MissingChildException extends SPFormatException {
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
	 * @param tag the current tag (the one that needs a child)
	 * @param line the current line
	 */
	public MissingChildException(final String tag, final int line) {
		super("Tag " + tag + " missing a child", line);
		context = tag;
	}

}
