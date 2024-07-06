package drivers.advancement;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import javax.swing.tree.DefaultTreeSelectionModel;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import common.map.HasName;

import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;

import legacy.map.fixtures.mobile.IWorker;

import drivers.worker_mgmt.UnitMemberListener;

import legacy.map.fixtures.UnitMember;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import legacy.map.fixtures.mobile.worker.IJob;
import legacy.map.fixtures.mobile.worker.ISkill;

import drivers.common.IAdvancementModel;

import java.util.stream.StreamSupport;

/**
 * A model for a tree of a worker's Jobs and Skills.
 */
/* package */ class JobTreeModel implements TreeModel, UnitMemberListener, AddRemoveListener {
	public JobTreeModel(final IAdvancementModel driverModel) {
		this.driverModel = driverModel;
	}

	private final IAdvancementModel driverModel;

	private final Collection<TreeModelListener> listeners = new ArrayList<>();

	/**
	 * The worker whom the Jobs and Skills describe.
	 */
	private @Nullable IWorker localRoot = null;

	// TODO: refactor so this can be provided in the constructor, if possible; it's currently provided by the tree, to
	//  whose constructor this tree-model object is passed
	private TreeSelectionModel selectionModel = new DefaultTreeSelectionModel();

	public void setSelectionModel(final TreeSelectionModel selectionModel) {
		this.selectionModel = selectionModel;
	}

	@Override
	public @Nullable IWorker getRoot() {
		return localRoot;
	}

	@Override
	public HasName getChild(final Object parent, final int index) {
		final Function<Iterable<? extends HasName>, HasName> impl =
				par -> StreamSupport.stream(par.spliterator(), false).skip(index - 1).findFirst()
						.orElseThrow(() -> new ArrayIndexOutOfBoundsException("Parent does not have that child"));
		return switch (parent) {
			case final IWorker w when index >= 0 -> impl.apply(w);
			case final IJob j when index >= 0 -> impl.apply(j);
			default -> throw new ArrayIndexOutOfBoundsException("Parent does not have that child");
		};
	}

	@Override
	public int getChildCount(final Object parent) {
		final ToIntFunction<Iterable<? extends HasName>> impl =
				par -> (int) StreamSupport.stream(par.spliterator(), false).count();
		return switch (parent) {
			case final IWorker w -> impl.applyAsInt(w);
			case final IJob j -> impl.applyAsInt(j);
			case final ISkill iSkill -> 0;
			default -> throw new IllegalArgumentException("Unexpected element type");
		};
	}

	@Override
	public boolean isLeaf(final Object node) {
		return !(node instanceof IWorker) && !(node instanceof IJob);
	}

	/**
	 * Handling changed values is not yet implemented.
	 *
	 * TODO: Implement if necessary
	 */
	@Override
	public void valueForPathChanged(final TreePath path, final Object newValue) {
		LovelaceLogger.error("valueForPathChanged needs to be implemented");
	}

	@Override
	public int getIndexOfChild(final Object parent, final Object child) {
		if (parent instanceof IWorker || parent instanceof IJob) {
			int i = 0;
			for (final Object ch : (Iterable<?>) parent) {
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
		for (final TreeModelListener listener : listeners) {
			listener.treeNodesInserted(event);
		}
	}

	private void fireTreeStructureChanged(final TreeModelEvent event) {
		for (final TreeModelListener listener : listeners) {
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
		final IWorker currentRoot = localRoot;
		if ("job".equals(category)) {
			if (Objects.isNull(currentRoot)) {
				LovelaceLogger.warning("Can't add a new Job when no worker selected");
			} else if (StreamSupport.stream(currentRoot.spliterator(), false).map(IJob::getName)
					.anyMatch(addendum::equals)) {
				LovelaceLogger.info("Addition would be no-op");
			} else {
				final int childCount = getChildCount(currentRoot);
				if (driverModel.addJobToWorker(currentRoot, addendum)) {
					final IJob job = StreamSupport.stream(currentRoot.spliterator(), false)
							.filter(j -> addendum.equals(j.getName()))
							.findAny().orElse(null);
					if (Objects.isNull(job)) {
						LovelaceLogger.warning("Worker not found");
					} else {
						fireTreeNodesInserted(new TreeModelEvent(this,
								new TreePath(currentRoot), new int[]{childCount},
								new Object[]{job}));
					}
				} else {
					LovelaceLogger.warning("Worker not found");
				}
			}
		} else if ("skill".equals(category)) {
			final TreePath selectionPath = selectionModel.getSelectionPath();
			if (!Objects.isNull(currentRoot) && !Objects.isNull(selectionPath) &&
					selectionPath.getLastPathComponent() instanceof final IJob job) {
				final int childCount = getChildCount(job);
				if (driverModel.addHoursToSkill(currentRoot, job.getName(), addendum,
						0, 200)) {
					final ISkill skill = StreamSupport.stream(job.spliterator(), false)
							.filter(s -> addendum.equals(s.getName()))
							.findAny().orElse(null);
					if (Objects.isNull(skill)) {
						LovelaceLogger.warning(
								"Worker not found, or skill-adding otherwise failed");
					} else {
						fireTreeNodesInserted(new TreeModelEvent(this,
								new TreePath(new Object[]{currentRoot, job}),
								new int[]{childCount}, new Object[]{skill}));
					}
				} else {
					LovelaceLogger.warning("Worker not found, or skill-adding otherwise failed");
				}
			} else {
				LovelaceLogger.warning("Can't add a new Skill when no Job selected");
			}
		} else {
			LovelaceLogger.warning("Don't know how to add a new '%s", category);
		}
	}

	/**
	 * Change what unit member is currently selected
	 */
	@Override
	public void memberSelected(final @Nullable UnitMember old, final @Nullable UnitMember selected) {
		if (selected instanceof final IWorker w) {
			localRoot = w;
			fireTreeStructureChanged(new TreeModelEvent(this, new TreePath(selected)));
		} else {
			localRoot = null;
			fireTreeStructureChanged(new TreeModelEvent(this, (TreePath) null));
		}
	}
}
