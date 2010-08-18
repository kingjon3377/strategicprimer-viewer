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
	 * The map we represent. Saved only so we can export it.
	 */
	private SPMap map;
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
	 * Load and draw a map
	 * 
	 * @param _map
	 *            the map to load
	 */
	public final void loadMap(final SPMap _map) {
		if (_map != null) {
			final Logger LOGGER = Logger.getLogger(MapPanel.class
					.getName());
			removeAll();
			setLayout(new GridLayout(_map.rows(), 0));
			for (int row = 0; row < _map.rows(); row++) {
				for (int col = 0; col < _map.cols(); col++) {
					if (_map.getTile(row, col) == null) {
						addTile(new NullGUITile(row, col)); // NOPMD
					} else {
						addTile(new GUITile(_map.getTile(row, col))); // NOPMD
					}
				}
				LOGGER.fine("Added row ");
				LOGGER.fine(Integer.toString(row));
				LOGGER.fine("\n");
			}
			this.map = _map;
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
	/**
	 * @return the map we represent
	 */
	public SPMap getMap() {
		return map;
	}
}
