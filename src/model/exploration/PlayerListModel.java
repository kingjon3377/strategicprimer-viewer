package model.exploration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultListModel;

import org.eclipse.jdt.annotation.Nullable;

import model.map.Player;
/**
 * A list model for players in the exploration GUI.
 * @author Jonathan Lovelace
 *
 */
public class PlayerListModel extends DefaultListModel<Player> implements
		PropertyChangeListener {
	/**
	 * The exploration model.
	 */
	private final IExplorationModel model;
	/**
	 * @param emodel the exploration model
	 */
	public PlayerListModel(final ExplorationModel emodel) {
		emodel.addPropertyChangeListener(this);
		model = emodel;
		for (final Player player : model.getPlayerChoices()) {
			addElement(player);
		}
	}
	/**
	 * Handle property change.
	 * @param evt the event to handle
	 */
	@Override
	public void propertyChange(@Nullable final PropertyChangeEvent evt) {
		if (evt != null && "map".equals(evt.getPropertyName())) {
			clear();
			for (final Player player : model.getPlayerChoices()) {
				addElement(player);
			}
		}
	}

}
