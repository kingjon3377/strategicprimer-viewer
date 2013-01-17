package view.worker;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.PropertyChangeSource;

import model.map.fixtures.UnitMember;
import model.workermgmt.UnitMemberListModel;
/**
 * A visual list of a unit's members. We'll also handle listening for selection changes.
 * @author Jonathan Lovelace
 *
 */
public class UnitMemberList extends JList<UnitMember> implements
		PropertyChangeSource, ListSelectionListener {
	/**
	 * Constructor.
	 * @param listener something to listen to us
	 * @param sources what our model should listen to
	 */
	public UnitMemberList(final PropertyChangeListener listener,
			final PropertyChangeSource... sources) {
		final UnitMemberListModel lmodel = new UnitMemberListModel(sources);
		setModel(lmodel);
		lmodel.addPropertyChangeListener(new PropertyChangeListener() {
			/**
			 * @param evt the event to handle
			 */
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				if ("finished".equalsIgnoreCase(evt.getPropertyName())
						&& Integer.valueOf(0).equals(evt.getNewValue())) {
					setSelectedIndex(0);
				}
			}
		});
		addPropertyChangeListener(listener);
		addListSelectionListener(this);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	/**
	 * @param evt an event indicating the selection changed.
	 */
	@Override
	public void valueChanged(final ListSelectionEvent evt) {
		firePropertyChange("member", null, getSelectedValue());
	}
}
