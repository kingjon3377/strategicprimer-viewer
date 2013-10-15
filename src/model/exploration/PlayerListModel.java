package model.exploration;

import javax.swing.DefaultListModel;

import model.listeners.MapChangeListener;
import model.map.Player;
/**
 * A list model for players in the exploration GUI.
 * @author Jonathan Lovelace
 *
 */
public class PlayerListModel extends DefaultListModel<Player> implements
		MapChangeListener {
	/**
	 * The exploration model.
	 */
	private final IExplorationModel model;
	/**
	 * @param emodel the exploration model
	 */
	public PlayerListModel(final ExplorationModel emodel) {
		emodel.addMapChangeListener(this);
		model = emodel;
		for (final Player player : model.getPlayerChoices()) {
			addElement(player);
		}
	}
	/**
	 * Handle notification that a new map was loaded.
	 */
	@Override
	public void mapChanged() {
		clear();
		for (final Player player : model.getPlayerChoices()) {
			addElement(player);
		}
	}

}
