package model.map;

/**
 * An interface for model objects that may have a "portrait" to display in the
 * "fixture details" panel.
 *
 * @author Jonathan Lovelace
 *
 */
public interface HasPortrait {
	/**
	 * @return the name of the image file containing the portrait.
	 */
	String getPortrait();
}
