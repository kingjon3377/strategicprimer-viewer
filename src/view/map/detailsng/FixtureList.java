package view.map.detailsng;

import javax.swing.JList;

import model.viewer.FixtureListModel;
import util.PropertyChangeSource;

/**
 * A visual tree-based representation of the contents of a tile.
 *
 * @author Jonathan Lovelace
 */
public class FixtureList extends JList {
	/**
	 * Constructor.
	 *
	 * @param property the property the model will be listening for
	 * @param sources objects the model should listen to
	 */
	public FixtureList(final String property,
			final PropertyChangeSource... sources) {
		super(new FixtureListModel(property, sources));
		setCellRenderer(new FixtureCellRenderer());
	}
}
