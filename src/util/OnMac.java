package util;

import java.awt.event.InputEvent;
import javax.swing.JButton;

/**
 * A utility class to detect, and cache, whether the system is a Mac or not.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class OnMac {
	/**
	 * Whether this system is a mac.
	 */
	@SuppressWarnings("AccessOfSystemProperties")
	public static final boolean SYSTEM_IS_MAC;
	/**
	 * The usual shortcut-key modifier.
	 */
	public static final int SHORTCUT_MASK;
	/**
	 * A String describing that modifier.
	 */
	public static final String SHORTCUT_DESC;
	/**
	 * Set up system-dependent properties.
	 */
	static {
		final String temp = System.getProperty("os.name");
		if (temp == null) {
			SYSTEM_IS_MAC = false;
		} else {
			SYSTEM_IS_MAC = temp.toLowerCase().startsWith("mac os x");
		}
		if (SYSTEM_IS_MAC) {
			SHORTCUT_MASK = InputEvent.META_DOWN_MASK;
			SHORTCUT_DESC = "\u2318";
		} else {
			SHORTCUT_MASK = InputEvent.CTRL_DOWN_MASK;
			SHORTCUT_DESC = "Ctrl+";
		}
	}

	/**
	 * Do not instantiate.
	 */
	private OnMac() {
		// Do not instantiate.
	}
	/**
	 * Make buttons segmented on Mac. Does nothing if zero or one buttons.
	 * @param buttons the buttons to style
	 */
	public static void makeButtonsSegmented(final JButton... buttons) {
		if (SYSTEM_IS_MAC && buttons.length > 1) {
			for (int i = 0; i < buttons.length; i++) {
				final JButton button = buttons[i];
				button.putClientProperty("JButton.buttonType", "segmented");
				if (i == 0) {
					button.putClientProperty("JButton.segmentPosition", "first");
				} else if (i == (buttons.length - 1)) {
					button.putClientProperty("JButton.segmentPosition", "last");
				}
			}
		}
	}
}
