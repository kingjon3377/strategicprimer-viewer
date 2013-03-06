package model.viewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.DefaultListModel;

import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import util.PropertyChangeSource;

/**
 * A model for a FixtureList.
 *
 * @author Jonathan Lovelace
 *
 */
public class FixtureListModel extends DefaultListModel<TileFixture> implements
		PropertyChangeListener {
	/**
	 * The property we listen for.
	 */
	private static final String LISTENED_PROP = "tile";
	/**
	 * The current tile.
	 */
	private Tile tile = new Tile(TileType.NotVisible);
	/**
	 * Constructor.
	 *
	 * @param sources sources to listen to
	 */
	public FixtureListModel(final PropertyChangeSource... sources) {
		super();
		for (final PropertyChangeSource source : sources) {
			source.addPropertyChangeListener(this);
		}
	}

	/**
	 * Handle a property change.
	 *
	 * @param evt the event to handle
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if (LISTENED_PROP.equalsIgnoreCase(evt.getPropertyName())
				&& evt.getNewValue() instanceof Tile) {
			tile = (Tile) evt.getNewValue();
			this.clear();
			if (!TileType.NotVisible.equals(tile.getTerrain())) {
				addElement(new TileTypeFixture(tile.getTerrain()));
			}
			for (TileFixture fix : (Tile) evt.getNewValue()) {
				this.addElement(fix);
			}
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
	public boolean equals(final Object obj) {
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
	/**
	 * This is part of a hack to prevent intra-component drops.
	 * @return the property we listen for
	 */
	public String getProperty() {
		return LISTENED_PROP;
	}
}
