package view.map.details;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;

import model.map.TileType;

/**
 * Provides a visual "key" to the various terrain types.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class KeyPanel extends JPanel {
	/**
	 * Constructor.
	 * @param version the map version
	 */
	public KeyPanel(final int version) {
		super(new GridLayout(0, 3));
		for (final TileType type : TileType.valuesForVersion(version)) {
			add(new KeyElement(version, type)); // NOPMD
		}
		setMinimumSize(new Dimension(getMinimumSize().width, new KeyElement(version, 
				TileType.NotVisible).getMinimumSize().height * 3));
		setPreferredSize(getMinimumSize());
	}
}
