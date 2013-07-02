package view.worker;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import model.workermgmt.IWorkerModel;
import model.workermgmt.IWorkerTreeModel;
import model.workermgmt.UnitMemberTransferable;
import model.workermgmt.UnitMemberTransferable.UnitMemberPair;
import model.workermgmt.WorkerTreeModelAlt;
import model.workermgmt.WorkerTreeModelAlt.UnitMemberNode;
import model.workermgmt.WorkerTreeModelAlt.UnitNode;
/**
 * A tree of a player's units.
 * @author Jonathan Lovelace
 *
 */
public class WorkerTree extends JTree {
	/**
	 * @param player the player whose units we want to see
	 * @param model the driver model to build on
	 */
	public WorkerTree(final Player player, final IWorkerModel model) {
		super(new WorkerTreeModelAlt(player, model));
		setRootVisible(false);
		setDragEnabled(true);
		setShowsRootHandles(true);
		setTransferHandler(new WorkerTreeTransferHandler(getSelectionModel(), (IWorkerTreeModel) getModel()));
		setCellRenderer(new UnitMemberCellRenderer());
	}
	/**
	 * A replacement transfer handler to make drag-and-drop work properly.
	 *
	 * Based on the tutorial found at http://www.javaprogrammingforums.com/java-swing-tutorials/3141-drag-drop-jtrees.html
	 * @author helloworld922
	 * @author Jonathan Lovelace
	 */
	private static class WorkerTreeTransferHandler extends TransferHandler {
		/**
		 * Logger.
		 */
		private static final Logger LOGGER = Logger.getLogger(WorkerTree.WorkerTreeTransferHandler.class.getName());
		/**
		 * Constructor.
		 * @param selmodel the tree's selection model
		 * @param tmodel the tree's data model
		 */
		WorkerTreeTransferHandler(final TreeSelectionModel selmodel, final IWorkerTreeModel tmodel) {
			smodel = selmodel;
			model = tmodel;
		}
		/**
		 * The tree's selection model.
		 */
		private final TreeSelectionModel smodel;
		/**
		 * The tree's data model.
		 */
		private final IWorkerTreeModel model;
		/**
		 * @param component ignored
		 * @return the actions we support
		 */
		@Override
		public int getSourceActions(final JComponent component) {
			return TransferHandler.MOVE;
		}
		/**
		 * @param component the component being dragged from? In any case, ignored.
		 * @return a Transferable representing the selected node, or null if none selected
		 */
		@Override
		protected Transferable createTransferable(final JComponent component) {
			final TreePath path = smodel.getSelectionPath();
			final Object selection = path.getLastPathComponent();
			if (selection instanceof UnitMember) {
				return new UnitMemberTransferable((UnitMember) selection, // NOPMD
						(Unit) path.getPathComponent(path.getPathCount() - 2));
			} else if (selection instanceof UnitMemberNode) {
				return new UnitMemberTransferable(// NOPMD
						(UnitMember) ((UnitMemberNode) selection)
								.getUserObject(),
						(Unit) ((UnitNode) path.getPathComponent(path
								.getPathCount() - 2)).getUserObject());
			} else {
				return null;
			}
		}
		/**
		 * @param support the object containing the detail of the transfer
		 * @return whether the drop is possible
		 */
		@Override
		public boolean canImport(final TransferSupport support) {
			if (support.isDataFlavorSupported(UnitMemberTransferable.FLAVOR)) {
				final DropLocation dloc = support.getDropLocation();
				if (!(dloc instanceof JTree.DropLocation)) {
					return false; // NOPMD
				}
				final TreePath path = ((JTree.DropLocation) dloc).getPath();
				return path != null // NOPMD
						&& (path.getLastPathComponent() instanceof Unit || path
								.getLastPathComponent() instanceof UnitNode);
			} else {
				return false;
			}
		}
		/**
		 * @param support the object containing the details of the transfer
		 * @return whether the transfer succeeded
		 */
		@Override
		public boolean importData(final TransferSupport support) {
			if (canImport(support)) {
				final Transferable trans = support.getTransferable();
				final DropLocation dloc = support.getDropLocation();
				if (!(dloc instanceof JTree.DropLocation)) {
					return false; // NOPMD
				}
				final TreePath path = ((JTree.DropLocation) dloc).getPath();
				if (path.getLastPathComponent() instanceof Unit || path.getLastPathComponent() instanceof UnitNode) {
					try {
						final UnitMemberTransferable.UnitMemberPair pair = (UnitMemberPair) trans
								.getTransferData(UnitMemberTransferable.FLAVOR);
						// ESCA-JAVA0177:
						Unit target;
						if (path.getLastPathComponent() instanceof Unit) {
							target = (Unit) path.getLastPathComponent();
						} else if (path.getLastPathComponent() instanceof UnitNode) {
							target = (Unit) ((UnitNode) path.getLastPathComponent()).getUserObject();
						} else {
							LOGGER.severe("Impossible instanceof failure");
							return false; // NOPMD
						}
						model.moveMember(pair.member, pair.unit, target);
						return true; // NOPMD
					} catch (UnsupportedFlavorException except) {
						LOGGER.log(Level.SEVERE, "Impossible unsupported data flavor", except);
						return false; // NOPMD
					} catch (IOException except) {
						LOGGER.log(Level.SEVERE, "I/O error in transfer after we checked ... shouldn't happen", except);
						return false; // NOPMD
					}
				} else {
					return false; // NOPMD
				}
			} else {
				return false;
			}
		}
	}
}
