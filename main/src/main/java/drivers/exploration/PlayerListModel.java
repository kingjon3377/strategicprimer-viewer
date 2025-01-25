package drivers.exploration;

import javax.swing.DefaultListModel;

import legacy.map.Player;

import drivers.common.MapChangeListener;

import exploration.common.IExplorationModel;

import java.io.NotSerializableException;
import java.io.Serial;

/**
 * A list model for players in the exploration GUI.
 */
/* package */ final class PlayerListModel extends DefaultListModel<Player> implements MapChangeListener {
	@Serial
	private static final long serialVersionUID = 1L;

	public PlayerListModel(final IExplorationModel model) {
		this.model = model;
		model.getPlayerChoices().forEach(this::addElement);
	}

	private final IExplorationModel model;

	@Override
	public void mapChanged() {
		clear();
		model.getPlayerChoices().forEach(this::addElement);
	}

	@Override
	public void mapMetadataChanged() {
	}

	@Serial
	private void readObject(final java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
		throw new NotSerializableException("drivers.exploration.PlayerListModel");
	}

	@Serial
	private void writeObject(final java.io.ObjectOutputStream out) throws java.io.IOException {
		throw new NotSerializableException("drivers.exploration.PlayerListModel");
	}
}
