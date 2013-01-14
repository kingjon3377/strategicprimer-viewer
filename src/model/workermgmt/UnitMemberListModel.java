package model.workermgmt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultListModel;

import util.PropertyChangeSource;

import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
/**
 * A list moel for a list of a unit's members.
 * @author Jonathan Lovelace
 *
 */
public class UnitMemberListModel extends DefaultListModel<UnitMember> implements
		PropertyChangeListener {
	/**
	 * The current unit.
	 */
	private Unit unit = null;
	/**
	 * Constructor.
	 * @param sources property-change sources to listen to.
	 */
	public UnitMemberListModel(final PropertyChangeSource... sources) {
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
				}
			} else if (unit != null && evt.getNewValue() == null) {
				unit = (Unit) evt.getNewValue();
				clear();
			}
		}
	}
}
