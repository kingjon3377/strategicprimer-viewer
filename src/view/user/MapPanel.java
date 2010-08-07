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
	 * Selection listener.
	 */
	private final transient SelectionListener selListener;
	/**
	 * Constructor.
	 * 
	 * @param map
	 *            The map object this panel is representing
	 */
	public MapPanel(final SPMap map) {
		super();
		selListener = new SelectionListener();
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
			final Logger LOGGER = Logger.getLogger(MapPanel.class
					.getName());
			removeAll();
			setLayout(new GridLayout(map.rows(), 0));
			for (int row = 0; row < map.rows(); row++) {
				for (int col = 0; col < map.cols(); col++) {
					if (map.getTile(row, col) == null) {
						addTile(new NullGUITile(row, col)); // NOPMD
					} else {
						addTile(new GUITile(map.getTile(row, col))); // NOPMD
					}
				}
				LOGGER.fine("Added row ");
				LOGGER.fine(Integer.toString(row));
				LOGGER.fine("\n");
			}
		}
	}

	/**
	 * Set up a new GUI tile
	 * @param tile the GUI tile to set up.
	 */
	private void addTile(final GUITile tile) {
		tile.addMouseListener(selListener);
		add(tile);
	}
}
