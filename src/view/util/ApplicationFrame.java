package view.util;

import javax.swing.JFrame;

import util.SingletonRandom;
/**
 * A class for all main app windows to inherit, for use in the WindowMenu.
 * @author Jonathan Lovelace
 *
 */
public class ApplicationFrame extends JFrame {
	/**
	 * A we-hope-unique ID # for the window.
	 */
	private final int windowID = SingletonRandom.RANDOM.nextInt();
	/**
	 * @return the window ID # for this window.
	 */
	public final int getWindowID() {
		return windowID;
	}
	/**
	 * Constructor.
	 * @param title the title of the window
	 */
	public ApplicationFrame(final String title) {
		super(title);
	}
}
