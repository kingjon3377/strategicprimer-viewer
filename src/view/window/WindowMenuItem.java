package view.window;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import org.eclipse.jdt.annotation.Nullable;

import view.window.WindowMenuModel.WindowState;
/**
 * A menu item in the Window menu.
 * @author Jonathan Lovelace
 */
public class WindowMenuItem extends JMenuItem {
	/**
	 * The window we wrap.
	 */
	private final Frame window;
	/**
	 * The current status.
	 */
	private WindowState state = WindowState.NotVisible;
	/**
	 * Whether we are the current window.
	 */
	private boolean curr = false;
	/**
	 * Constructor.
	 * @param win the window to wrap
	 */
	public WindowMenuItem(final Frame win) {
		super(win.getTitle());
		window = win;
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(@Nullable final ActionEvent e) {
				win.setState(Frame.NORMAL);
				win.setVisible(true);
			}
		});
		update();
	}
	/**
	 * @return the window we wrap
	 */
	public Window getWindow() {
		return window;
	}
	/**
	 * @param status the window's status.
	 */
	public void updateForStatus(final WindowState status) {
		state = status;
		update();
	}
	/**
	 * Update for our status and currency.
	 */
	private void update() {
		if (curr) {
			setText("<html>&check;&nbsp;" + window.getTitle() + "</html>");
		} else {
			switch (state) {
			case Minimized:
				setText("<html>&diams;&nbsp;" + window.getTitle() + "</html>");
				break;
			default:
				setText("<html>&nbsp;&nbsp;" + window.getTitle() + "</html>");
				break;
			}
		}

	}
	/**
	 * @param current whether the window is the current window
	 */
	public void setCurrent(final boolean current) {
		curr = current;
		update();
	}
}
