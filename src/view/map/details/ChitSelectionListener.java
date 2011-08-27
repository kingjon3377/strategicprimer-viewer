package view.map.details;

import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;

import model.viewer.Player;
import model.viewer.SPMap;
import view.map.main.SelectionListener;

/**
 * A class to handle selecting Chits.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ChitSelectionListener extends SelectionListener {
	/**
	 * The label we'll write the details to.
	 */
	private final JLabel detailLabel;

	/**
	 * Constructor.
	 * 
	 * @param label
	 *            the label we'll write the details of the selected item to.
	 */
	public ChitSelectionListener(final JLabel label) {
		super();
		detailLabel = label;
	}

	/**
	 * Handle mouse clicks.
	 * 
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void mouseClicked(final MouseEvent event) {
		super.mouseClicked(event);
		if (selection() instanceof Chit) {
			detailLabel.setText(fixText("<html>" + ((Chit) (selection())).describe() + "</html>"));
		} else {
			detailLabel.setText("");
		}
	}

	/**
	 * @param text
	 *            a chit's description text
	 * @return the text, with "player N" replaced by the player's name if known.
	 */
	private String fixText(final String text) {
		if (text.indexOf("player ", 0) >= 0) {
			final Matcher matcher = Pattern.compile("player \\d+")
					.matcher(text);
			if (matcher.find()) {
				final Matcher substr = Pattern.compile("\\d+").matcher(
						matcher.group());
				if (!substr.find()) {
					throw new IllegalStateException(
							"Didn't find numbers after finding them once");
				}
				return players.containsKey(substr.group()) ? text.replaceAll(// NOPMD
						matcher.group(), players.get(substr.group())) : text;
			} else {
				return text; // NOPMD
			}
		} else {
			return text;
		}
	}

	/**
	 * A list of the players the current map knows about.
	 */
	private final Map<String, String> players = new HashMap<String, String>();

	/**
	 * @param mapref
	 *            the new main map
	 */
	public void setMap(final SPMap mapref) {
		players.clear();
		for (Player player : mapref.getPlayers()) {
			players.put(Integer.toString(player.getId()), player.getName());
		}
	}

	/**
	 * Clear the selection.
	 */
	@Override
	public void clearSelection() {
		super.clearSelection();
		detailLabel.setText("");
	}
}
