package view.worker;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;

import model.map.Player;
/**
 * A label to show the current player.
 * @author Jonathan Lovelace
 *
 */
public class PlayerLabel extends JLabel implements PropertyChangeListener {
	/**
	 * Text to give before the current player's name.
	 */
	private final String text;
	/**
	 * Wrap a string in HTML tags.
	 * @param string the string to wrap
	 * @return the wrapped string
	 */
	private static String htmlize(final String string) {
		return "<html><body>" + string + "</body></html>";
	}
	/**
	 * Constructor.
	 * @param prefix text to give before the current player's name.
	 * @param player the initial player
	 */
	public PlayerLabel(final String prefix, final Player player) {
		super(htmlize(prefix + ' ' + player.getName()));
		text = prefix;
	}
	/**
	 * @param evt the event to handle
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("player".equalsIgnoreCase(evt.getPropertyName()) && evt.getNewValue() instanceof Player) {
			setText(htmlize(text + ' ' + ((Player) evt.getNewValue()).getName()));
		}
	}
}
