package drivers.advancement;

import lovelace.util.TreeAutoExpander;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.TreeSelectionEvent;

import javax.swing.JTree;

import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

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

		jtModel.addTreeModelListener(new TreeAutoExpander(super::expandPath, super::getRowCount, super::expandRow));
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
}
