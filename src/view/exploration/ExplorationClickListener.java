package view.exploration;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;
import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
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
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;
import util.Pair;
import util.TypesafeLogger;
import view.map.details.FixtureList;

/**
 * The listener for clicks on tile buttons indicating movement.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
public final class ExplorationClickListener extends AbstractAction implements
		MovementCostSource, SelectionChangeSource {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			TypesafeLogger.getLogger(ExplorationClickListener.class);
	/**
	 * The list of movement-cost listeners.
	 */
	private final Collection<MovementCostListener> mcListeners = new ArrayList<>();

	/**
	 * The list of selection-change listeners.
	 */
	private final Collection<SelectionChangeListener> scListeners = new ArrayList<>();

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
	 * @param emodel   the exploration model
	 * @param direct   what direction this button is from the center.
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
		SwingUtilities.invokeLater(this::handleMove);
	}

	/**
	 * Handle a button press. This was refactored out of the actionPerformed method
	 * because it has to be run on the EDT to prevent concurrency issues, and putting
	 * this
	 * code in the Runnable means accessing private members from that inner class ...
	 */
	protected void handleMove() {
		try {
			if (Direction.Nowhere == direction) {
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
			final Point dPoint = model.getSelectedUnitLocation();
			final Player player =
					NullCleaner.assertNotNull(model.getSelectedUnit()).getOwner();
			final Collection<CacheFixture> caches = new HashSet<>();
			final List<TileFixture> fixtures = getSelectedValuesList();
			for (final Pair<IMutableMapNG, File> pair : model.getSubordinateMaps()) {
				final IMutableMapNG map = pair.first();
				map.setBaseTerrain(dPoint, model.getMap()
						                           .getBaseTerrain(dPoint));
				for (final TileFixture fix : fixtures) {
					if ((fix instanceof Ground) && (map.getGround(dPoint) == null)) {
						map.setGround(dPoint, ((Ground) fix).copy(false));
					} else if ((fix instanceof Ground)
							           &&
							           fix.equals(
									           map.getGround(
											           dPoint))) {
						continue;
					} else if ((fix instanceof Forest)
							           &&
							           (map.getForest(
									           dPoint) ==
									            null)) {
						map.setForest(dPoint, ((Forest) fix).copy(false));
					} else if ((fix instanceof Forest)
							           &&
							           fix.equals(
									           map.getForest(
											           dPoint))) {
						continue;
					} else if (fix instanceof Mountain) {
						map.setMountainous(dPoint, true);
					} else if (!hasFixture(map, dPoint, fix)) {
						final boolean zero =
								(fix instanceof HasOwner) && !((HasOwner) fix)
										                              .getOwner()
										                              .equals
												                               (player);
						map.addFixture(dPoint, fix.copy(zero));
						if (fix instanceof CacheFixture) {
							caches.add((CacheFixture) fix);
						}
					}
				}
			}
			for (final CacheFixture cache : caches) {
				model.getMap().removeFixture(dPoint, cache);
			}
		} catch (final TraversalImpossibleException except) {
			LOGGER.log(Level.FINEST, "Attempted movement to impassable destination",
					except);
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
	 * @param map    a map
	 * @param dPoint a point
	 * @param fix    a fixture
	 * @return whether the map has that fixture there
	 */
	private static boolean hasFixture(final IMapNG map, final Point dPoint,
	                                  final TileFixture fix) {
		return StreamSupport.stream(map.getOtherFixtures(dPoint).spliterator(), false)
				       .anyMatch(fix::equals);
	}

	/**
	 * Change the allegiance of any villages on the current tile to the moving unit's
	 * owner.
	 */
	private void swearVillages() {
		for (final Pair<IMutableMapNG, File> pair : model.getAllMaps()) {
			final IMutableMapNG map = pair.first();
			final IUnit mover = model.getSelectedUnit();
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
	 * A reimplementation of {@link JList#getSelectedValuesList()} that's guaranteed not
	 * to throw an ArrayIndexOutOfBoundsException.
	 *
	 * @return a list of the list's selected members
	 */
	private List<TileFixture> getSelectedValuesList() {
		final int[] selections = list.getSelectedIndices();
		final ListModel<TileFixture> listModel = list.getModel();
		final List<TileFixture> retval = new ArrayList<>();
		for (final int sel : selections) {
			if (sel < listModel.getSize()) {
				retval.add(listModel.getElementAt(sel));
			} else {
				retval.add(listModel.getElementAt(listModel.getSize() - 1));
			}
		}
		return retval;
	}

	/**
	 * @param listener the listener to add
	 */
	@Override
	public void addSelectionChangeListener(
			                                      final SelectionChangeListener
					                                      listener) {
		scListeners.add(listener);
	}

	/**
	 * @param listener the listener to remove
	 */
	@Override
	public void removeSelectionChangeListener(
			                                         final SelectionChangeListener
					                                         listener) {
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
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ExplorationClickListener";
	}
}
