package controller.map;
/**
 * A custom exception for not-yet-supported tags.
 * @author Jonathan Lovelace
 *
 */
public class UnsupportedTagException extends SPFormatException {
	/**
	 * The tag.
	 */
	private final String tag;
	/**
	 * @return the tag.
	 */
	public final String getTag() {
		return tag;
	}
	/**
	 * @param uTag the unsupported tag
	 * @param line the line it's on
	 */
	public UnsupportedTagException(final String uTag, final int line) {
		super("Unexpected tag " + uTag
				+ "; probably a more recent map format than viewer", line);
		tag = uTag;
	}
}
