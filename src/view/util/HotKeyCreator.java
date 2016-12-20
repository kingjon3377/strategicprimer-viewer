package view.util;

import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * An interface to hold a helper method for setting up hotkeys that don't call *menu*
 * items.
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
public interface HotKeyCreator {
	/**
	 * Set up hot-keys for a particular action.
	 * @param component the component that defines its context
	 * @param action the String to use to identify the action
	 * @param handler the listener that should handle the action
	 * @param condition See {@link JComponent#getInputMap(int)}.
	 * @param keys the keys to use as hot-keys.
	 */
	default void createHotKey(final JComponent component, final String action,
							  final Action handler, final int condition,
							  final KeyStroke... keys) {
		final InputMap inputMap = component.getInputMap(condition);
		for (final KeyStroke key : keys) {
			if (key != null) {
				inputMap.put(key, action);
			}
		}
		component.getActionMap().put(action, handler);
	}
}
