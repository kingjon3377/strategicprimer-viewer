package view.util;

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;

/**
 * A utility class to create menu items in a more functional style.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class MenuItemCreator {
	/**
	 * Create a menu item.
	 *
	 * @param item     the text of the item
	 * @param mnemonic the mnemonic key
	 * @param accelerator    the keyboard accelerator. Null if none is wanted.
	 * @param desc     the accessible description.
	 * @param list     the listener to handle when the item is selected.
	 * @return the configured menu item.
	 */
	public static JMenuItem createMenuItem(final String item, final int mnemonic,
											@Nullable final KeyStroke accelerator,
											final String desc,
											final ActionListener list) {
		final JMenuItem menuItem = new JMenuItem(item, mnemonic);
		menuItem.setAccelerator(accelerator);
		menuItem.getAccessibleContext().setAccessibleDescription(desc);
		menuItem.addActionListener(list);
		menuItem.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(accelerator,
				menuItem.getAction());
		return menuItem;
	}

	/**
	 * Create a hot-key.
	 *
	 * @param key the base key
	 * @return the hot-key
	 */
	public static KeyStroke createHotKey(final int key) {
		return NullCleaner.assertNotNull(KeyStroke.getKeyStroke(key,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	/**
	 * Create a hot-key with the additional Shift modifier.
	 *
	 * @param key the base key
	 * @return the hot-key
	 */
	public static KeyStroke createShiftHotKey(final int key) {
		return NullCleaner.assertNotNull(KeyStroke.getKeyStroke(key,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() |
						InputEvent.SHIFT_DOWN_MASK));
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "MenuItemCreator";
	}
}
