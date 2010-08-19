package view.util;

/**
 * An interface that, together with the ApplyButtonHandler class, simplifies
 * form management.
 * 
 * @author Jonathan Lovelace
 * 
 */
public interface Applyable {
	/**
	 * Method to call when the Apply button is pressed.
	 */
	void apply();
	/**
	 * Method to call when the Revert button is pressed.
	 */
	void revert();
}
