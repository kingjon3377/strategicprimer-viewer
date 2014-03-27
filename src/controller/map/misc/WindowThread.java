package controller.map.misc;

import javax.swing.JFrame;

/**
 * A thread to run a window.
 *
 * @author Jonathan Lovelace
 */
public class WindowThread implements Runnable {
	/**
	 * The window to start.
	 */
	private final JFrame window;

	/**
	 * Constructor.
	 *
	 * @param frame the window to start
	 */
	public WindowThread(final JFrame frame) {
		window = frame;
	}

	/**
	 * Start the window.
	 */
	@Override
	public void run() {
		window.setVisible(true);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "WindowThread";
	}
}
