package view.map.key;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import model.map.TileType;

/**
 * Provides a visual "key" to the various terrain types.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class KeyPanel extends JPanel implements PropertyChangeListener {
	/**
	 * Constructor.
	 * @param version the map version
	 */
	public KeyPanel(final int version) {
		super(new GridLayout(0, 3));
		updateForVersion(version);
		setMinimumSize(new Dimension(getMinimumSize().width, new KeyElement(version, 
				TileType.NotVisible).getMinimumSize().height * 3));
		setPreferredSize(getMinimumSize());
	}
	/**
	 * Update the key for a new map version.
	 * @param version the map version
	 */
	private void updateForVersion(final int version) {
		removeAll();
		for (final TileType type : TileType.valuesForVersion(version)) {
			add(new KeyElement(version, type)); // NOPMD
		}
	}
	/**
	 * Listen for property changes---specifically, the map version.
	 * @param evt the event to handle
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("version".equals(evt.getPropertyName())) {
			updateForVersion((Integer) evt.getNewValue());
		}
	}
}
