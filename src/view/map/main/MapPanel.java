package view.map.main;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JPanel;

import model.viewer.SPMap;
import model.viewer.Tile;
import util.Pair;

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
	private final transient TileSelectionListener selListener;
	/**
	 * The map we represent. Saved only so we can export it.
	 */
	private SPMap map;
	/**
	 * The secondary map.
	 */
	private SPMap secondaryMap;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(MapPanel.class
			.getName());

	/**
	 * Constructor.
	 * 
	 * @param newMap
	 *            The map object this panel is representing
	 * @param details
	 *            The panel that'll show the details of the selected tile
	 */
	public MapPanel(final SPMap newMap, final DetailPanel details) {
		super();
		selListener = new TileSelectionListener(this, details);
		setOpaque(true);
		loadMap(newMap);
	}

	/**
	 * Load and draw a subset of a map.
	 * 
	 * @param newMap
	 *            the map to load. If null, ignored.
	 * @param minRow
	 *            the first row to draw
	 * @param maxRow
	 *            the last row to draw
	 * @param minCol
	 *            the first column to draw
	 * @param maxCol
	 *            the last column to draw
	 */
	public final void loadMap(final SPMap newMap, final int minRow,
			final int maxRow, final int minCol, final int maxCol) {
		if (newMap != null) {
			LOGGER.info("Started loading panel");
			LOGGER.info(Long.toString(System.currentTimeMillis()));
			selListener.clearSelection();
			removeAll();
			setLayout(new GridLayout(Math.min(newMap.rows(),
					Math.max(0, maxRow + 1 - minRow)), 0));
			for (int row = Math.max(0, minRow); row < Math.min(newMap.rows(),
					maxRow + 1); row++) {
				for (int col = Math.max(0, minCol); col < Math.min(
						newMap.cols(), maxCol + 1); col++) {
					addTile(row, col, newMap.getTile(row, col));
				}
				LOGGER.fine("Added row ");
				LOGGER.fine(Integer.toString(row));
				LOGGER.fine("\n");
			}
			map = newMap;
			LOGGER.info("Finished loading panel");
			LOGGER.info(Long.toString(System.currentTimeMillis()));
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					repaint();
				}
			});
		}
	}

	/**
	 * Load and draw a map.
	 * 
	 * @param newMap
	 *            the map to load
	 */
	public final void loadMap(final SPMap newMap) {
		loadMap(newMap, 0, Integer.MAX_VALUE - 1, 0, Integer.MAX_VALUE - 1);
	}

	/**
	 * Set up a new tile, possibly getting it from cache.
	 * 
	 * @param row
	 *            the row
	 * @param col
	 *            the column
	 * @param tile
	 *            the tile we're setting up a GUI for.
	 */
	private void addTile(final int row, final int col, final Tile tile) {
		if (tile == null) {
			if (!nullCache.containsKey(Pair.of(row, col))) {
				nullCache.put(Pair.of(row, col), new NullGUITile(row, col));
			}
			addTile(nullCache.get(Pair.of(row, col)));
		} else {
			if (!tileCache.containsKey(tile)) {
				tileCache.put(tile, new GUITile(tile));
			}
			addTile(tileCache.get(tile));
		}
	}

	/**
	 * A cache of NullTiles.
	 */
	private final Map<Pair<Integer, Integer>, NullGUITile> nullCache = new HashMap<Pair<Integer, Integer>, NullGUITile>();
	/**
	 * A cache of GUITiles.
	 */
	private final Map<Tile, GUITile> tileCache = new HashMap<Tile, GUITile>();

	/**
	 * Set up a new GUI tile.
	 * 
	 * @param tile
	 *            the GUI tile to set up.
	 */
	private void addTile(final GUITile tile) {
		tile.addMouseListener(selListener);
		add(tile);
		tile.setVisible(true);
		tile.repaint();
	}

	/**
	 * @return the map we represent
	 */
	public SPMap getMap() {
		return map;
	}

	/**
	 * @param secMap
	 *            the new secondary map
	 */
	public void setSecondaryMap(final SPMap secMap) {
		secondaryMap = secMap;
	}

	/**
	 * Swap the main and secondary maps, i.e. show the secondary map
	 */
	public void swapMaps() {
		if (secondaryMap != null) {
			final SPMap temp = map;
			loadMap(secondaryMap);
			secondaryMap = temp;
		}
	}

	/**
	 * @return the secondary map
	 */
	public SPMap getSecondaryMap() {
		return secondaryMap;
	}

	/**
	 * Copy a tile from the main map to the secondary map.
	 * 
	 * @param selection a tile in the relevant position.
	 */
	public void copyTile(final Tile selection) {
		if (map.getTile(selection.getRow(), selection.getCol()) != null
				&& secondaryMap != null) {
			if (secondaryMap.getTile(selection.getRow(), selection.getCol()) == null) {
				secondaryMap.addTile(map.getTile(selection.getRow(),
						selection.getCol()));
			} else {
				secondaryMap.getTile(selection.getRow(), selection.getCol())
						.update(map.getTile(selection.getRow(),
								selection.getCol()));
			}
		}
	}
}
