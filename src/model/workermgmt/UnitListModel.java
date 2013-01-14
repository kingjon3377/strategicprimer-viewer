package model.workermgmt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.DefaultListModel;

import model.map.Player;
import model.map.fixtures.mobile.Unit;
import model.viewer.MapModel;
import util.PropertyChangeSource;
import controller.map.misc.MapHelper;

/**
 * A list-model for the list of a player's units.
 * @author Jonathan Lovelace
 *
 */
public class UnitListModel extends DefaultListModel<Unit> implements
		PropertyChangeListener {
	/**
	 * The current player.
	 */
	private Player player = new Player(-1, "none");
	/**
	 * The map model to work from.
	 */
	private final MapModel model;
	/**
	 * The helper to use to get the list of units from the model.
	 */
	private final MapHelper helper = new MapHelper();
	/**
	 * Constructor.
	 * @param mmodel the map model to work from
	 * @param sources property change sources to listen to
	 */
	public UnitListModel(final MapModel mmodel, final PropertyChangeSource... sources) {
		super();
		model = mmodel;
		for (final PropertyChangeSource source : sources) {
			source.addPropertyChangeListener(this);
		}
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
			final List<Unit> units = helper.getUnits(model.getMainMap(), player);
			for (Unit unit : units) {
				addElement(unit);
			}
		}
	}
}
