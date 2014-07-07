package view.window;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;

/**
 * A singleton object to hold the list of all open windows.
 * @author Jonathan Lovelace
 */
public final class WindowMenuModel extends WindowAdapter implements
		ListModel<Frame>, Iterable<Frame> {
	/**
	 * If a window's 'state' bitmask matches any of these fields, it's
	 * "maximized" for our purposes.
	 */
	private static final int MAXIMIZED_ANY = Frame.MAXIMIZED_HORIZ
			| Frame.MAXIMIZED_VERT | Frame.MAXIMIZED_BOTH;
	/**
	 * Singleton.
	 */
	public static final WindowMenuModel MODEL = new WindowMenuModel();
	/**
	 * The list of windows.
	 */
	private final List<Frame> windows = new ArrayList<>();
	/**
	 * The windows' states.
	 */
	private final Map<Window, WindowState> states = new LinkedHashMap<>();
	/**
	 * The list of listeners to notify about changes to this model.
	 */
	private final List<ListDataListener> listeners = new ArrayList<>();
	/**
	 * Possible window states.
	 */
	public static enum WindowState {
		/**
		 * Visible, but not minimized or maximized.
		 */
		Visible,
		/**
		 * Minimized, and so not visible.
		 */
		Minimized,
		/**
		 * Maximized.
		 */
		Maximized,
		/**
		 * Not visible. For when we're asked about a window that has been removed.
		 */
		NotVisible;
	}
	/**
	 * Singleton.
	 */
	private WindowMenuModel() {
		// Singleton.
	}
	/**
	 * Add a window.
	 * @param window the window to add
	 * @param state its state
	 */
	public void addWindow(final Frame window, final WindowState state) {
		if (!windows.contains(window)) {
			windows.add(window);
		}
		states.put(window, state);
		window.addWindowListener(this);
		window.addWindowStateListener(this);
		final ListDataEvent event =
				new ListDataEvent(window, ListDataEvent.INTERVAL_ADDED,
						states.size() - 1, states.size() - 1);
		for (ListDataListener listener : listeners) {
			listener.intervalAdded(event);
		}
	}
	/**
	 * Add a window in the default (non-minimized, non-maximized) state.
	 * @param window the window to add
	 */
	public void addWindow(final Frame window) {
		addWindow(window, WindowState.Visible);
	}
	/**
	 * @param window a window
	 * @return its state
	 */
	public WindowState getState(final Window window) {
		final WindowState retval = states.get(window);
		if (retval == null) {
			return WindowState.NotVisible;
		} else {
			return retval;
		}
	}
	/**
	 * @param evt the event to handle.
	 */
	@Override
	public void windowStateChanged(@Nullable final WindowEvent evt) {
		if (evt == null) {
			return;
		}
		final Window source = evt.getWindow();
		if (source != null && states.containsKey(source)) {
			final int state = evt.getNewState();
			if ((state & Frame.ICONIFIED) != 0) {
				states.put(source, WindowState.Minimized);
			} else if ((state & MAXIMIZED_ANY) != 0) {
				states.put(source, WindowState.Maximized);
			} else {
				states.put(source, WindowState.Visible);
			}
			final int index = windows.indexOf(source);
			final ListDataEvent event =
					new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED,
							index, index);
			for (final ListDataListener listener : listeners) {
				listener.contentsChanged(event);
			}
		}
	}
	/**
	 * @param evt the event to handle
	 */
	@Override
	public void windowClosing(@Nullable final WindowEvent evt) {
		if (evt != null) {
			removeWindow(evt.getWindow());
		}
	}
	/**
	 * Remove a window from the list and map.
	 * @param window the window to remove
	 */
	private void removeWindow(@Nullable final Window window) {
		final int index = windows.indexOf(window);
		if (index == -1) {
			return;
		}
		final ListDataEvent event =
				new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED,
						index, index);
		for (final ListDataListener listener : listeners) {
			listener.intervalRemoved(event);
		}
		states.remove(window);
	}
	/**
	 * @param evt the event to handle
	 */
	@Override
	public void windowClosed(@Nullable final WindowEvent evt) {
		if (evt != null) {
			removeWindow(evt.getWindow());
		}
	}
	/**
	 * @param evt the event to handle
	 */
	@Override
	public void windowIconified(@Nullable final WindowEvent evt) {
		if (evt == null) {
			return;
		}
		final Window source = evt.getWindow();
		if (source != null && states.containsKey(source)) {
			states.put(source, WindowState.Minimized);
			final int index = windows.indexOf(source);
			final ListDataEvent event =
					new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED,
							index, index);
			for (final ListDataListener listener : listeners) {
				listener.contentsChanged(event);
			}
		}
	}
	@Override
	public void windowDeiconified(@Nullable final WindowEvent evt) {
		if (evt == null) {
			return;
		}
		final Window source = evt.getWindow();
		if (source != null && states.containsKey(source)) {
			final int state = evt.getNewState();
			if ((state & MAXIMIZED_ANY) != 0) {
				states.put(source, WindowState.Maximized);
			} else {
				states.put(source, WindowState.Visible);
			}
			final int index = windows.indexOf(source);
			final ListDataEvent event =
					new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED,
							index, index);
			for (final ListDataListener listener : listeners) {
				listener.contentsChanged(event);
			}
		}
	}
	/**
	 * @return the number of windows
	 */
	@Override
	public int getSize() {
		return states.size();
	}
	/**
	 * @param index an index in the list of windows
	 * @return the window at that index
	 */
	@Override
	public Frame getElementAt(final int index) {
		return NullCleaner.assertNotNull(windows.get(index));
	}
	@Override
	public void addListDataListener(@Nullable final ListDataListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}
	@Override
	public void removeListDataListener(@Nullable final ListDataListener listener) {
		if (listener != null) {
			listeners.remove(listener);
		}
	}
	/**
	 * @return the windows we know about
	 */
	@Override
	public Iterator<Frame> iterator() {
		return NullCleaner.assertNotNull(windows.iterator());
	}
}
