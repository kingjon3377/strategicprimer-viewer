package view.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import model.listeners.SkillSelectionListener;
import model.listeners.SkillSelectionSource;
import model.listeners.UnitSelectionSource;
import model.map.fixtures.mobile.worker.Skill;
import model.workermgmt.JobTreeModel;

import org.eclipse.jdt.annotation.Nullable;

import util.TypesafeLogger;
import view.util.AddRemovePanel;

/**
 * A tree representing a worker's Jobs and Skills.
 *
 * @author Jonathan Lovelace
 */
public class JobsTree extends JTree implements TreeSelectionListener,
		SkillSelectionSource {
	/**
	 * Constructor.
	 *
	 * @param sources things for the model to listen to for property changes.
	 * @param src ignored for now, TODO: figure out what should be done with it.
	 */
	public JobsTree(final AddRemovePanel[] sources, final UnitSelectionSource src) {
		super();
		final TreeSelectionModel tsm = getSelectionModel();
		if (tsm == null) {
			throw new IllegalStateException("Selection model is null somehow");
		}
		final JobTreeModel model = new JobTreeModel(tsm);
		setModel(model);
		for (final AddRemovePanel source : sources) {
			source.addAddRemoveListener(model);
		}
		setRootVisible(false);
		setShowsRootHandles(true);
		getSelectionModel().addTreeSelectionListener(this);
	}

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(JobsTree.class);

	/**
	 * Fire the 'skill' property with the current selection if it's a Skill, or
	 * null if not.
	 *
	 * @param evt the selection event to handle
	 */
	@Override
	public void valueChanged(@Nullable final TreeSelectionEvent evt) {
		if (evt != null) {
			final TreePath selPath = evt.getNewLeadSelectionPath();
			// ESCA-JAVA0177:
			final Object component; // NOPMD
			if (selPath == null) {
				LOGGER.warning("Selection path was null.");
				component = null;
			} else {
				component = selPath.getLastPathComponent();
			}
			final Skill retval = component instanceof Skill ? (Skill) component : null;
			for (final SkillSelectionListener list : ssListeners) {
				list.selectSkill(retval);
			}
		}
	}

	/**
	 * The list of completion listeners listening to us.
	 */
	private final List<SkillSelectionListener> ssListeners = new ArrayList<>();

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addSkillSelectionListener(final SkillSelectionListener list) {
		ssListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removeSkillSelectionListener(final SkillSelectionListener list) {
		ssListeners.remove(list);
	}
}
