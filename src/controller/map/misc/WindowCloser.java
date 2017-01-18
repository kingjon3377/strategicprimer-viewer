package controller.map.misc;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A class to close a window when the user says to.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class WindowCloser implements ActionListener {
	/**
	 * The window to close.
	 */
	private final Window frame;
	/**
	 * Constructor.
	 * @param window the window to close
	 */
	public WindowCloser(final Window window) {
		frame = window;
	}
	/**
	 * Close the window.
	 * @param evt ignored: only set this to listen to the item you want!
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void actionPerformed(final ActionEvent evt) {
		frame.dispose();
	}
}
