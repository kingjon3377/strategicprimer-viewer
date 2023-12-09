package legacy.map;

/**
 * An interface for model elements that have images that can be used to represent them.
 */
public interface HasImage {
	/**
	 * The filename of an image to use as an icon if the individual fixture
	 * doesn't specify a different image (if {@link #getImage()} is empty).
	 * This should be constant over an instance's lifetime, and with a few
	 * exceptions should be constant for all instances of a class.
	 *
	 * TODO: replace this with a centralized registry.
	 */
	String getDefaultImage();

	/**
	 * The filename of an image to use as an icon for this particular instance.
	 */
	String getImage();
}
