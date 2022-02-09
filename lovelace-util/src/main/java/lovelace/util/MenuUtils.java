package lovelace.util;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.Toolkit;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.util.stream.Stream;

/**
 * A class to contain utility methods related to menus and menu items.
 */
public final class MenuUtils {
	/**
	 * An enumeration of possible modifiers to hot-keys.
	 */
	public enum HotKeyModifier {
		Shift(InputEvent.SHIFT_DOWN_MASK),
		Ctrl(InputEvent.CTRL_DOWN_MASK),
		Meta(InputEvent.META_DOWN_MASK);
		private final int mask;
		public int getMask() {
			return mask;
		}
		HotKeyModifier(final int mask) {
			this.mask = mask;
		}
	}

	/**
	 * Create a key-stroke representing a hot-key accelerator.
	 */
	public static KeyStroke createAccelerator(final int key, final HotKeyModifier... modifiers) {
		return KeyStroke.getKeyStroke(key, Stream.of(modifiers).mapToInt(HotKeyModifier::getMask).reduce(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), (a, b) -> a | b));
	}

	/**
	 * Set up a hot-key for an action that doesn't call a *menu* item.
	 *
	 * @param component the component defining the action's context
	 * @param action the String to use to identify the action
	 * @param handler the listener that should handle the action
	 * @param condition See {@link JComponent#getInputMap(Integer)}
	 * @param keys the keys to use as hot-keys.
	 */
	public static void createHotKey(final JComponent component, final String action, final ActionListener handler,
	                                final int condition, final KeyStroke... keys) {
		createHotKey(component, action, new ActionWrapper(handler), condition, keys);
	}

	/**
	 * Set up a hot-key for an action that doesn't call a *menu* item.
	 *
	 * @param component the component defining the action's context
	 * @param action the String to use to identify the action
	 * @param handler the listener that should handle the action
	 * @param condition See {@link JComponent#getInputMap(int)}
	 * @param keys the keys to use as hot-keys.
	 */
	public static void createHotKey(final JComponent component, final String action, final Action handler,
	                                final int condition, final KeyStroke... keys) {
		final InputMap inputMap = component.getInputMap(condition);
		for (final KeyStroke key : keys) {
			inputMap.put(key, action);
		}
		component.getActionMap().put(action, handler);
	}

	/**
	 * Create a menu item, where the listener doesn't care about the event object.
	 * @param item the text of the item
	 * @param mnemonic the mnemonic key
	 * @param the description to show to accessibility software
	 * @param listener the listener to handle when the item is selected
	 * @param accelerators the keyboard accelerators (hot-keys).
	 *
	 * Only the first accelerator is shown in the menu, but all are listened for.
	 */
	public static JMenuItem createMenuItem(final String item, final int mnemonic, final String description,
	                                       final Runnable listener, final KeyStroke... accelerators) {
		return createMenuItem(item, mnemonic, description, (event) -> listener.run(), accelerators);
	}

	/**
	 * Create a menu item.
	 * @param item the text of the item
	 * @param mnemonic the mnemonic key
	 * @param the description to show to accessibility software
	 * @param listener the listener to handle when the item is selected
	 * @param accelerators the keyboard accelerators (hot-keys).
	 *
	 * Only the first accelerator is shown in the menu, but all are listened for.
	 */
	public static JMenuItem createMenuItem(final String item, final int mnemonic, final String description,
	                                       final ActionListener listener, final KeyStroke... accelerators) {
		final JMenuItem menuItem = new JMenuItem(item, mnemonic);
		if (accelerators.length > 0) {
			menuItem.setAccelerator(accelerators[0]);
		}
		menuItem.getAccessibleContext().setAccessibleDescription(description);
		menuItem.addActionListener(listener);
		final InputMap inputMap = menuItem.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		for (final KeyStroke accelerator : accelerators) {
			inputMap.put(accelerator, menuItem.getAction());
		}
		return menuItem;
	}
}
