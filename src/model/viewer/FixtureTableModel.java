package model.viewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;

import javax.swing.table.DefaultTableModel;

import model.map.Tile;
import model.map.TileFixture;
import util.PropertyChangeSource;
/**
 * The model for a table-based detail-view implementation.
 * @author Jonathan Lovelace
 *
 */
public class FixtureTableModel extends DefaultTableModel implements PropertyChangeListener {
	/**
	 * There's only one column: this is basically a list, but JList doesn't work well enough.
	 * @return 1
	 */
	@Override
	public int getColumnCount() {
		return 1;
	}
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
	public FixtureTableModel(final String property,
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
			this.setRowCount(0);
			for (TileFixture fix : (Tile) evt.getNewValue()) {
				addRow(Collections.singletonList(fix).toArray());
			}
		}
	}
	/**
	 * We disable editing.
	 * @param rowIndex ignored
	 * @param mColIndex ignored
	 * @return false
	 */
	@Override
    public boolean isCellEditable(final int rowIndex, final int mColIndex) {
        return false;
	}
}
