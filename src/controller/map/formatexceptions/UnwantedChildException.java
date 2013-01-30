package controller.map.formatexceptions;

/**
 * A custom exception for when a tag has a child tag it can't handle.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class UnwantedChildException extends SPFormatException {

	/**
	 * @param parent the current tag
	 * @param child the unwanted child
	 * @param errorLine the line where this happened
	 */
	public UnwantedChildException(final String parent, final String child,
			final int errorLine) {
		super("Unexpected child " + child + " in tag " + parent, errorLine);
		tag = parent;
		chld = child;
	}

	/**
	 * The current tag.
	 */
	private final String tag;

	/**
	 * @return the current tag.
	 */
	public final String getTag() {
		return tag;
	}

	/**
	 * The unwanted child.
	 */
	private final String chld;

	/**
	 * @return the unwanted child.
	 */
	public final String getChild() {
		return chld;
	}
}
