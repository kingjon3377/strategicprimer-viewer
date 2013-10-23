package view.map.details;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import model.listeners.SelectionChangeListener;
import model.map.PlayerCollection;
import model.map.Point;
import model.map.Tile;
import model.map.TileFixture;
import model.viewer.CurriedFixtureTransferable;
import model.viewer.FixtureListDropListener;
import model.viewer.FixtureListModel;
import model.viewer.FixtureTransferable;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A visual list-based representation of the contents of a tile.
 *
 * @author Jonathan Lovelace
 */
public class FixtureList extends JList<TileFixture> implements
		DragGestureListener, SelectionChangeListener {
	/**
	 * The list model.
	 */
	private final FixtureListModel flm;

	/**
	 * Constructor.
	 *
	 * @param parent a parent of this list
	 * @param players the players in the map
	 */
	public FixtureList(final JComponent parent, final PlayerCollection players) {
		super();
		flm = new FixtureListModel();
		setModel(flm);
		setCellRenderer(new FixtureCellRenderer());
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
				this, DnDConstants.ACTION_COPY, this);
		setDropTarget(new DropTarget(this, new FixtureListDropListener(parent,
				flm)));
		final InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
				"delete");
		getActionMap().put("delete", new AbstractAction() {
			@Override
			public void actionPerformed(@Nullable final ActionEvent event) {
				((FixtureListModel) getModel()).remove(getSelectedValuesList());
			}
		});
		addMouseListener(new FixtureMouseListener(players));
	}

	/**
	 * Start a drag when appropriate.
	 *
	 * @param dge the event to handle
	 */
	@Override
	public void dragGestureRecognized(@Nullable final DragGestureEvent dge) {
		if (dge != null) {
			final List<TileFixture> selection = getSelectedValuesList();
			if (selection.isEmpty()) {
				return;
			}
			final TileFixture firstElement = selection.get(0);
			assert firstElement != null;
			final Transferable trans = selection.size() == 1 ? new FixtureTransferable(
					firstElement) : new CurriedFixtureTransferable(
					selection);
			dge.startDrag(null, trans);
		}
	}

	/**
	 * A FixtureList is equal to only another JList with the same model. If obj
	 * is a DropTarget, we compare to its Component.
	 *
	 * @param obj another object
	 * @return whether it's equal to this one
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj
				|| (obj instanceof JList && getModel().equals(
						((JList) obj).getModel()));
	}

	/**
	 * @return a hash-code for the object
	 */
	@Override
	public int hashCode() {
		return getModel().hashCode();
	}

	/**
	 * A listener to set up pop-up menus.
	 */
	private class FixtureMouseListener extends MouseAdapter {
		/**
		 * The collection of players in the map.
		 */
		private final PlayerCollection players;

		/**
		 * Constructor.
		 *
		 * @param playerColl the collection of players in the map
		 */
		FixtureMouseListener(final PlayerCollection playerColl) {
			players = playerColl;
		}

		/**
		 * @param event the event to handle
		 */
		@Override
		public void mouseClicked(@Nullable final MouseEvent event) {
			handleMouseEvent(event);
		}

		/**
		 * @param event the event to handle
		 */
		@Override
		public void mousePressed(@Nullable final MouseEvent event) {
			handleMouseEvent(event);
		}

		/**
		 * @param event the event to handle
		 */
		@Override
		public void mouseReleased(@Nullable final MouseEvent event) {
			handleMouseEvent(event);
		}

		/**
		 * @param event the event to handle. Marked as @Nullable so we only have
		 *        to handle the null-event case once.
		 */
		private void handleMouseEvent(@Nullable final MouseEvent event) {
			if (event != null && event.isPopupTrigger()
					&& event.getClickCount() == 1) {
				final TileFixture selectedElement = getModel().getElementAt(
								locationToIndex(event.getPoint()));
				if (selectedElement != null) {
					new FixtureEditMenu(selectedElement, players).show(
							event.getComponent(), event.getX(), event.getY());
				}
			}
		}
	}
	/**
	 * @param old passed to the list model
	 * @param newPoint passed to the list model
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point old, final Point newPoint) {
		flm.selectedPointChanged(old, newPoint);
	}
	/**
	 * @param old passed to the list model
	 * @param newTile passed to the list model
	 */
	@Override
	public void selectedTileChanged(@Nullable final Tile old, final Tile newTile) {
		flm.selectedTileChanged(old, newTile);
	}
}
