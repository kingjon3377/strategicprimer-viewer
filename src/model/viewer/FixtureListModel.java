package model.viewer;

import java.util.List;

import javax.swing.DefaultListModel;

import model.listeners.SelectionChangeListener;
import model.listeners.SelectionChangeSource;
import model.map.Point;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A model for a FixtureList.
 *
 * TODO: tests
 *
 * @author Jonathan Lovelace
 *
 */
public class FixtureListModel extends DefaultListModel<TileFixture> implements
		SelectionChangeListener {
	/**
	 * The current tile.
	 */
	private Tile tile = new Tile(TileType.NotVisible);
	/**
	 * Constructor.
	 *
	 * @param sources sources to listen to
	 */
	public FixtureListModel(final Iterable<? extends SelectionChangeSource> sources) {
		super();
		for (final SelectionChangeSource source : sources) {
			source.addSelectionChangeListener(this);
		}
	}
	/**
	 * @param old the formerly selected location
	 * @param newPoint the newly selected location
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point old, final Point newPoint) {
		// Do nothing; we only care about the tile, not its location.
	}
	/**
	 * @param old the formerly selected tile
	 * @param newTile the newly selected tile
	 */
	@Override
	public void selectedTileChanged(@Nullable final Tile old, final Tile newTile) {
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
	 * @param fix the fixture to add.
	 */
	public void addFixture(final TileFixture fix) {
		if (fix instanceof TileTypeFixture) {
			if (!tile.getTerrain().equals(((TileTypeFixture) fix).getTileType())) {
				tile.setTerrain(((TileTypeFixture) fix).getTileType());
			}
			addElement(fix);
		} else if (tile.addFixture(fix)) {
			// addFixture returns false if it wasn't actually added---e.g. it
			// was already in the set---so we only want to add it to the display
			// if it returns true.
			addElement(fix);
		}
	}
	/**
	 * Remove the specified items from the tile and the list.
	 * @param list the list of items to remove
	 */
	public void remove(final List<TileFixture> list) {
		for (TileFixture fix : list) {
			if (fix instanceof TileTypeFixture) {
				tile.setTerrain(TileType.NotVisible);
				removeElement(fix);
			} else if (tile.removeFixture(fix)) {
				removeElement(fix);
			}
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
		return this == obj
				|| (obj instanceof FixtureListModel && ((FixtureListModel) obj).tile
						.equals(tile));
	}
	/**
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return tile.hashCode();
	}
}
