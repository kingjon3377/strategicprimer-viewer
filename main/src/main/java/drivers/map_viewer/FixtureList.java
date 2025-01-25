package drivers.map_viewer;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import java.util.Optional;
import java.util.List;

import legacy.map.Player;
import legacy.map.TileFixture;
import legacy.map.Point;

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

import legacy.idreg.IDRegistrar;

import legacy.map.fixtures.mobile.IUnit;

import worker.common.IFixtureEditHelper;

/**
 * A visual list-based representation of the contents of a tile.
 */
public final class FixtureList extends JList<TileFixture>
		implements DragGestureListener, SelectionChangeListener {
	@Serial
	private static final long serialVersionUID = 1L;
	private final FixtureListModel listModel;

	public FixtureList(final JComponent parentComponent, final FixtureListModel listModel,
					   final IFixtureEditHelper feh, final IDRegistrar idf, final Iterable<Player> players) {
		super(listModel);
		this.listModel = listModel;
		setCellRenderer(new FixtureCellRenderer());
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		addMouseListener(new FixtureMouseListener(listModel, this::locationToIndex, players, feh, idf));
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_COPY, this::dragGestureRecognized);

		setDropTarget(new DropTarget(this, new DropListener(parentComponent, listModel)));

		createHotKey(this, "delete",
				event -> listModel.removeAll(getSelectedValuesList()),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
				KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
	}

	@Override
	public void selectedUnitChanged(final @Nullable IUnit old, final @Nullable IUnit newSel) {
	}

	@Override
	public void cursorPointChanged(final @Nullable Point old, final Point newCursor) {
	}

	@Override
	public void dragGestureRecognized(final DragGestureEvent event) {
		final List<TileFixture> selection = getSelectedValuesList();
		if (!selection.isEmpty()) {
			final Transferable payload;
			if (selection.size() == 1) {
				payload = new FixtureTransferable(selection.getFirst());
			} else {
				payload = new CurriedFixtureTransferable(
						selection.toArray(TileFixture[]::new));
			}
			event.startDrag(null, payload);
		}
	}

	@Override
	public boolean equals(final Object that) {
		if (that instanceof final JList<?> l) {
			return getModel().equals(l.getModel());
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

	private static final class FixtureMouseListener extends MouseAdapter {
		private final FixtureListModel listModel;
		private final ToIntFunction<? super java.awt.Point> locationToIndex;
		private final Iterable<Player> players;
		private final IFixtureEditHelper feh;
		private final IDRegistrar idf;
		FixtureMouseListener(final FixtureListModel listModel,
		                     final ToIntFunction<? super java.awt.Point> locationToIndex,
		                     final Iterable<Player> players, final IFixtureEditHelper feh, final IDRegistrar idf) {
			this.listModel = listModel;
			this.locationToIndex = locationToIndex;
			this.players = players;
			this.feh = feh;
			this.idf = idf;
		}
		private void handleMouseEvent(final MouseEvent event) {
			if (event.isPopupTrigger() && event.getClickCount() == 1) {
				final int index = locationToIndex.applyAsInt(event.getPoint());
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

	private static final class DropListener extends DropTargetAdapter {
		private final JComponent parentComponent;
		private final FixtureListModel listModel;
		DropListener(final JComponent parentComponent, final FixtureListModel listModel) {
			this.parentComponent = parentComponent;
			this.listModel = listModel;
		}
		// TODO: Figure out how to skip all this (return true) on non-local drags
		private boolean isXfrFromOutside(final DropTargetEvent dtde) {
			return !(dtde.getSource() instanceof final Component c) || !parentComponent.isAncestorOf(c);
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
					if (transferData instanceof final TileFixture tf) {
						listModel.addFixture(tf);
					} // TODO: else what? log?
				} else if (CurriedFixtureTransferable.FLAVOR.equals(flavor)) {
					// Suppression is reasonable: this can only break in the face of
					// an actively hostile caller, as this flavor supposedly indicates
					// this is the type.
					@SuppressWarnings("unchecked") final Iterable<Transferable> curried =
							(Iterable<Transferable>) trans.getTransferData(flavor);
					for (final Transferable t : curried) {
						handleDrop(t);
					}
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
						.anyMatch(Predicate.isEqual(flavor))) {
					try {
						dtde.acceptDrop(dtde.getDropAction());
						final Transferable t = dtde.getTransferable();
						if (Objects.nonNull(t)) {
							handleDrop(t);
						}
						return;
					} catch (final UnsupportedFlavorException except) {
						LovelaceLogger.error(except, "Unsupported flavor when it said it was supported");
					} catch (final IOException except) {
						LovelaceLogger.error(except, "I/O error getting the data");
					}
				}
			}
			dtde.rejectDrop();
		}
	}
}
