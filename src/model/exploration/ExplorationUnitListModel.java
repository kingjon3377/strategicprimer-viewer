package model.exploration;

import java.util.List;

import javax.swing.DefaultListModel;

import model.listeners.PlayerChangeListener;
import model.map.Player;
import model.map.fixtures.mobile.IUnit;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A unit list model for the exploration GUI.
 *
 * @author Jonathan Lovelace
 *
 */
public class ExplorationUnitListModel extends DefaultListModel<IUnit> implements
		PlayerChangeListener {
	/**
	 * The exploration model to work from.
	 */
	private final IExplorationModel model;
	/**
	 * Constructor.
	 *
	 * @param emodel the exploration model, so we can select the unit the user
	 *        selects
	 */
	public ExplorationUnitListModel(final IExplorationModel emodel) {
		model = emodel;
	}

	/**
	 * Called when the current player has changed.
	 *
	 * @param old the previous current player
	 * @param newPlayer the new current player
	 */
	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		clear();
		final List<IUnit> units = model.getUnits(newPlayer);
		for (final IUnit unit : units) {
			addElement(unit);
		}
	}
}
