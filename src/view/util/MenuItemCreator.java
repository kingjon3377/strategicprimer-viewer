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
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class MenuItemCreator {
	/**
	 * Create a menu item.
	 *
	 * @param item the text of the item
	 * @param mnemonic the mnemonic key
	 * @param accel the keyboard accelerator. Null if none is wanted.
	 * @param desc the accessibile description.
	 * @param list the listener to hande when the item is selected.
	 *
	 * @return the configured menu item.
	 */
	public static JMenuItem createMenuItem(final String item,
			final int mnemonic, @Nullable final KeyStroke accel,
			final String desc, final ActionListener list) {
		final JMenuItem mitem = new JMenuItem(item, mnemonic);
		mitem.setAccelerator(accel);
		mitem.getAccessibleContext().setAccessibleDescription(desc);
		mitem.addActionListener(list);
		mitem.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(accel,
				mitem.getAction());
		return mitem;
	}

	/**
	 * Create a hotkey.
	 *
	 * @param key the base key
	 * @return the hotkey
	 */
	public static KeyStroke createHotkey(final int key) {
		return NullCleaner.assertNotNull(KeyStroke.getKeyStroke(key, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	/**
	 * Create a hotkey with the additional Shift modifier.
	 *
	 * @param key the base key
	 * @return the hotkey
	 */
	public static KeyStroke createShiftHotkey(final int key) {
		return NullCleaner.assertNotNull(KeyStroke.getKeyStroke(key, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()
				| InputEvent.SHIFT_DOWN_MASK));
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MenuItemCreator";
	}
}
