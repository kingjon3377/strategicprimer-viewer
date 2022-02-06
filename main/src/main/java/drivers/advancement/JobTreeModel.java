package drivers.advancement;

import javax.swing.tree.DefaultTreeSelectionModel;
import org.jetbrains.annotations.Nullable;

import common.map.HasName;

import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;

import common.map.fixtures.mobile.IWorker;

import drivers.worker_mgmt.UnitMemberListener;

import common.map.fixtures.UnitMember;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import common.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.ISkill;

import drivers.common.IAdvancementModel;

import java.util.logging.Logger;

import java.util.stream.StreamSupport;

/**
 * A model for a tree of a worker's Jobs and Skills.
 */
/* package */ class JobTreeModel implements TreeModel, UnitMemberListener, AddRemoveListener {
	private static final Logger LOGGER = Logger.getLogger(JobTreeModel.class.getName());

	public JobTreeModel(final IAdvancementModel driverModel) {
		this.driverModel = driverModel;
	}

	private final IAdvancementModel driverModel;

	private final List<TreeModelListener> listeners = new ArrayList<>();

	/**
	 * The worker whom the Jobs and Skills describe.
	 */
	@Nullable
	private IWorker localRoot = null;

	private TreeSelectionModel selectionModel = new DefaultTreeSelectionModel(); // TODO: refactor so this can be provided in the constructor, if possible; it's currently provided by the tree, to whose constructor this tree-model object is passed

	public void setSelectionModel(final TreeSelectionModel selectionModel) {
		this.selectionModel = selectionModel;
	}

	@Override
	@Nullable
	public IWorker getRoot() {
		return localRoot;
	}

	@Override
	public HasName getChild(final Object parent, final int index) {
		if (index >= 0 && (parent instanceof IWorker || parent instanceof IJob)) {
			return StreamSupport.stream(
					((Iterable<? extends HasName>) parent).spliterator(), false)
				.skip(index - 1).findFirst()
				.orElseThrow(() -> new ArrayIndexOutOfBoundsException(
					"Parent does not have that child"));
		} else {
			throw new ArrayIndexOutOfBoundsException("Parent does not have that child");
		}
	}

	@Override
	public int getChildCount(final Object parent) {
		if (parent instanceof IWorker || parent instanceof IJob) {
			return (int) StreamSupport.stream(
					((Iterable<? extends HasName>) parent).spliterator(), false)
				.count();
		} else if (parent instanceof ISkill) {
			return 0;
		} else {
			throw new IllegalArgumentException("Unexpected element type");
		}
	}

	@Override
	public boolean isLeaf(final Object node) {
		if (node instanceof IWorker || node instanceof IJob) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Handling changed values is not yet implemented.
	 *
	 * TODO: Implement if necessary
	 */
	@Override
	public void valueForPathChanged(final TreePath path, final Object newValue) {
		LOGGER.severe("valueForPathChanged needs to be implemented");
	}

	@Override
	public int getIndexOfChild(final Object parent, final Object child) {
		if (parent instanceof IWorker || parent instanceof IJob) {
			int i = 0;
			for (Object ch : (Iterable<? extends HasName>) parent) {
				if (Objects.equals(ch, child)) {
					return i;
				}
				i++;
			}
		}
		return -1;
	}

	@Override
	public void addTreeModelListener(final TreeModelListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeTreeModelListener(final TreeModelListener listener) {
		listeners.remove(listener);
	}

	private void fireTreeNodesInserted(final TreeModelEvent event) {
		for (TreeModelListener listener : listeners) {
			listener.treeNodesInserted(event);
		}
	}

	private void fireTreeStructureChanged(final TreeModelEvent event) {
		for (TreeModelListener listener : listeners) {
			listener.treeStructureChanged(event);
		}
	}

	/**
	 * Add a new Job or Skill.
	 *
	 * TODO: Show error dialog, or at least visual-beep, instead of just logging warnings?
	 */
	@Override
	public void add(final String category, final String addendum) {
		IWorker currentRoot = localRoot;
		if ("job".equals(category)) {
			if (currentRoot == null) {
				LOGGER.warning("Can't add a new Job when no worker selected");
			} else {
				int childCount = getChildCount(currentRoot);
				if (driverModel.addJobToWorker(currentRoot, addendum)) {
					IJob job = StreamSupport.stream(currentRoot.spliterator(), false)
						.filter(j -> addendum.equals(j.getName()))
						.findAny().orElse(null);
					if (job != null) {
						// TODO: Check for no-op before firing event ...
						fireTreeNodesInserted(new TreeModelEvent(this,
							new TreePath(currentRoot), new int[] { childCount },
							new Object[] { job }));
					} else {
						LOGGER.warning("Worker not found");
					}
				} else {
					LOGGER.warning("Worker not found");
				}
			}
		} else if ("skill".equals(category)) {
			TreePath selectionPath = selectionModel.getSelectionPath();
			if (currentRoot != null && selectionPath != null &&
					selectionPath.getLastPathComponent() instanceof IJob) {
				IJob job = (IJob) selectionPath.getLastPathComponent();
				int childCount = getChildCount(job);
				if (driverModel.addHoursToSkill(currentRoot, job.getName(), addendum,
						0, 200)) {
					ISkill skill = StreamSupport.stream(job.spliterator(), false)
						.filter(s -> addendum.equals(s.getName()))
						.findAny().orElse(null);
					if (skill != null) {
						fireTreeNodesInserted(new TreeModelEvent(this,
							new TreePath(new Object[] { localRoot, job }),
							new int[] { childCount }, new Object[] { skill }));
					} else {
						LOGGER.warning(
							"Worker not found, or skill-adding otherwise failed");
					}
				} else {
					LOGGER.warning("Worker not found, or skill-adding otherwise failed");
				}
			} else {
				LOGGER.warning("Can't add a new Skill when no Job selected");
			}
		} else {
			LOGGER.warning(String.format("Don't know how to add a new '%s", category));
		}
	}

	/**
	 * Change what unit member is currently selected
	 */
	@Override
	public void memberSelected(@Nullable final UnitMember old, @Nullable final UnitMember selected) {
		if (selected instanceof IWorker) {
			localRoot = (IWorker) selected;
			fireTreeStructureChanged(new TreeModelEvent(this, new TreePath(selected)));
		} else {
			localRoot = null;
			fireTreeStructureChanged(new TreeModelEvent(this, (TreePath) null));
		}
	}
}
