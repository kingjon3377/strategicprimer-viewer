package model.viewer;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;

import model.map.TileFixture;

import org.eclipse.jdt.annotation.Nullable;

import util.EqualsAny;
import util.TypesafeLogger;

/**
 * The details of inter-FixtureList drag-and-drop, extracted to reduce the
 * number of methods in the class.
 *
 * @author Jonathan Lovelace
 *
 */
public class FixtureListDropListener implements DropTargetListener {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(FixtureListDropListener.class);
	/**
	 * The List's model.
	 */
	private final FixtureListModel model;

	/**
	 * Constructor.
	 *
	 * @param listModel the List's model
	 * @param parent a parent of the list.
	 */
	public FixtureListDropListener(final JComponent parent,
			final FixtureListModel listModel) {
		model = listModel;
		parentComponent = parent;
	}

	/**
	 * A parent component. If it's an ancestor of the drop, it's an
	 * intra-component drop.
	 */
	private final JComponent parentComponent;

	/**
	 * A possible drag entering the component?
	 *
	 * @param dtde the event to handle
	 */
	@Override
	public void dragEnter(@Nullable final DropTargetDragEvent dtde) {
		if (dtde != null) {
			if ((dtde.getDropAction() & DnDConstants.ACTION_COPY) != 0
					&& (EqualsAny.equalsAny(FixtureTransferable.FLAVOR,
							dtde.getCurrentDataFlavorsAsList()) || EqualsAny
							.equalsAny(CurriedFixtureTransferable.FLAVOR,
									dtde.getCurrentDataFlavorsAsList()))
					&& !isIntraComponentXfr(dtde)) {
				dtde.acceptDrag(dtde.getDropAction());
			} else {
				dtde.rejectDrag();
			}
		}
	}

	/**
	 * TODO: Figure out how to skip all this (return false) on non-local drags.
	 *
	 * @param dtde an event
	 * @return whether the data it holds come from are (probably) from this
	 *         component. I/O etc. problems return true.
	 */
	private boolean isIntraComponentXfr(final DropTargetEvent dtde) {
		return dtde.getSource() instanceof Component
				&& parentComponent.isAncestorOf((Component) dtde.getSource());
	}

	/**
	 * Continued dragging over the component.
	 *
	 * @param dtde the event to handle
	 */
	@Override
	public void dragOver(@Nullable final DropTargetDragEvent dtde) {
		if (dtde != null) {
			if ((dtde.getDropAction() & DnDConstants.ACTION_COPY) != 0
					&& (EqualsAny.equalsAny(FixtureTransferable.FLAVOR,
							dtde.getCurrentDataFlavorsAsList()) || EqualsAny
							.equalsAny(CurriedFixtureTransferable.FLAVOR,
									dtde.getCurrentDataFlavorsAsList()))
					&& !isIntraComponentXfr(dtde)) {
				dtde.acceptDrag(dtde.getDropAction());
			} else {
				dtde.rejectDrag();
			}
		}
	}

	/**
	 * Handle change to the type of drag---which we don't care about.
	 *
	 * @param dtde the event to handle.
	 */
	@Override
	public void dropActionChanged(@Nullable final DropTargetDragEvent dtde) {
		if (dtde != null) {
			if ((dtde.getDropAction() & DnDConstants.ACTION_COPY) != 0
					&& (EqualsAny.equalsAny(FixtureTransferable.FLAVOR,
							dtde.getCurrentDataFlavorsAsList()) || EqualsAny
							.equalsAny(CurriedFixtureTransferable.FLAVOR,
									dtde.getCurrentDataFlavorsAsList()))
					&& !isIntraComponentXfr(dtde)) {
				dtde.acceptDrag(dtde.getDropAction());
			} else {
				dtde.rejectDrag();
			}
		}
	}

	/**
	 * The drag is exiting the component.
	 *
	 * @param dte ignored
	 */
	@Override
	public void dragExit(@Nullable final DropTargetEvent dte) {
		// ignored
	}

	/**
	 * Handle drop.
	 *
	 * @param dtde the event to handle.
	 */
	@Override
	public void drop(@Nullable final DropTargetDropEvent dtde) {
		if (dtde == null) {
			return; // NOPMD
		} else if (isIntraComponentXfr(dtde)) {
			dtde.rejectDrop();
			return; // NOPMD
		} else {
			for (final DataFlavor flavor : dtde.getCurrentDataFlavorsAsList()) {
				if (flavor != null
						&& EqualsAny.equalsAny(flavor,
								FixtureTransferable.FLAVOR,
								CurriedFixtureTransferable.FLAVOR)) {
					try {
						dtde.acceptDrop(dtde.getDropAction());
						final Transferable trans = dtde.getTransferable();
						if (trans != null) {
							handleDrop(trans);
						}
					} catch (final UnsupportedFlavorException except) {
						LOGGER.log(
								Level.SEVERE,
								"Unsupported flavor when it said it was supported",
								except);
						continue;
					} catch (final IOException except) {
						LOGGER.log(Level.SEVERE, "I/O error getting the data",
								except);
						continue;
					}
					return; // NOPMD
				}
			}
			dtde.rejectDrop();
		}
	}

	/**
	 * Handle a drop.
	 *
	 * @param trans the transferable containing the dragged data.
	 * @throws IOException on I/O error getting the data
	 * @throws UnsupportedFlavorException if the data flavor isn't actually
	 *         supported
	 */
	private void handleDrop(final Transferable trans)
			throws UnsupportedFlavorException, IOException {
		final DataFlavor[] dflav = trans.getTransferDataFlavors();
		if (dflav == null) {
			throw new UnsupportedFlavorException(new DataFlavor(
					DataFlavor.class, "null"));
		}
		if (EqualsAny.equalsAny(FixtureTransferable.FLAVOR, dflav)) {
			final TileFixture transferData = (TileFixture) trans
					.getTransferData(FixtureTransferable.FLAVOR);
			if (transferData != null) {
				model.addFixture(transferData);
			}
		} else if (EqualsAny
				.equalsAny(CurriedFixtureTransferable.FLAVOR, dflav)) {
			for (final Transferable item : (List<Transferable>) trans
					.getTransferData(CurriedFixtureTransferable.FLAVOR)) {
				if (item != null) {
					handleDrop(item);
				}
			}
		} else {
			throw new UnsupportedFlavorException(
					trans.getTransferDataFlavors()[0]);
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "FixtureListDropListener";
	}
}
