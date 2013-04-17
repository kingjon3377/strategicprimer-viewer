package view.map.key;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import model.map.TileType;
import util.PropertyChangeSource;

/**
 * Provides a visual "key" to the various terrain types.
 *
 * @author Jonathan Lovelace
 *
 */
public class KeyPanel extends JPanel implements PropertyChangeListener {
	/**
	 * Constructor.
	 *
	 * @param version the map version
	 * @param sources things to listen to for property change events
	 */
	public KeyPanel(final int version, final PropertyChangeSource... sources) {
		super(new GridLayout(0, 4));
		updateForVersion(version);
		setMinimumSize(new Dimension(new KeyElement(version,
				TileType.NotVisible).getMinimumSize().width * 4,
				getMinimumSize().height));
		setPreferredSize(getMinimumSize());
		for (final PropertyChangeSource source : sources) {
			source.addPropertyChangeListener(this);
		}
	}

	/**
	 * Update the key for a new map version.
	 *
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
	 *
	 * @param evt the event to handle
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("version".equals(evt.getPropertyName())) {
			updateForVersion(((Integer) evt.getNewValue()).intValue());
		}
	}
}
