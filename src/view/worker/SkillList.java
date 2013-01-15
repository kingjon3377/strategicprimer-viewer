package view.worker;

import java.beans.PropertyChangeListener;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.map.fixtures.mobile.worker.Skill;
import model.workermgmt.SkillListModel;
import util.PropertyChangeSource;
/**
 * A visual list of a job's skills. Also handles listening for selection changes.
 * @author Jonathan Lovelace
 *
 */
public class SkillList extends JList<Skill> implements PropertyChangeSource,
		ListSelectionListener {
	/**
	 * Constructor.
	 * @param listener something that should listen to us
	 * @param sources what our model should listen to
	 */
	public SkillList(final PropertyChangeListener listener, final PropertyChangeSource... sources) {
		setModel(new SkillListModel(sources));
		addPropertyChangeListener(listener);
		addListSelectionListener(this);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	/**
	 * @param evt an event indicating the selection changed.
	 */
	@Override
	public void valueChanged(final ListSelectionEvent evt) {
		firePropertyChange("skill", null, getSelectedValue());
	}
}
