package view.worker;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.JLabel;
import model.listeners.PlayerChangeListener;
import model.map.Player;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A label to show the current player.
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
	 * @param text the string to wrap
	 * @return the wrapped string
	 */
	private static String htmlWrapped(final String text) {
		return "<html><body>" + text + "</body></html>";
	}

	/**
	 * Constructor.
	 *
	 * @param prefix  text to give before the current player's name. Doesn't have to
	 *                include delimiting space.
	 * @param player  the initial player
	 * @param postfix text to give after the current player's name. Must include
	 *                delimiting space, since the first character after the name might be
	 *                punctuation instead.
	 */
	@SuppressWarnings("StringConcatenationMissingWhitespace")
	public PlayerLabel(final String prefix,
					@SuppressWarnings("TypeMayBeWeakened") final Player player,
					final String postfix) {
		super(htmlWrapped(prefix + ' ' + player.getName() + postfix));
		before = prefix;
		after = postfix;
	}

	/**
	 * @param old       the old current player
	 * @param newPlayer the new current player
	 */
	@SuppressWarnings("StringConcatenationMissingWhitespace")
	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		setText(htmlWrapped(before + ' ' + newPlayer.getName() + after));
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
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "PlayerLabel showing " + getText();
	}
}
