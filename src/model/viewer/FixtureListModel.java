package model.viewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
	private final String listenedProperty;
	/**
	 * The current tile.
	 */
	private Tile tile = new Tile(-1, -1, TileType.NotVisible, "string");
	/**
	 * Constructor.
	 *
	 * @param property The property to listen for to get the new tile
	 * @param sources sources to listen to
	 */
	public FixtureListModel(final String property,
			final PropertyChangeSource... sources) {
		super();
		listenedProperty = property;
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
		if (listenedProperty.equalsIgnoreCase(evt.getPropertyName())
				&& evt.getNewValue() instanceof Tile) {
			tile = (Tile) evt.getNewValue();
			this.clear();
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
		if (tile.addFixture(fix)) {
			// addFixture returns false if it wasn't actually added---e.g. it
			// was already in the set---so we only want to add it to the display
			// if it returns true.
			addElement(fix);
		}
	}
}
