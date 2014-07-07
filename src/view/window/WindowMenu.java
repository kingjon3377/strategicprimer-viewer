package view.window;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.eclipse.jdt.annotation.Nullable;
/**
 * A menu listing all the program's open windows.
 * @author Jonathan Lovelace
 *
 */
public class WindowMenu extends JMenu implements ListDataListener,
		WindowFocusListener {
	/**
	 * Menu-item cache.
	 */
	private final Map<Window, WindowMenuItem> cache = new HashMap<>();
	/**
	 * Constructor.
	 */
	public WindowMenu() {
		super("Window");
		setMnemonic(KeyEvent.VK_W);
		WindowMenuModel.MODEL.addListDataListener(this);
		for (final Frame window : WindowMenuModel.MODEL) {
			if (window != null) {
				add(addToCache(window, new WindowMenuItem(window)));
				window.addWindowFocusListener(this);
			}
		}
	}
	/**
	 * @param window a window to add to the cache
	 * @param item its menu item
	 * @return the menu item
	 */
	public WindowMenuItem addToCache(final Frame window, final WindowMenuItem item) {
		cache.put(window, item);
		return item;
	}
	/**
	 * @param window a window to remove from the cache
	 */
	public void removeFromCache(final Frame window) {
		cache.remove(window);
	}
	/**
	 * @param evt a notification of a window being added.
	 */
	@Override
	public void intervalAdded(@Nullable final ListDataEvent evt) {
		if (evt == null) {
			return;
		}
		final int index = evt.getIndex0();
		final Frame window = WindowMenuModel.MODEL.getElementAt(index);
		final WindowMenuItem item = new WindowMenuItem(window);
		addToCache(window, item);
		add(item);
	}
	/**
	 * @param evt A notification of a window about to be removed.
	 */
	@Override
	public void intervalRemoved(@Nullable final ListDataEvent evt) {
		if (evt == null) {
			return;
		}
		final int index = evt.getIndex0();
		final Window window = WindowMenuModel.MODEL.getElementAt(index);
		if (window instanceof Frame) {
			removeFromCache((Frame) window);
		}
		for (final Component item : getComponents()) {
			if (item instanceof WindowMenuItem
					&& ((WindowMenuItem) item).getWindow() == window) {
				remove(item);
				break;
			}
		}
	}
	/**
	 * @param evt A notification of a window changing state.
	 *
	 * TODO: We should do more than refresh its title.
	 */
	@Override
	public void contentsChanged(@Nullable final ListDataEvent evt) {
		if (evt == null) {
			return;
		}
		final int index = evt.getIndex0();
		final Frame window = WindowMenuModel.MODEL.getElementAt(index);
		for (final Component item : getComponents()) {
			if (item instanceof WindowMenuItem
					&& ((WindowMenuItem) item).getWindow() == window) {
				((WindowMenuItem) item).updateForStatus(WindowMenuModel.MODEL
						.getState(window));
			}
		}
	}
	@Override
	public void windowGainedFocus(@Nullable final WindowEvent e) {
		if (e == null) {
			return;
		}
		final Window window = e.getWindow();
		cache.get(window).setCurrent(true);
	}
	@Override
	public void windowLostFocus(@Nullable final WindowEvent e) {
		if (e == null) {
			return;
		}
		final Window window = e.getWindow();
		cache.get(window).setCurrent(false);
	}

}
