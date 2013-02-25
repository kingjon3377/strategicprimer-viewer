package view.worker;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.map.fixtures.mobile.Unit;
import model.misc.IDriverModel;
import model.workermgmt.UnitListModel;
import util.PropertyChangeSource;
/**
 * A visual list of the units in a map. We'll also handle listening for selection changes.
 * @author Jonathan Lovelace
 *
 */
public class UnitList extends JList<Unit> implements PropertyChangeSource, ListSelectionListener {
	/**
	 * Constructor.
	 * @param mmodel the map model to pass to our model
	 * @param listener something to listen to us
	 * @param sources what our model should listen to
	 */
	public UnitList(final IDriverModel mmodel,
			final PropertyChangeListener listener,
			final PropertyChangeSource... sources) {
		final UnitListModel lmodel = new UnitListModel(mmodel, sources);
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
		firePropertyChange("unit", null, getSelectedValue());
	}
}
