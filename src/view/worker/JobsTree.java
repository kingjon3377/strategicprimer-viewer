package view.worker;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import model.map.fixtures.mobile.worker.Skill;
import model.workermgmt.JobTreeModel;
import util.PropertyChangeSource;
/**
 * A tree representing a worker's Jobs and Skills.
 * @author Jonathan Lovelace
 */
public class JobsTree extends JTree implements PropertyChangeSource, TreeSelectionListener {
	/**
	 * Constructor.
	 * @param sources things for the model to listen to for property changes.
	 */
	public JobsTree(final PropertyChangeSource... sources) {
		super();
		setModel(new JobTreeModel(getSelectionModel()));
		final JobTreeModel model = (JobTreeModel) getModel();
		for (final PropertyChangeSource source : sources) {
			source.addPropertyChangeListener(model);
		}
		setRootVisible(false);
		setShowsRootHandles(true);
		getSelectionModel().addTreeSelectionListener(this);
	}
	/**
	 * Fire the 'skill' property with the current selection if it's a Skill, or null if not.
	 * @param evt the selection event to handle
	 */
	@Override
	public void valueChanged(final TreeSelectionEvent evt) {
		final Object component = evt.getNewLeadSelectionPath().getLastPathComponent();
		if (component instanceof Skill) {
			firePropertyChange("skill", null, component);
		} else {
			firePropertyChange("skill", "", null);
		}
	}
}
