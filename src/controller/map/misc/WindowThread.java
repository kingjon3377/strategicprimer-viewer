package controller.map.misc;

import javax.swing.JFrame;

/**
 * A thread to run a window.
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
public final class WindowThread implements Runnable {
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
