package view.map.details;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.map.TileFixture;
import model.viewer.FixtureTransferable;
import util.EqualsAny;

/**
 * A class to listen for potential Chit drops.
 *
 * @author Jonathan Lovelace
 *
 */
public class ChitDropListener implements DropTargetListener {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(ChitDropListener.class.getName());

	/**
	 * A possible drag entering the component?
	 *
	 * @param dtde the event to handle
	 */
	@Override
	public void dragEnter(final DropTargetDragEvent dtde) {
		if ((dtde.getDropAction() & DnDConstants.ACTION_COPY) != 0
				&& EqualsAny.equalsAny(FixtureTransferable.FLAVOR,
						dtde.getCurrentDataFlavorsAsList())) {
			dtde.acceptDrag(dtde.getDropAction());
		} else {
			dtde.rejectDrag();
		}
	}

	/**
	 * Continued dragging over the component.
	 *
	 * @param dtde the event to handle
	 */
	@Override
	public void dragOver(final DropTargetDragEvent dtde) {
		if ((dtde.getDropAction() & DnDConstants.ACTION_COPY) != 0
				&& EqualsAny.equalsAny(FixtureTransferable.FLAVOR,
						dtde.getCurrentDataFlavorsAsList())) {
			dtde.acceptDrag(dtde.getDropAction());
		} else {
			dtde.rejectDrag();
		}
	}

	/**
	 * Handle change to the type of drag---which we don't care about.
	 *
	 * @param dtde the event to handle.
	 */
	@Override
	public void dropActionChanged(final DropTargetDragEvent dtde) {
		if ((dtde.getDropAction() & DnDConstants.ACTION_COPY) != 0
				&& EqualsAny.equalsAny(FixtureTransferable.FLAVOR,
						dtde.getCurrentDataFlavorsAsList())) {
			dtde.acceptDrag(dtde.getDropAction());
		} else {
			dtde.rejectDrag();
		}
	}

	/**
	 * The drag is exiting the component.
	 *
	 * @param dte ignored
	 */
	@Override
	public void dragExit(final DropTargetEvent dte) {
		// ignored
	}

	/**
	 * Handle drop.
	 *
	 * @param dtde the event to handle.
	 */
	@Override
	public void drop(final DropTargetDropEvent dtde) {
		for (final DataFlavor flavor : dtde.getCurrentDataFlavorsAsList()) {
			if (FixtureTransferable.FLAVOR.equals(flavor)) {
				try {
					((ChitAndDetailPanel) ((DropTarget) dtde.getSource())
							.getComponent()).addFixture((TileFixture) dtde
							.getTransferable().getTransferData(flavor));
				} catch (final UnsupportedFlavorException e) {
					LOGGER.log(Level.SEVERE,
							"Unsupported flavor when it said it was supported",
							e);
					continue;
				} catch (final IOException e) {
					LOGGER.log(Level.SEVERE, "I/O error getting the data", e);
					continue;
				}
				dtde.acceptDrop(dtde.getDropAction());
				return;
			}
		}
		dtde.rejectDrop();
	}

	/**
	 *
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "ChitDropListener";
	}
}
