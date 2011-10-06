package view.map.main;
/**
 * An interface for something that can make the selection go in one of the cardinal directions.
 * @author Jonathan Lovelace
 *
 */
public interface DirectionSelectionChanger {

	/**
	 * Move the cursor up one.
	 */
	void up(); // NOPMD

	/**
	 * Move the cursor left one.
	 */
	void left();

	/**
	 * Move the cursor down one.
	 */
	void down();

	/**
	 * Move the cursor right one.
	 */
	void right();

}
