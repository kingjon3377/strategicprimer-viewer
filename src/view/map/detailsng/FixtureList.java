package view.map.detailsng;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JList;
import javax.swing.ListSelectionModel;

import model.map.TileFixture;
import model.viewer.CurriedFixtureTransferable;
import model.viewer.FixtureListModel;
import model.viewer.FixtureTransferable;
import util.EqualsAny;
import util.PropertyChangeSource;

/**
 * A visual list-based representation of the contents of a tile.
 *
 * @author Jonathan Lovelace
 */
public class FixtureList extends JList<TileFixture> implements DragGestureListener, DropTargetListener {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(FixtureList.class.getName());
	/**
	 * Constructor.
	 *
	 * @param property the property the model will be listening for
	 * @param sources objects the model should listen to
	 */
	public FixtureList(final String property,
			final PropertyChangeSource... sources) {
		super(new FixtureListModel(property, sources));
		setCellRenderer(new FixtureCellRenderer());
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
				this, DnDConstants.ACTION_COPY, this);
		setDropTarget(new DropTarget(this, this));

	}
	/**
	 * Start a drag when appropriate.
	 * @param dge the event to handle
	 */
	@Override
	public void dragGestureRecognized(final DragGestureEvent dge) {
		final List<TileFixture> selection = getSelectedValuesList();
		final Transferable trans = selection.size() == 1 ? new FixtureTransferable(
				selection.get(0)) : new CurriedFixtureTransferable(selection);
		dge.startDrag(null, trans);
	}
	/**
	 * A possible drag entering the component?
	 *
	 * @param dtde the event to handle
	 */
	@Override
	public void dragEnter(final DropTargetDragEvent dtde) {
		if ((dtde.getDropAction() & DnDConstants.ACTION_COPY) != 0
				&& dtde.getSource() != this
				&& (EqualsAny.equalsAny(FixtureTransferable.FLAVOR,
						dtde.getCurrentDataFlavorsAsList()) || EqualsAny
						.equalsAny(CurriedFixtureTransferable.FLAVOR,
								dtde.getCurrentDataFlavorsAsList()))) {
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
				&& dtde.getSource() != this
				&& (EqualsAny.equalsAny(FixtureTransferable.FLAVOR,
						dtde.getCurrentDataFlavorsAsList()) || EqualsAny
						.equalsAny(CurriedFixtureTransferable.FLAVOR,
								dtde.getCurrentDataFlavorsAsList()))) {
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
				&& dtde.getSource() != this
				&& (EqualsAny.equalsAny(FixtureTransferable.FLAVOR,
						dtde.getCurrentDataFlavorsAsList()) || EqualsAny
						.equalsAny(CurriedFixtureTransferable.FLAVOR,
								dtde.getCurrentDataFlavorsAsList()))) {
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
		if (dtde.getSource() != this) {
			for (final DataFlavor flavor : dtde.getCurrentDataFlavorsAsList()) {
				if (EqualsAny.equalsAny(flavor, FixtureTransferable.FLAVOR, CurriedFixtureTransferable.FLAVOR)) {
					try {
						handleDrop(dtde.getTransferable());
					} catch (UnsupportedFlavorException except) {
						LOGGER.log(Level.SEVERE,
								"Unsupported flavor when it said it was supported",
								except);
						continue;
					} catch (IOException except) {
						LOGGER.log(Level.SEVERE, "I/O error getting the data", except);
						continue;
					}
					dtde.acceptDrop(dtde.getDropAction());
					return; // NOPMD
				}
			}
		}
		dtde.rejectDrop();
	}
	/**
	 * Handle a drop.
	 * @param trans the transferable containing the dragged data.
	 * @throws IOException on I/O error getting the data
	 * @throws UnsupportedFlavorException if the data flavor isn't actually supported
	 */
	private void handleDrop(final Transferable trans) throws UnsupportedFlavorException, IOException {
		if (EqualsAny.equalsAny(FixtureTransferable.FLAVOR, trans.getTransferDataFlavors())) {
			((FixtureListModel) getModel()).addFixture((TileFixture) trans
					.getTransferData(FixtureTransferable.FLAVOR));
		} else if (EqualsAny.equalsAny(CurriedFixtureTransferable.FLAVOR, trans.getTransferDataFlavors())) {
			for (Transferable item : (List<Transferable>) trans.getTransferData(CurriedFixtureTransferable.FLAVOR)) {
				handleDrop(item);
			}
		} else {
			throw new UnsupportedFlavorException(trans.getTransferDataFlavors()[0]);
		}
	}
}
