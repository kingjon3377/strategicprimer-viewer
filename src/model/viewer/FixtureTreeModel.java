package model.viewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.tree.DefaultTreeModel;

import model.map.Tile;
import model.map.TileType;
import util.PropertyChangeSource;
/**
 * A model for a FixtureTree.
 * @author Jonathan Lovelace
 *
 */
public class FixtureTreeModel extends DefaultTreeModel implements PropertyChangeListener {
	/**
	 * The property we listen for.
	 */
	private final String listenedProperty;
	/**
	 * Constructor.
	 * @param property The property to listen for to get the new tile
	 * @param sources sources to listen to
	 */
	public FixtureTreeModel(final String property, final PropertyChangeSource... sources) {
		super(new TileNode(new Tile(-1, -1, TileType.NotVisible, "")));
		listenedProperty = property;
		for (PropertyChangeSource source : sources) {
			source.addPropertyChangeListener(this);
		}
	}
	/**
	 * Handle a property change.
	 * @param evt the event to handle
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if (listenedProperty.equalsIgnoreCase(evt.getPropertyName())) {
			setRoot(new TileNode((Tile) evt.getNewValue()));
		}
	}

}
