package model.workermgmt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import javax.swing.DefaultListModel;

import model.map.Player;
import model.map.fixtures.mobile.Unit;
import util.PropertyChangeSource;

/**
 * A list-model for the list of a player's units.
 * @author Jonathan Lovelace
 *
 */
public class UnitListModel extends DefaultListModel<Unit> implements
		PropertyChangeListener, PropertyChangeSource {
	/**
	 * The current player.
	 */
	private Player player = new Player(-1, "none");
	/**
	 * The map model to work from.
	 */
	private final IWorkerModel model;
	/**
	 * Constructor.
	 * @param mmodel the map model to work from
	 * @param sources property change sources to listen to
	 */
	public UnitListModel(final IWorkerModel mmodel, final PropertyChangeSource... sources) {
		super();
		model = mmodel;
		if (sources.length == 0) {
			throw new IllegalStateException("No sources given");
		}
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
			final List<Unit> units = model.getUnits(player);
			for (Unit unit : units) {
				addElement(unit);
			}
			pcs.firePropertyChange("finished", null, isEmpty() ? Integer.valueOf(-1) : Integer.valueOf(0));
		}
	}
	/**
	 * Our delegate for property-change handling.
	 */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	/**
	 * @param list a listener to listen to us
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener list) {
		pcs.addPropertyChangeListener(list);
	}
	/**
	 * @param list a listener to stop listenng to us
	 */
	@Override
	public void removePropertyChangeListener(final PropertyChangeListener list) {
		pcs.removePropertyChangeListener(list);
	}
}
