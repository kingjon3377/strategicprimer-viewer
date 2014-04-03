package model.viewer;

import javax.swing.DefaultListModel;

import model.listeners.SelectionChangeListener;
import model.map.IMutableTile;
import model.map.ITile;
import model.map.Point;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;

import org.eclipse.jdt.annotation.Nullable;

import view.util.ErrorShower;

/**
 * A model for a FixtureList.
 *
 * TODO: tests
 *
 * @author Jonathan Lovelace
 *
 */
public final class FixtureListModel extends DefaultListModel<TileFixture>
		implements SelectionChangeListener {
	/**
	 * The current tile.
	 */
	private ITile tile = new Tile(TileType.NotVisible);

	/**
	 * @param old the formerly selected location
	 * @param newPoint the newly selected location
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point old,
			final Point newPoint) {
		// Do nothing; we only care about the tile, not its location.
	}

	/**
	 * @param old the formerly selected tile
	 * @param newTile the newly selected tile
	 */
	@Override
	public void selectedTileChanged(@Nullable final ITile old,
			final ITile newTile) {
		tile = newTile;
		this.clear();
		if (!TileType.NotVisible.equals(tile.getTerrain())) {
			addElement(new TileTypeFixture(tile.getTerrain()));
		}
		for (final TileFixture fix : tile) {
			addElement(fix);
		}
	}

	/**
	 * Add a tile fixture to the current tile.
	 *
	 * @param fix the fixture to add.
	 */
	public void addFixture(final TileFixture fix) {
		if (tile instanceof IMutableTile) {
			if (fix instanceof TileTypeFixture) {
				if (!tile.getTerrain().equals(
						((TileTypeFixture) fix).getTileType())) {
					((IMutableTile) tile).setTerrain(((TileTypeFixture) fix)
							.getTileType());
				}
				addElement(fix);
			} else if (((IMutableTile) tile).addFixture(fix)) {
				// addFixture returns false if it wasn't actually added---e.g. it
				// was already in the set---so we only want to add it to the display
				// if it returns true.
				addElement(fix);
			}
		} else {
			ErrorShower.showErrorDialog(null,
					"Cannot add a fixture: selected tile is not mutable");
		}
	}

	/**
	 * Remove the specified items from the tile and the list.
	 *
	 * @param list the list of items to remove. If null, none are removed.
	 */
	public void remove(@Nullable final Iterable<TileFixture> list) {
		if (tile instanceof IMutableTile) {
			if (list != null) {
				for (final TileFixture fix : list) {
					if (fix == null) {
						continue;
					} else if (fix instanceof TileTypeFixture) {
						((IMutableTile) tile).setTerrain(TileType.NotVisible);
						removeElement(fix);
					} else if (((IMutableTile) tile).removeFixture(fix)) {
						removeElement(fix);
					}
				}
			}
		} else {
			ErrorShower.showErrorDialog(null, "Cannot remove item from list: "
					+ "selected tile is not mutable");
		}
	}

	/**
	 * A FixtureListModel is equal only to another FixtureListModel listening
	 * for the same property and representing the same tile.
	 *
	 * @param obj an object
	 * @return whether we're equal to it
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof FixtureListModel
				&& ((FixtureListModel) obj).tile.equals(tile);
	}

	/**
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return tile.hashCode();
	}
}
