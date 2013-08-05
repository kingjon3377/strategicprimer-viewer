package view.worker;

import javax.swing.JTree;

import model.workermgmt.JobTreeModel;

import util.PropertyChangeSource;
/**
 * A tree representing a worker's Jobs and Skills.
 * @author Jonathan Lovelace
 */
public class JobsTree extends JTree implements PropertyChangeSource {
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
	}
}
