package model.exploration;

import java.util.List;

import javax.swing.DefaultListModel;

import model.listeners.PlayerChangeListener;
import model.listeners.PlayerChangeSource;
import model.map.Player;
import model.map.fixtures.mobile.Unit;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A unit list model for the exploration GUI.
 *
 * @author Jonathan Lovelace
 *
 */
public class ExplorationUnitListModel extends DefaultListModel<Unit> implements
		PlayerChangeListener {
	/**
	 * Constructor.
	 *
	 * @param emodel the exploration model, so we can select the unit the user
	 *        selects
	 * @param source what to listen to for property-change events.
	 */
	public ExplorationUnitListModel(final IExplorationModel emodel,
			final PlayerChangeSource source) {
		model = emodel;
		source.addPlayerChangeListener(this);
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
		final List<Unit> units = model.getUnits(newPlayer);
		for (final Unit unit : units) {
			addElement(unit);
		}
	}
	/**
	 * The exploration model to work from.
	 */
	private final IExplorationModel model;
}
