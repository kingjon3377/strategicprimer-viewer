package drivers.exploration;

import javax.swing.DefaultListModel;

import common.map.Player;

import drivers.common.MapChangeListener;

import exploration.common.IExplorationModel;

/**
 * A list model for players in the exploration GUI.
 */
/* package */ class PlayerListModel extends DefaultListModel<Player> implements MapChangeListener {
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
	public void mapMetadataChanged() {}
}
