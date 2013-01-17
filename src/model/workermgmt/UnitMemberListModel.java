package model.workermgmt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.DefaultListModel;

import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import util.PropertyChangeSource;
/**
 * A list moel for a list of a unit's members.
 * @author Jonathan Lovelace
 *
 */
public class UnitMemberListModel extends DefaultListModel<UnitMember> implements
		PropertyChangeListener, PropertyChangeSource {
	/**
	 * The current unit.
	 */
	private Unit unit = null;
	/**
	 * Constructor.
	 * @param sources property-change sources to listen to.
	 */
	public UnitMemberListModel(final PropertyChangeSource... sources) {
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
		if ("unit".equalsIgnoreCase(evt.getPropertyName())) {
			if (evt.getNewValue() instanceof Unit) {
				if (unit == null || !unit.equals(evt.getNewValue())) {
					clear();
					unit = (Unit) evt.getNewValue();
					for (UnitMember member : unit) {
						addElement(member);
					}
					pcs.firePropertyChange("finished", null, isEmpty() ? Integer.valueOf(-1) : Integer.valueOf(0));
				}
			} else if (unit != null && evt.getNewValue() == null) {
				unit = (Unit) evt.getNewValue();
				clear();
			}
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
