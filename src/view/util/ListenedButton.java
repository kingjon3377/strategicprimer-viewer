package view.util;

import java.awt.event.ActionListener;
import java.util.stream.Stream;

import javax.swing.JButton;

/**
 * A button that takes its listeners as constructor parameters.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2013 Jonathan Lovelace
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
public final class ListenedButton extends JButton {
	/**
	 * Constructor.
	 *
	 * @param text the text to put on the button
	 * @param listeners listeners to add to the button
	 */
	public ListenedButton(final String text, final ActionListener... listeners) {
		super(text);
		Stream.of(listeners).forEach(this::addActionListener);
	}
}
