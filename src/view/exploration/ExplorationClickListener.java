package view.exploration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

import org.eclipse.jdt.annotation.Nullable;

import model.exploration.IExplorationModel;
import model.exploration.IExplorationModel.Direction;
import model.listeners.MovementCostListener;
import model.listeners.MovementCostSource;
import model.listeners.SelectionChangeListener;
import model.listeners.SelectionChangeSource;
import model.map.HasOwner;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.Ground;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.towns.Village;
import util.NullCleaner;
import util.Pair;
import view.map.details.FixtureList;

/**
 * The listener for clicks on tile buttons indicating movement.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ExplorationClickListener implements ActionListener,
		MovementCostSource, SelectionChangeSource {
	/**
	 * The list of movement-cost listeners.
	 */
	private final List<MovementCostListener> mcListeners = new ArrayList<>();

	/**
	 * The list of selection-change listeners.
	 */
	private final List<SelectionChangeListener> scListeners = new ArrayList<>();

	/**
	 * The exploration model.
	 */
	private final IExplorationModel model;
	/**
	 * The direction this button is from the currently selected tile.
	 */
	private final Direction direction;
	/**
	 * The list of fixtures on this tile in the main map.
	 */
	private final FixtureList list;

	/**
	 * Constructor.
	 *
	 * @param emodel the exploration model
	 * @param direct what direction this button is from the center.
	 * @param mainList the list of fixtures on this tile in the main map.
	 */
	public ExplorationClickListener(final IExplorationModel emodel,
			final Direction direct, final FixtureList mainList) {
		model = emodel;
		direction = direct;
		list = mainList;
	}

	/**
	 * @param evt the event to handle.
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				handleMove();
			}
		});
	}

	/**
	 * Handle a button press. This was refactored out of the actionPerformed
	 * method because it has to be run on the EDT to prevent concurrency issues,
	 * and putting this code in the Runnable means accessing private members
	 * from that inner class ...
	 *
	 * TODO: Remove caches from main map.
	 */
	protected void handleMove() {
		try {
			final List<TileFixture> fixtures = getSelectedValuesList(list);
			if (Direction.Nowhere.equals(direction)) {
				final int swearing = JOptionPane.showConfirmDialog(null,
						"Should the explorer swear any villages on this tile?");
				switch (swearing) {
				case JOptionPane.CANCEL_OPTION:
					return;
				case JOptionPane.YES_OPTION:
					swearVillages();
					for (final MovementCostListener listener : mcListeners) {
						listener.deduct(5);
					}
					break;
				default: // NO_OPTION
					break;
				}
			}
			model.move(direction);
			Point dPoint = model.getSelectedUnitLocation();
			Player player = NullCleaner.assertNotNull(model.getSelectedUnit()).getOwner();
			Set<CacheFixture> caches = new HashSet<>();
			for (final Pair<IMutableMapNG, File> pair : model.getSubordinateMaps()) {
				final IMutableMapNG map = pair.first();
				map.setBaseTerrain(dPoint, model.getMap()
						.getBaseTerrain(dPoint));
				for (final TileFixture fix : fixtures) {
					if (fix instanceof Ground && map.getGround(dPoint) == null) {
						map.setGround(dPoint, ((Ground) fix).copy(false));
					} else if (fix instanceof Ground
							&& fix.equals(map.getGround(dPoint))) {
						continue;
					} else if (fix instanceof Forest
							&& map.getForest(dPoint) == null) {
						map.setForest(dPoint, ((Forest) fix).copy(false));
					} else if (fix instanceof Forest
							&& fix.equals(map.getForest(dPoint))) {
						continue;
					} else if (fix instanceof Mountain) {
						map.setMountainous(dPoint, true);
					} else if (fix != null && !hasFixture(map, dPoint, fix)) {
						boolean zero = fix instanceof HasOwner && !((HasOwner) fix)
								.getOwner().equals(player);
						map.addFixture(dPoint, fix.copy(zero));
						if (fix instanceof CacheFixture) {
							caches.add((CacheFixture) fix);
						}
					}
				}
			}
			for (CacheFixture cache : caches) {
				if (cache != null) {
					model.getMap().removeFixture(dPoint, cache);
				}
			}
		} catch (final TraversalImpossibleException except) {
			final Point sel = model.getSelectedUnitLocation();
			for (final SelectionChangeListener listener : scListeners) {
				listener.selectedPointChanged(null, sel);
			}
			for (final MovementCostListener listener : mcListeners) {
				listener.deduct(1);
			}
		}
	}
	/**
	 * @param map a map
	 * @param dPoint a point
	 * @param fix a fixture
	 * @return whether the map has that fixture there
	 */
	private static boolean hasFixture(final IMapNG map, final Point dPoint,
			final TileFixture fix) {
		for (TileFixture item : map.getOtherFixtures(dPoint)) {
			if (fix.equals(item)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Change the allegiance of any villages on the current tile to the moving
	 * unit's owner.
	 */
	private void swearVillages() {
		for (final Pair<IMutableMapNG, File> pair : model.getAllMaps()) {
			final IMutableMapNG map = pair.first();
			IUnit mover = model.getSelectedUnit();
			if (mover != null) {
				final Player owner = mover.getOwner();
				for (final TileFixture fix : map.getOtherFixtures(model
						.getSelectedUnitLocation())) {
					if (fix instanceof Village) {
						((Village) fix).setOwner(owner);
					}
				}
			}
		}
	}

	/**
	 * A reimplementation of {@link javax.swing.JList#getSelectedValuesList()}
	 * that's guaranteed not to throw an ArrayIndexOutOfBoundsException.
	 *
	 * @param list the list to operate on
	 * @return a list of its selected members
	 */
	private static List<TileFixture> getSelectedValuesList(
			final FixtureList list) {
		final int[] selections = list.getSelectedIndices();
		final ListModel<TileFixture> model = list.getModel();
		final List<TileFixture> retval = new ArrayList<>();
		for (final int sel : selections) {
			if (sel < model.getSize()) {
				retval.add(model.getElementAt(sel));
			} else {
				retval.add(model.getElementAt(model.getSize() - 1));
			}
		}
		return retval;
	}

	/**
	 * @param listener the listener to add
	 */
	@Override
	public void addSelectionChangeListener(
			final SelectionChangeListener listener) {
		scListeners.add(listener);
	}

	/**
	 * @param listener the listener to remove
	 */
	@Override
	public void removeSelectionChangeListener(
			final SelectionChangeListener listener) {
		scListeners.remove(listener);
	}

	/**
	 * @param listener the listener to add
	 */
	@Override
	public void addMovementCostListener(final MovementCostListener listener) {
		mcListeners.add(listener);
	}

	/**
	 * @param listener the listener to remove
	 */
	@Override
	public void removeMovementCostListener(final MovementCostListener listener) {
		mcListeners.remove(listener);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ExplorationClickListener";
	}
}
