package view.window;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.eclipse.jdt.annotation.Nullable;
/**
 * A menu listing all the program's open windows.
 * @author Jonathan Lovelace
 *
 */
public class WindowMenu extends JMenu implements ListDataListener {
	/**
	 * Constructor.
	 */
	public WindowMenu() {
		super("Window");
		setMnemonic(KeyEvent.VK_W);
		WindowMenuModel.MODEL.addListDataListener(this);
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
		final JMenuItem item = new WindowMenuItem(window);
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
				// TODO: Handle state changes with font or icon changes
				((JMenuItem) item).setText(window.getTitle());
			}
		}
	}

}
