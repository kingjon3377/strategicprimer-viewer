package view.map.key;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;

import model.listeners.VersionChangeListener;
import model.map.TileType;

/**
 * Provides a visual "key" to the various terrain types.
 *
 * @author Jonathan Lovelace
 *
 */
public class KeyPanel extends JPanel implements VersionChangeListener {
	/**
	 * Constructor.
	 *
	 * @param version the map version
	 */
	public KeyPanel(final int version) {
		super(new GridLayout(0, 4));
		updateForVersion(version);
		setMinimumSize(new Dimension(new KeyElement(version,
				TileType.NotVisible).getMinimumSize().width * 4,
				getMinimumSize().height));
		setPreferredSize(getMinimumSize());
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
	 * @param old the previous map version
	 * @param newVersion the new map version
	 */
	@Override
	public void changeVersion(final int old, final int newVersion) {
		updateForVersion(newVersion);
	}
}
