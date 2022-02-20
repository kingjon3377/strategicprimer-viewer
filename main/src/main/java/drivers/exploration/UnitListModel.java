package drivers.exploration;

import java.util.logging.Logger;
import javax.swing.DefaultListModel;

import common.map.fixtures.mobile.IUnit;

import drivers.common.PlayerChangeListener;

import common.map.Player;

import exploration.common.IExplorationModel;
import org.jetbrains.annotations.Nullable;

/**
 * The list model for the list of units to choose the explorer (or otherwise moving unit) from.
 */
/* package */ class UnitListModel extends DefaultListModel<IUnit> implements PlayerChangeListener {
	private static final Logger LOGGER = Logger.getLogger(UnitListModel.class.getName());

	public UnitListModel(final IExplorationModel model) {
		this.model = model;
	}

	private final IExplorationModel model;

	@Override
	public void playerChanged(final @Nullable Player old, final Player newPlayer) {
		LOGGER.finer("Regenerating UnitListModel"); // TODO: move to below equality check?
		if (old != null && old.equals(newPlayer)) {
			return;
		}
		clear();
		model.getUnits(newPlayer).forEach(this::addElement);
	}
}
