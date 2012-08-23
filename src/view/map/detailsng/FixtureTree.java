package view.map.detailsng;

import javax.swing.JTree;

import model.viewer.FixtureTreeModel;
import util.PropertyChangeSource;

/**
 * A visual tree-based representation of the contents of a tile.
 *
 * @author Jonathan Lovelace
 */
public class FixtureTree extends JTree {
	/**
	 * Constructor.
	 * @param property the property the model will be listening for
	 * @param sources objects the model should listen to
	 */
	public FixtureTree(final String property, final PropertyChangeSource... sources) {
		super(new FixtureTreeModel(property, sources));
		setCellRenderer(new FixtureTreeRenderer());
	}
}
