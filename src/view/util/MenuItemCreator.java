package view.util;

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

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
	 * @param item
	 *            the text of the item
	 * @param mnemonic
	 *            the mnemonic key
	 * @param accel
	 *            the keyboard accelerator
	 * @param desc
	 *            the accessibile description.
	 * @param list
	 *            the listener to hande when the item is selected.
	 * @return the configured menu item.
	 */
	public JMenuItem createMenuItem(final String item, final int mnemonic,
			final KeyStroke accel, final String desc, final ActionListener list) {
		final JMenuItem mitem = new JMenuItem(item, mnemonic);
		mitem.setAccelerator(accel);
		mitem.getAccessibleContext().setAccessibleDescription(desc);
		mitem.addActionListener(list);
		return mitem;
	}
}
