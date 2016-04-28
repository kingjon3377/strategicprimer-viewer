package view.util;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.*;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An improved combo-box, improved by making the Tab key do what one
 * expects.
 *
 * The central procedure is derived from http://stackoverflow.com/a/24336768
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
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
 * @param <T> the type of thing listed in the combo box
 */
public class ImprovedComboBox<T> extends JComboBox<T> {
	/**
	 * Constructor. We make the ComboBox editable by default.
	 */
	public ImprovedComboBox() {
		setEditable(true);
	}
	/**
	 * From http://stackoverflow.com/a/24336768
	 *
	 * @param evt the event to process
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void processKeyEvent(@Nullable final KeyEvent evt) {
		if (evt == null) {
			return;
		}
		if ((evt.getID() != KeyEvent.KEY_PRESSED) ||
					(evt.getKeyCode() != KeyEvent.VK_TAB)) {
			super.processKeyEvent(evt);
			return;
		}

		if (isPopupVisible()) {
			assert evt.getSource() instanceof Component;
			final KeyEvent fakeEnterKeyEvent =
					new KeyEvent((Component) evt.getSource(), evt.getID(), evt.getWhen(),
										0, // No modifiers.
										KeyEvent.VK_ENTER, // Enter key.
										KeyEvent.CHAR_UNDEFINED);
			super.processKeyEvent(fakeEnterKeyEvent);
		}
		if (evt.getModifiers() == 0) {
			transferFocus();
		} else if (evt.getModifiers() == InputEvent.SHIFT_MASK) {
			transferFocusBackward();
		}
	}
	/**
	 * Prevent serialization.
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * Prevent serialization
	 * @param in ignored
	 * @throws IOException always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}
