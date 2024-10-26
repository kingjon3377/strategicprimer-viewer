package drivers.map_viewer;

import java.awt.Component;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.Transferable;

import java.io.IOException;
import java.io.Serial;

import javax.swing.JTable;
import javax.swing.JList;
import javax.swing.TransferHandler;
import javax.swing.JComponent;

import lovelace.util.LovelaceLogger;
import lovelace.util.Reorderable;
import lovelace.util.IntTransferable;
import drivers.common.FixtureMatcher;

/**
 * A transfer-handler to let the user drag items in the list to control Z-order.
 */
/* package */ final class FixtureFilterTransferHandler extends TransferHandler {
	private static final DataFlavor FLAVOR = new DataFlavor(FixtureMatcher.class, "FixtureMatcher");
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * A drag/drop operation is supported if and only if both it is a
	 * supported flavor and it is or can be coerced to be a move operation.
	 */
	@Override
	public boolean canImport(final TransferSupport support) {
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
	public Transferable createTransferable(final JComponent component) {
		return switch (component) {
			case final JList<?> l -> new IntTransferable(FLAVOR, l.getSelectedIndex());
			case final JTable t -> new IntTransferable(FLAVOR, t.getSelectedRow());
			default -> throw new IllegalArgumentException("component must be a JList or a JTable");
		};
	}

	/**
	 * This listener only allows move operations.
	 */
	@Override
	public int getSourceActions(final JComponent component) {
		return TransferHandler.MOVE;
	}

	/**
	 * Handle a drop.
	 */
	@Override
	public boolean importData(final TransferSupport support) {
		if (!support.isDrop()) {
			return false;
		}
		final Component component = support.getComponent();
		final DropLocation dropLocation = support.getDropLocation();
		final Transferable transfer = support.getTransferable();
		final int payload;
		try {
			payload = (Integer) transfer.getTransferData(FLAVOR);
		} catch (final UnsupportedFlavorException | IOException except) {
			LovelaceLogger.debug(except, "Transfer failure");
			return false;
		}
		switch (component) {
			case final JList<?> l when l.getModel() instanceof final Reorderable model &&
					dropLocation instanceof final JList.DropLocation dl -> {
				final int index = dl.getIndex();
				model.reorder(payload, index);
				return true;
			}
			case final JTable t when t.getModel() instanceof final Reorderable model &&
					dropLocation instanceof final JTable.DropLocation dl -> {
				final int index = dl.getRow();
				final int selection = t.getSelectedRow();
				model.reorder(payload, index);
				if (selection == payload) {
					t.setRowSelectionInterval(index, index);
				} else if (selection > index && selection < payload) {
					t.setRowSelectionInterval(selection - 1, selection - 1);
				}
				return true;
			}
			case null, default -> {
				return false;
			}
		}
	}
}
