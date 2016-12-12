package controller.map.misc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.TypesafeLogger;

/**
 * A class to match menu item selections to the listeners to handle them.
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
public class MenuBroker implements ActionListener {
	/**
	 * The mapping from "actions" to listeners.
	 */
	private final Map<String, ActionListener> mapping = new HashMap<>();
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(MenuBroker.class);
	/**
	 * Note that at most one listener will be notified of any given action-command;
	 * subsequent registrations override previous ones.
	 * @param listener a listener
	 * @param actions actions it should be notified of. Treated case-insensitively
	 */
	public final void register(final ActionListener listener, final String... actions) {
		for (final String action : actions) {
			mapping.put(action.toLowerCase(), listener);
		}
	}
	/**
	 * Passes the event to the item that's registered to handle that action command, or
	 * logs a warning.
	 * @param evt a menu item press to handle
	 */
	@Override
	public void actionPerformed(final ActionEvent evt) {
		final String action = evt.getActionCommand().toLowerCase();
		if (mapping.containsKey(action)) {
			mapping.get(action).actionPerformed(evt);
		} else {
			LOGGER.log(Level.WARNING, "Unhandled action: %s", evt.getActionCommand());
		}
	}
}
