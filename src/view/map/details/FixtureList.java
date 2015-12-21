package view.map.details;

import model.listeners.SelectionChangeListener;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.misc.IDriverModel;
import model.viewer.CurriedFixtureTransferable;
import model.viewer.FixtureListDropListener;
import model.viewer.FixtureListModel;
import model.viewer.FixtureTransferable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;

import javax.swing.*;
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

/**
 * A visual list-based representation of the contents of a tile.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class FixtureList extends JList<@NonNull TileFixture> implements
		DragGestureListener, SelectionChangeListener {
	/**
	 * The list model.
	 */
	private final FixtureListModel flm;

	/**
	 * Constructor.
	 *
	 * @param parent  a parent of this list
	 * @param dmodel  the driver model (needed to get at the map for the list model)
	 * @param players the players in the map
	 */
	public FixtureList(final JComponent parent, final IDriverModel dmodel,
	                   final Iterable<Player> players) {
		flm = new FixtureListModel(dmodel);
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
				((FixtureListModel) getModel()).removeAll(getSelectedValuesList());
			}
		});
		addMouseListener(new FixtureMouseListener(players, this));
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
			final TileFixture firstElement = NullCleaner
					                                 .assertNotNull(selection.get(0));
			if (selection.size() == 1) {
				dge.startDrag(null, new FixtureTransferable(firstElement));
			} else {
				dge.startDrag(null, new CurriedFixtureTransferable(selection));
			}
		}
	}

	/**
	 * A FixtureList is equal to only another JList with the same model. If obj is a
	 * DropTarget, we compare to its Component.
	 *
	 * @param obj another object
	 * @return whether it's equal to this one
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof JList)
				                         && getModel().equals(((JList) obj).getModel()));
	}

	/**
	 * @return a hash-code for the object
	 */
	@Override
	public int hashCode() {
		return flm.hashCode();
	}

	/**
	 * A listener to set up pop-up menus.
	 *
	 * @author Jonathan Lovelace
	 */
	private static final class FixtureMouseListener extends MouseAdapter {
		/**
		 * The collection of players in the map.
		 */
		private final Iterable<Player> players;
		/**
		 * The list we're listening on.
		 */
		private final JList<TileFixture> list;

		/**
		 * Constructor.
		 *
		 * @param playerColl the collection of players in the map
		 */
		protected FixtureMouseListener(final Iterable<Player> playerColl, final
		JList<TileFixture> theList) {
			players = playerColl;
			list = theList;
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
		 * @param event the event to handle. Marked as @Nullable so we only have to
		 *                    handle
		 *              the null-event case once.
		 */
		private void handleMouseEvent(@Nullable final MouseEvent event) {
			if ((event != null) && event.isPopupTrigger()
					    && (event.getClickCount() == 1)) {
				final int index = list.locationToIndex(event.getPoint());
				if ((index >= 0) && (index < list.getModel().getSize())) {
					new FixtureEditMenu(list.getModel().getElementAt(
							list.locationToIndex(event.getPoint())), players).show(
							event.getComponent(), event.getX(),
							event.getY());
				}
			}
		}

		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "FixtureMouseListener";
		}
	}

	/**
	 * @param old      passed to the list model
	 * @param newPoint passed to the list model
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point old,
	                                 final Point newPoint) {
		flm.selectedPointChanged(old, newPoint);
	}
}
