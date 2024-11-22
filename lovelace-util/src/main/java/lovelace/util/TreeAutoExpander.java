package lovelace.util;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * A tree model listener to force new or changed nodes to start expanded.
 *
 * @author Jonathan Lovelace
 */
public class TreeAutoExpander implements TreeModelListener {
	private final Consumer<TreePath> expandPath;
	private final IntSupplier getRowCount;
	private final IntConsumer expandRow;
	public TreeAutoExpander(final Consumer<TreePath> expandPath, final IntSupplier getRowCount,
	                        final IntConsumer expandRow) {
		this.expandPath = expandPath;
		this.getRowCount = getRowCount;
		this.expandRow = expandRow;
	}
	@Override
	public void treeStructureChanged(final TreeModelEvent event) {
		Optional.ofNullable(event.getTreePath())
				.map(TreePath::getParentPath)
				.ifPresent(expandPath);
		// FIXME: Do we really need to expand *all* rows after expanding the node that changed and all its parents?
		// TODO: Maybe in an ifPresentOrElse() call?
		for (int i = 0; i < getRowCount.getAsInt(); i++) {
			expandRow.accept(i);
		}
	}

	@Override
	public void treeNodesRemoved(final TreeModelEvent event) {
	}

	@Override
	public void treeNodesInserted(final TreeModelEvent event) {
		expandPath.accept(event.getTreePath());
		expandPath.accept(event.getTreePath().getParentPath());
	}

	@Override
	public void treeNodesChanged(final TreeModelEvent event) {
		expandPath.accept(event.getTreePath().getParentPath());
	}
}
