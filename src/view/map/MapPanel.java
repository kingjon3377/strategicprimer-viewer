package view.map;

import java.awt.GridLayout;
import java.util.logging.Logger;

import javax.swing.JPanel;

import model.viewer.SPMap;
import model.viewer.Tile;

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
	 * The map we represent. Saved only so we can export it.
	 */
	private SPMap map;
	private static final Logger LOGGER = Logger.getLogger(MapPanel.class.getName());

	/**
	 * Constructor.
	 * 
	 * @param _map
	 *            The map object this panel is representing
	 */
	public MapPanel(final SPMap _map) {
		super();
		selListener = new SelectionListener();
		loadMap(_map);
	}

	/**
	 * Load and draw a subset of a map.
	 * 
	 * @param _map
	 *            the map to load
	 * @param minRow
	 *            the first row to draw
	 * @param maxRow
	 *            the last row to draw
	 * @param minCol
	 *            the first column to draw
	 * @param maxCol
	 *            the last column to draw
	 */
	public final void loadMap(final SPMap _map, final int minRow,
			final int maxRow, final int minCol, final int maxCol) {
		if (_map != null) {
			LOGGER.info("Started loading panel");
			LOGGER.info(Long.toString(System.currentTimeMillis()));
			removeAll();
			setLayout(new GridLayout(Math.min(_map.rows(), Math.max(0, maxRow
					+ 1 - minRow)), 0));
			for (int row = Math.max(0, minRow); row < _map.rows()
					&& row < maxRow + 1; row++) {
				for (int col = Math.max(0, minCol); col < _map.cols()
						&& col < maxCol + 1; col++) {
					final Tile tile = _map.getTile(row, col);
					if (tile == null) {
						addTile(new NullGUITile(row, col)); // NOPMD
					} else {
						addTile(new GUITile(tile)); // NOPMD
					}
				}
				LOGGER.fine("Added row ");
				LOGGER.fine(Integer.toString(row));
				LOGGER.fine("\n");
			}
			this.map = _map;
			LOGGER.info("Finished loading panel");
			LOGGER.info(Long.toString(System.currentTimeMillis()));
		}
	}

	/**
	 * Load and draw a map
	 * 
	 * @param _map
	 *            the map to load
	 */
	public final void loadMap(final SPMap _map) {
		loadMap(_map, 0, Integer.MAX_VALUE - 1, 0, Integer.MAX_VALUE - 1);
	}

	/**
	 * Set up a new GUI tile
	 * 
	 * @param tile
	 *            the GUI tile to set up.
	 */
	private void addTile(final GUITile tile) {
		tile.addMouseListener(selListener);
		add(tile);
	}

	/**
	 * @return the map we represent
	 */
	public SPMap getMap() {
		return map;
	}
}
