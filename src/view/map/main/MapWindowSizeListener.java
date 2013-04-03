package view.map.main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;

/**
 * A listener to help {@link MapSizeListener} adjust the number of displayed
 * tiles when the window is maximized or restored.
 *
 * @author Jonathan Lovelace
 *
 */
public class MapWindowSizeListener extends WindowAdapter {
	/**
	 * The component to remind of its having been resized on these events.
	 */
	private final JComponent component;
	/**
	 * Whether we should add or subtract 1 to force recalculation this time.
	 */
	private boolean add = false;
	/**
	 * @param comp The component to remind of its having been resized on these events.
	 */
	public MapWindowSizeListener(final JComponent comp) {
		component = comp;
	}
	/**
	 * Invoked when a window is de-iconified.
	 * @param evt the event to handle
	 */
	@Override
	public void windowDeiconified(final WindowEvent evt) {
		final int addend = add ? 1 : -1;
		add = !add;
		component.setSize(component.getWidth() + addend, component.getHeight() + addend);
	}

	/**
	 * Invoked when a window state is changed.
	 * @param evt the event to handle
	 */
	@Override
	public void windowStateChanged(final WindowEvent evt) {
		final int addend = add ? 1 : -1;
		add = !add;
		component.setSize(component.getWidth() + addend, component.getHeight() + addend);
	}
}
