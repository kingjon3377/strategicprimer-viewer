package view.map.detailsng;

import javax.swing.JTable;

import model.viewer.FixtureTableModel;
import util.PropertyChangeSource;
/**
 * A table-based view of a tile's contents.
 * @author Jonathan Lovelace
 *
 */
public class FixtureTable extends JTable {
	/**
	 * Constructor.
	 * @param property the property the model will be listening for
	 * @param sources objects the model should listen to
	 */
	public FixtureTable(final String property, final PropertyChangeSource... sources) {
		super(new FixtureTableModel(property, sources));
		setDefaultRenderer(Object.class, new FixtureCellRenderer());
		setTableHeader(null);
	}
}
