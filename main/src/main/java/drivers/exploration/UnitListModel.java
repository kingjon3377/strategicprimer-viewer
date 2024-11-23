package drivers.exploration;

import javax.swing.DefaultListModel;

import legacy.map.fixtures.mobile.IUnit;

import drivers.common.PlayerChangeListener;

import legacy.map.Player;

import exploration.common.IExplorationModel;
import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;

/**
 * The list model for the list of units to choose the explorer (or otherwise moving unit) from.
 */
/* package */ final class UnitListModel extends DefaultListModel<IUnit> implements PlayerChangeListener {
	@Serial
	private static final long serialVersionUID = 1L;

	public UnitListModel(final IExplorationModel model) {
		this.model = model;
	}

	private final IExplorationModel model;

	@Override
	public void playerChanged(final @Nullable Player old, final Player newPlayer) {
		if (newPlayer.equals(old)) {
			return;
		}
		LovelaceLogger.trace("Regenerating UnitListModel");
		clear();
		model.getUnits(newPlayer).forEach(this::addElement);
	}
}
