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
	 */
	public KeyPanel() {
		super(new GridLayout(0, 3));
		for (final TileType type : TileType.values()) {
			add(new KeyElement(type)); // NOPMD
		}
		setMinimumSize(new Dimension(getMinimumSize().width, new KeyElement(
				TileType.NotVisible).getMinimumSize().height * 3));
		setPreferredSize(getMinimumSize());
	}
}
