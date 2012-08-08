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
	 * in the tile-draw helper, and might let us reduce the number of Chit
	 * classes.
	 * 
	 * @return the name of an image to represent the fixture, on the tile and
	 *         probably on the chit to represent it.
	 */
	String getImage();
}
