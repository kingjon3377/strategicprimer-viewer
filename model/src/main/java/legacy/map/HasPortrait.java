package legacy.map;

/**
 * An interface for model objects that may have a "portrait" to display in the
 * "fixture details" panel.
 */
public interface HasPortrait {
	/**
	 * The filename of an image to use as a portrait.
	 */
	String getPortrait();
}
