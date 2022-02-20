package drivers.map_viewer;

import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Optional;
import java.util.List;
import common.map.Player;
import common.map.TileFixture;
import common.map.Point;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.Transferable;

import java.awt.event.MouseAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import static lovelace.util.MenuUtils.createHotKey;

import java.awt.Component;

import java.io.IOException;

import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DropTargetDragEvent;

import javax.swing.KeyStroke;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import drivers.common.SelectionChangeListener;

import common.idreg.IDRegistrar;

import common.map.fixtures.mobile.IUnit;

import worker.common.IFixtureEditHelper;

/**
 * A visual list-based representation of the contents of a tile.
 */
public final class FixtureList extends JList<TileFixture>
		implements DragGestureListener, SelectionChangeListener {
	private static final Logger LOGGER = Logger.getLogger(FixtureList.class.getName());
	private final JComponent parentComponent;
	private final FixtureListModel listModel;
	private final IFixtureEditHelper feh;
	private final IDRegistrar idf;
	private final Iterable<Player> players;
	public FixtureList(final JComponent parentComponent, final FixtureListModel listModel,
	                   final IFixtureEditHelper feh, final IDRegistrar idf, final Iterable<Player> players) {
		this.parentComponent = parentComponent;
		this.listModel = listModel;
		this.feh = feh;
		this.idf = idf;
		this.players = players;
		setCellRenderer(new FixtureCellRenderer());
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		addMouseListener(new FixtureMouseListener());
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this,
			DnDConstants.ACTION_COPY, this);

		setDropTarget(new DropTarget(this, new DropListener()));

		createHotKey(this, "delete",
			event -> listModel.removeAll(this.getSelectedValuesList()),
			JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
			KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
			KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
	}

	@Override
	public void selectedUnitChanged(final @Nullable IUnit old, final @Nullable IUnit newSel) {}

	@Override
	public void cursorPointChanged(final @Nullable Point old, final Point newCursor) {}

	@Override
	public void dragGestureRecognized(final DragGestureEvent event) {
		final List<TileFixture> selection = getSelectedValuesList();
		if (!selection.isEmpty()) {
			final Transferable payload;
			if (selection.size() == 1) {
				payload = new FixtureTransferable(selection.get(0));
			} else {
				payload = new CurriedFixtureTransferable(
						selection.toArray(new TileFixture[0]));
			}
			event.startDrag(null, payload);
		}
	}

	@Override
	public boolean equals(final Object that) {
		if (that instanceof JList) {
			return getModel().equals(((JList<?>) that).getModel());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return listModel.hashCode();
	}

	@Override
	public void selectedPointChanged(final @Nullable Point old, final Point newPoint) {
		SwingUtilities.invokeLater(() -> listModel.selectedPointChanged(old, newPoint));
	}

	@Override
	public void interactionPointChanged() {
		SwingUtilities.invokeLater(listModel::interactionPointChanged);
	}

	// TODO: Try to make static, taking necessary dependencies as parameters
	private class FixtureMouseListener extends MouseAdapter {
		private void handleMouseEvent(final MouseEvent event) {
			if (event.isPopupTrigger() && event.getClickCount() == 1) {
				final int index = locationToIndex(event.getPoint());
				if (index >= 0 && index < listModel.getSize()) {
					new FixtureEditMenu(listModel.getElementAt(index), players,
							idf, feh)
						.show(event.getComponent(), event.getX(), event.getY());
				}
			}
		}

		@Override
		public void mouseClicked(final MouseEvent event) {
			handleMouseEvent(event);
		}

		@Override
		public void mousePressed(final MouseEvent event) {
			handleMouseEvent(event);
		}

		@Override
		public void mouseReleased(final MouseEvent event) {
			handleMouseEvent(event);
		}
	}

	// TODO: Try to make static, taking necessary dependencies as parameters
	private class DropListener extends DropTargetAdapter {
		// TODO: Figure out how to skip all this (return true) on non-local drags
		private boolean isXfrFromOutside(final DropTargetEvent dtde) {
			return !(dtde.getSource() instanceof Component) ||
					       !parentComponent.isAncestorOf((Component) dtde.getSource());
		}

		private void handleDrag(final DropTargetDragEvent dtde) {
			if ((dtde.getDropAction() & DnDConstants.ACTION_COPY) != 0 &&
					(dtde.getCurrentDataFlavorsAsList()
							.contains(FixtureTransferable.FLAVOR) ||
						dtde.getCurrentDataFlavorsAsList().contains(
							CurriedFixtureTransferable.FLAVOR)) &&
					isXfrFromOutside(dtde)) {
				dtde.acceptDrag(dtde.getDropAction());
			} else {
				dtde.rejectDrag();
			}
		}

		@Override
		public void dragEnter(final DropTargetDragEvent dtde) {
			handleDrag(dtde);
		}

		@Override
		public void dragOver(final DropTargetDragEvent dtde) {
			handleDrag(dtde);
		}

		@Override
		public void dropActionChanged(final DropTargetDragEvent dtde) {
			handleDrag(dtde);
		}

		private void handleDrop(final Transferable trans) throws UnsupportedFlavorException, IOException {
			final DataFlavor[] flavors = Optional.ofNullable(trans.getTransferDataFlavors())
					.orElseGet(() -> new DataFlavor[0]);
			for (final DataFlavor flavor : flavors) {
				if (FixtureTransferable.FLAVOR.equals(flavor)) {
					final Object transferData = trans.getTransferData(flavor);
					if (transferData instanceof TileFixture) {
						listModel.addFixture((TileFixture) transferData);
					} // TODO: else what? log?
				} else if (CurriedFixtureTransferable.FLAVOR.equals(flavor)) {
					final List<Transferable> curried =
						(List<Transferable>) trans.getTransferData(flavor);
					for (final Transferable t : curried) {
						handleDrop(t);
					}
				} else {
					// FIXME: Just skip!
					throw new UnsupportedFlavorException(flavor);
				}
			}
			if (flavors.length == 0) {
				throw new UnsupportedFlavorException(
					new DataFlavor(DataFlavor.class, "null"));
			} else {
				throw new UnsupportedFlavorException(flavors[0]);
			}
		}

		@Override
		public void drop(final DropTargetDropEvent dtde) {
			if (!isXfrFromOutside(dtde)) {
				return;
			}
			for (final DataFlavor flavor : dtde.getCurrentDataFlavorsAsList()) {
				if (Stream.of(FixtureTransferable.FLAVOR,
							CurriedFixtureTransferable.FLAVOR)
						.anyMatch(flavor::equals)) {
					try {
						dtde.acceptDrop(dtde.getDropAction());
						final Transferable t = dtde.getTransferable();
						if (t != null) {
							handleDrop(t);
						}
						return;
					} catch (final UnsupportedFlavorException except) {
						LOGGER.log(Level.SEVERE,
							"Unsupported flavor when it said it was supported",
							except);
					} catch (final IOException except) {
						LOGGER.log(Level.SEVERE,
							"I/O error getting the data", except);
					}
				}
			}
			dtde.rejectDrop();
		}
	}
}
