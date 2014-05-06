package view.window;

import java.awt.Frame;
import java.awt.Window;

import javax.swing.JMenuItem;
/**
 * A menu item in the Window menu.
 * @author Jonathan Lovelace
 */
public class WindowMenuItem extends JMenuItem {
	/**
	 * The window we wrap.
	 */
	private final Window window;
	/**
	 * Constructor.
	 * @param win the window to wrap
	 */
	public WindowMenuItem(final Frame win) {
		super(win.getTitle());
		window = win;
	}
	/**
	 * @return the window we wrap
	 */
	public Window getWindow() {
		return window;
	}
}
