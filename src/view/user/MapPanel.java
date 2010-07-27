package view.user;

import java.awt.GridLayout;
import java.util.logging.Logger;

import javax.swing.JPanel;

import model.SPMap;

/**
 * A panel to display a map.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class MapPanel extends JPanel {
	/**
	 * A UID for serialization.
	 */
	private static final long serialVersionUID = 4707498496341178052L;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(MapPanel.class
			.getName());

	/**
	 * Constructor.
	 * 
	 * @param map
	 *            The map object this panel is representing
	 */
	public MapPanel(final SPMap map) {
		super();
		loadMap(map);
	}

	/**
	 * Load and draw a map
	 * 
	 * @param map
	 *            the map to load
	 */
	public final void loadMap(final SPMap map) {
		if (map != null) {
			removeAll();
			setLayout(new GridLayout(map.rows(), 0));
			for (int row = 0; row < map.rows(); row++) {
				for (int col = 0; col < map.cols(); col++) {
					if (map.getTile(row, col) == null) {
						add(new NullGUITile(row, col)); // NOPMD
					} else {
						add(new GUITile(map.getTile(row, col))); // NOPMD
					}
				}
				LOGGER.fine("Added row ");
				LOGGER.fine(Integer.toString(row));
				LOGGER.fine("\n");
			}
		}
	}
}
