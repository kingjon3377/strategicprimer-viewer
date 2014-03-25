package view.util;

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A utility class to create menu items in a more functional style.
 *
 * @author Jonathan Lovelace
 *
 */
public class MenuItemCreator {
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
		return mitem;
	}

	/**
	 * Create a hotkey.
	 *
	 * @param key the base key
	 * @return the hotkey
	 */
	public static KeyStroke createHotkey(final int key) {
		final KeyStroke retval = KeyStroke.getKeyStroke(key, Toolkit
				.getDefaultToolkit()
				.getMenuShortcutKeyMask());
		assert retval != null;
		return retval;
	}

	/**
	 * Create a hotkey with the additional Shift modifier.
	 *
	 * @param key the base key
	 * @return the hotkey
	 */
	public static KeyStroke createShiftHotkey(final int key) {
		final KeyStroke retval = KeyStroke.getKeyStroke(key, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()
				| InputEvent.SHIFT_DOWN_MASK);
		assert retval != null;
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MenuItemCreator";
	}
}
