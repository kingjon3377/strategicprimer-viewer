package model.map;

/**
 * An interface for model elements that have images that can be used to
 * represent them. This interface should really be in model.viewer, but that
 * would, I think, introduce circular dependencies between packages.
 *
 * @author Jonathan Lovelace
 *
 */
public interface HasImage {
	/**
	 * FIXME: This is model-view mixing, but should fix code-complexity problems
	 * in the tile-draw helper.
	 *
	 * This is the image to use if the individual fixture doesn't specify a
	 * different image. It should be a "constant function."
	 *
	 * @return the name of an image to represent this kind of fixture.
	 */
	String getDefaultImage();
	/**
	 * @param image the new image for this *individual* fixture. If null or the
	 *        empty string, the default image will be used.
	 */
	void setImage(final String image);
	/**
	 * @return the name of an image to represent this individual fixture.
	 */
	String getImage();
}
