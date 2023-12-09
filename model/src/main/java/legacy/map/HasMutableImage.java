package legacy.map;

/**
 * An interface for model elements that have images that can be used to
 * represent them and that can be changed.
 */
public interface HasMutableImage extends HasImage {
	/**
	 * Set the per-instance icon filename.
	 */
	void setImage(String image);
}
