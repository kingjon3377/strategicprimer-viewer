package view.worker;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import model.listeners.AddRemoveListener;
import model.listeners.SkillSelectionListener;
import model.listeners.SkillSelectionSource;
import model.listeners.UnitMemberListener;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.Skill;
import model.workermgmt.JobTreeModel;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A tree representing a worker's Jobs and Skills.
 *
 * @author Jonathan Lovelace
 */
public final class JobsTree extends JTree implements TreeSelectionListener,
		SkillSelectionSource, AddRemoveListener, UnitMemberListener {
	/**
	 * The list of completion listeners listening to us.
	 */
	private final List<SkillSelectionListener> ssListeners = new ArrayList<>();
	/**
	 * The tree model.
	 */
	private final JobTreeModel model;

	/**
	 * Constructor.
	 */
	public JobsTree() {
		super();
		final TreeSelectionModel tsm = getSelectionModel();
		if (tsm == null) {
			throw new IllegalStateException("Selection model is null somehow");
		}
		model = new JobTreeModel(tsm);
		setModel(model);
		final JTree tree = this;
		model.addTreeModelListener(new TreeModelListener() {
			@Override
			public void treeStructureChanged(@Nullable final TreeModelEvent e) {
				if (e == null) {
					return;
				}
				tree.expandPath(e.getTreePath().getParentPath());
				for (int i = 0; i < getRowCount(); i++) {
					expandRow(i);
				}
			}
			@Override
			public void treeNodesRemoved(@Nullable final TreeModelEvent e) {
				// Ignored
			}
			@Override
			public void treeNodesInserted(@Nullable final TreeModelEvent e) {
				if (e == null) {
					return;
				}
				tree.expandPath(e.getTreePath());
				tree.expandPath(e.getTreePath().getParentPath());
			}
			@Override
			public void treeNodesChanged(@Nullable final TreeModelEvent e) {
				if (e == null) {
					return;
				}
				tree.expandPath(e.getTreePath().getParentPath());
			}
		});
		setRootVisible(false);
		for (int i = 0; i < getRowCount(); i++) {
			expandRow(i);
		}
		setShowsRootHandles(true);
		getSelectionModel().addTreeSelectionListener(this);
	}

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
			final Skill retval;
			if (selPath == null) {
				retval = null;
			} else {
				final Object component = selPath.getLastPathComponent();
				if (component instanceof Skill) {
					retval = (Skill) component;
				} else {
					retval = null;
				}
			}
			for (final SkillSelectionListener list : ssListeners) {
				list.selectSkill(retval);
			}
		}
	}

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
	/**
	 * @param category passed to tree model
	 * @param addendum passed to tree model
	 */
	@Override
	public void add(final String category, final String addendum) {
		model.add(category, addendum);
	}
	/**
	 * @param category passed to tree model
	 */
	@Override
	public void remove(final String category) {
		model.remove(category);
	}
	/**
	 * @param old passed to tree model
	 * @param selected passed to tree model
	 */
	@Override
	public void memberSelected(@Nullable final UnitMember old,
			@Nullable final UnitMember selected) {
		model.memberSelected(old, selected);
	}
}
