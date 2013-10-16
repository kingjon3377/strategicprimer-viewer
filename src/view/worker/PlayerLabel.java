package view.worker;

import javax.swing.JLabel;

import model.listeners.PlayerChangeListener;
import model.map.Player;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A label to show the current player.
 *
 * @author Jonathan Lovelace
 *
 */
public class PlayerLabel extends JLabel implements PlayerChangeListener {
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
