package drivers.map_viewer;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Component;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.Transferable;

import java.io.IOException;

import javax.swing.JTable;
import javax.swing.JList;
import javax.swing.TransferHandler;
import javax.swing.JComponent;

import lovelace.util.Reorderable;
import lovelace.util.IntTransferable;
import drivers.common.FixtureMatcher;

/**
 * A transfer-handler to let the user drag items in the list to control Z-order.
 */
/* package */ class FixtureFilterTransferHandler extends TransferHandler {
	private static final DataFlavor FLAVOR = new DataFlavor(FixtureMatcher.class, "FixtureMatcher");
	private static final Logger LOGGER =
		Logger.getLogger(FixtureFilterTransferHandler.class.getName());
	private static final long serialVersionUID = 1L;

	/**
	 * A drag/drop operation is supported iff it is a supported flavor and
	 * it is or can be coerced to be a move operation.
	 */
	@Override
	public boolean canImport(TransferSupport support) {
		if (support.isDrop() && support.isDataFlavorSupported(FLAVOR) &&
				((TransferHandler.MOVE & support.getSourceDropActions()) ==
					TransferHandler.MOVE)) {
			support.setDropAction(TransferHandler.MOVE);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Create a wrapper to transfer contents of the given component, which must be a {@link JList}
	 * or a {@link JTable}.
	 */
	@Override
	public Transferable createTransferable(JComponent component) {
		if (component instanceof JList) {
			return new IntTransferable(FLAVOR, ((JList<?>) component).getSelectedIndex());
		} else if (component instanceof JTable) {
			return new IntTransferable(FLAVOR, ((JTable) component).getSelectedRow());
		} else {
			throw new IllegalArgumentException("component must be a JList or a JTable");
		}
	}

	/**
	 * This listener only allows move operations.
	 */
	@Override
	public int getSourceActions(JComponent component) {
		return TransferHandler.MOVE;
	}

	/**
	 * Handle a drop.
	 */
	@Override
	public boolean importData(TransferSupport support) {
		if (!support.isDrop()) {
			return false;
		}
		Component component = support.getComponent();
		DropLocation dropLocation = support.getDropLocation();
		Transferable transfer = support.getTransferable();
		int payload;
		try {
			payload = (Integer) transfer.getTransferData(FLAVOR);
		} catch (UnsupportedFlavorException|IOException except) {
			LOGGER.log(Level.FINE, "Transfer failure", except);
			return false;
		}
		if (component instanceof JList && ((JList<?>) component).getModel() instanceof Reorderable
				&& dropLocation instanceof JList.DropLocation) {
			int index = ((JList.DropLocation) dropLocation).getIndex();
			((Reorderable) ((JList<?>) component).getModel()).reorder(payload, index);
			return true;
		} else if (component instanceof JTable &&
				((JTable) component).getModel() instanceof Reorderable &&
				dropLocation instanceof JTable.DropLocation) {
			int index = ((JTable.DropLocation) dropLocation).getRow();
			int selection = ((JTable) component).getSelectedRow();
			((Reorderable) ((JTable) component).getModel()).reorder(payload, index);
			if (selection == payload) {
				((JTable) component).setRowSelectionInterval(index, index);
			} else if (selection > index && selection < payload) {
				((JTable) component).setRowSelectionInterval(selection - 1, selection - 1);
			}
			return true;
		} else {
			return false;
		}
	}
}
