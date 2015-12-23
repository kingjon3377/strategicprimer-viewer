package view.map.main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A listener to help {@link MapSizeListener} adjust the number of displayed tiles when
 * the window is maximized or restored.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class MapWindowSizeListener extends WindowAdapter {
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
	 *
	 * @param evt the event to handle
	 */
	@Override
	public void windowDeiconified(@Nullable final WindowEvent evt) {
		final int addend;
		if (add) {
			addend = 1;
		} else {
			addend = -1;
		}
		add ^= true;
		component.setSize(component.getWidth() + addend, component.getHeight()
				                                                 + addend);
	}

	/**
	 * Invoked when a window state is changed.
	 *
	 * @param evt the event to handle
	 */
	@Override
	public void windowStateChanged(@Nullable final WindowEvent evt) {
		final int addend;
		if (add) {
			addend = 1;
		} else {
			addend = -1;
		}
		add ^= true;
		component.setSize(component.getWidth() + addend, component.getHeight()
				                                                 + addend);
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "MapWindowSizeListener";
	}
}
