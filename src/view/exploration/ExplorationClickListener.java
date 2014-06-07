package view.exploration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

import model.exploration.IExplorationModel;
import model.exploration.IExplorationModel.Direction;
import model.listeners.MovementCostListener;
import model.listeners.MovementCostSource;
import model.listeners.SelectionChangeListener;
import model.listeners.SelectionChangeSource;
import model.map.IMap;
import model.map.IMutableTile;
import model.map.ITile;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import model.map.fixtures.towns.Village;

import org.eclipse.jdt.annotation.Nullable;

import util.Pair;
import view.map.details.FixtureList;
import view.util.ErrorShower;

/**
 * The listener for clicks on tile buttons indicating movement.
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
			for (final Pair<IMap, File> pair : model.getSubordinateMaps()) {
				final IMap map = pair.first();
				final ITile tile = map.getTile(model.getSelectedUnitLocation());
				if (!(tile instanceof IMutableTile)) {
					ErrorShower.showErrorDialog(null, "Adding fixtures to "
							+ pair.second().getPath()
							+ " failed because the tile was not mutable.");
					return;
				}
				((IMutableTile) tile).setTerrain(model.getMap()
						.getTile(model.getSelectedUnitLocation()).getTerrain());
				for (final TileFixture fix : fixtures) {
					if (fix != null) {
						((IMutableTile) tile).addFixture(fix);
					}
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
	 * Change the allegiance of any villages on the current tile to the moving
	 * unit's owner.
	 */
	private void swearVillages() {
		for (final Pair<IMap, File> pair : model.getAllMaps()) {
			final IMap map = pair.first();
			final ITile tile = map.getTile(model.getSelectedUnitLocation());
			IUnit mover = model.getSelectedUnit();
			if (mover != null) {
				final Player owner = mover.getOwner();
				for (final TileFixture fix : tile) {
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
