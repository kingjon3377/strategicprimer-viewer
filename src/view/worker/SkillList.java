package view.worker;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.eclipse.jdt.annotation.Nullable;

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
		final SkillListModel lmodel = new SkillListModel(sources);
		setModel(lmodel);
		lmodel.addPropertyChangeListener(new PropertyChangeListener() {
			/**
			 * @param evt the event to handle
			 */
			@Override
			public void propertyChange(@Nullable final PropertyChangeEvent evt) {
				if (evt != null && "finished".equalsIgnoreCase(evt.getPropertyName())) {
					if (Integer.valueOf(0).equals(evt.getNewValue())) {
						setSelectedIndex(0);
					} else if (evt.getNewValue() instanceof Skill) {
						setSelectedValue(evt.getNewValue(), true);
					}
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
	public void valueChanged(@Nullable final ListSelectionEvent evt) {
		firePropertyChange("skill", null, getSelectedValue());
	}
}
