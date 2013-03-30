package model.exploration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.DefaultListModel;

import model.map.Player;
import model.map.fixtures.mobile.Unit;
import util.PropertyChangeSource;
/**
 * A unit list model for the exploration GUI.
 * @author Jonathan Lovelace
 *
 */
public class ExplorationUnitListModel extends DefaultListModel<Unit> implements
		PropertyChangeListener {
	/**
	 * Constructor.
	 * @param emodel the exploration model, so we can select the unit the user selects
	 * @param source what to listen to for property-change events.
	 */
	public ExplorationUnitListModel(final IExplorationModel emodel,
			final PropertyChangeSource source) {
		model = emodel;
		source.addPropertyChangeListener(this);
	}
	/**
	 * Handle a property change.
	 * @param evt the event to handle.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("player".equalsIgnoreCase(evt.getPropertyName())
				&& evt.getNewValue() instanceof Player
				&& !player.equals(evt.getNewValue())) {
			player = (Player) evt.getNewValue();
			clear();
			final List<Unit> units = model.getUnits(player);
			for (Unit unit : units) {
				addElement(unit);
			}
		}
	}
	/**
	 * The current player.
	 */
	private Player player = new Player(-1, "none");
	/**
	 * The exploration model to work from.
	 */
	private final IExplorationModel model;
}
