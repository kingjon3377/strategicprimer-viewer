package drivers.advancement;

import org.jetbrains.annotations.Nullable;

import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeSelectionEvent;

import javax.swing.JTree;

import javax.swing.tree.TreePath;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import legacy.map.fixtures.mobile.worker.ISkill;
import legacy.map.fixtures.mobile.worker.IJob;

/**
 * A tree representing a worker's Jobs and Skills.
 *
 * TODO: Can we split our special code out of this class, perhaps into a
 * TreeSelectionModel, so we can make callers use a bog-standard JTree?
 */
/* package */ final class JobsTree extends JTree implements SkillSelectionSource {
	@Serial
	private static final long serialVersionUID = 1L;

	public JobsTree(final JobTreeModel jtModel) {
		super(jtModel);

		jtModel.setSelectionModel(getSelectionModel());

		setRootVisible(false);

		for (int i = 0; i < getRowCount(); i++) {
			expandRow(i);
		}

		setShowsRootHandles(true);
		selectionModel.addTreeSelectionListener(this::handleTreeSelectionChange);

		jtModel.addTreeModelListener(new ExpansionModelListener());
	}

	private final Collection<SkillSelectionListener> listeners = new ArrayList<>();

	@Override
	public void addSkillSelectionListener(final SkillSelectionListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeSkillSelectionListener(final SkillSelectionListener listener) {
		listeners.remove(listener);
	}

	private void handleTreeSelectionChange(final TreeSelectionEvent event) {
		final @Nullable ISkill retval;
		final @Nullable IJob job;
		final TreePath selectionPath = event.getNewLeadSelectionPath();
		if (!Objects.isNull(selectionPath) && selectionPath.getLastPathComponent() instanceof ISkill) {
			retval = (ISkill) selectionPath.getLastPathComponent();
			final Object[] path = selectionPath.getPath();
			if (path.length < 2) {
				job = null;
			} else {
				job = Optional.of(path[path.length - 2]).filter(IJob.class::isInstance)
						.map(IJob.class::cast).orElse(null);
			}
		} else {
			retval = null;
			job = null;
		}
		for (final SkillSelectionListener listener : listeners) {
			listener.selectJob(job);
			listener.selectSkill(retval);
		}
	}

	private final class ExpansionModelListener implements TreeModelListener {
		@Override
		public void treeStructureChanged(final TreeModelEvent event) {
			Optional.ofNullable(event.getTreePath())
					.map(TreePath::getParentPath)
					.ifPresent(JobsTree.this::expandPath);
			// FIXME: Why this loop here?
			for (int i = 0; i < getRowCount(); i++) {
				expandRow(i);
			}
		}

		@Override
		public void treeNodesRemoved(final TreeModelEvent event) {
		}

		@Override
		public void treeNodesInserted(final TreeModelEvent event) {
			expandPath(event.getTreePath());
			expandPath(event.getTreePath().getParentPath());
		}

		@Override
		public void treeNodesChanged(final TreeModelEvent event) {
			expandPath(event.getTreePath().getParentPath());
		}
	}
}
