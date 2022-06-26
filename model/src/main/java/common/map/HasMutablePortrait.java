package common.map;

/**
 * An interface for model objects that may have a "portrait" to display in the
 * "fixture details" panel, where that portrait can be changed.
 */
public interface HasMutablePortrait extends HasPortrait {
	/**
	 * Set the filename of the image to use as a portrait.
	 */
	void setPortrait(String portrait);
}
