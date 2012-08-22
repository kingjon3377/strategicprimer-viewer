package model.viewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultListModel;

import model.map.Tile;
import model.map.TileFixture;
import util.PropertyChangeSource;

/**
 * A model for a FixtureTree.
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
			this.clear();
			for (TileFixture fix : (Tile) evt.getNewValue()) {
				this.addElement(fix);
			}
		}
	}

}
