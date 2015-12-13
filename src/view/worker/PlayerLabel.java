package view.worker;

import javax.swing.JLabel;

import org.eclipse.jdt.annotation.Nullable;

import model.listeners.PlayerChangeListener;
import model.map.Player;

/**
 * A label to show the current player.
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
public final class PlayerLabel extends JLabel implements PlayerChangeListener {
	/**
	 * Text to give before the current player's name.
	 */
	private final String before;
	/**
	 * Text to give after the current player's name.
	 */
	private final String after;

	/**
	 * Wrap a string in HTML tags.
	 *
	 * @param string the string to wrap
	 * @return the wrapped string
	 */
	private static String htmlize(final String string) {
		return "<html><body>" + string + "</body></html>";
	}

	/**
	 * Constructor.
	 *
	 * @param prefix text to give before the current player's name. Doesn't have
	 *        to include delimiting space.
	 * @param player the initial player
	 * @param postfix text to give after the current player's name. Must include
	 *        delimiting space, since the first character after the name might
	 *        be punctuation instead.
	 */
	public PlayerLabel(final String prefix, final Player player,
			final String postfix) {
		super(htmlize(prefix + ' ' + player.getName() + postfix));
		before = prefix;
		after = postfix;
	}

	/**
	 * @param old the old current player
	 * @param newPlayer the new current player
	 */
	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		setText(htmlize(before + newPlayer.getName() + after));
	}
}
