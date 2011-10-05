package view.map.main;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import model.viewer.Point;
import model.viewer.PointFactory;
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
	 * The map we represent. Saved only so we can export it.
	 */
	private SPMap map;
	/**
	 * The secondary map.
	 */
	private SPMap secondaryMap;

	/**
	 * Constructor.
	 * 
	 * @param newMap
	 *            The map object this panel is representing
	 */
	public MapPanel(final SPMap newMap) {
		super();
		setMinimumSize(new Dimension(newMap.cols() * GUITile.TILE_SIZE, newMap.rows() * GUITile.TILE_SIZE));
		setPreferredSize(new Dimension(newMap.cols() * GUITile.TILE_SIZE, newMap.rows() * GUITile.TILE_SIZE));
		setSize(getPreferredSize());
		final InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
		setOpaque(true);
		loadMap(newMap);
		secondaryMap = new SPMap(newMap.rows(), newMap.cols());
	}
	
	/**
	 * Set up arrow-key listeners. The listener is added as a MouseListener to
	 * this and connected to the arrow keys.
	 * 
	 * @param list
	 *            a TileSelectionListener
	 */
	public void setUpListeners(final TileSelectionListener list) {
		addMouseListener(list);
		final Thread thr = new Thread() {
			/**
			 * Add the listener to all the tiles.
			 */
			@Override
			public void run() {
				for (Component comp : getComponents()) {
					comp.addMouseListener(list);
				}
			}
		};
		thr.start();
		new ArrowKeyListener().setUpListeners(list, getActionMap());
	}
	/**
	 * Our visible dimensions.
	 */
	private VisibleDimensions dimensions;
	/**
	 * @return our visible dimensions
	 */
	public VisibleDimensions getVisibleDimensions() {
		return dimensions;
	}
	/**
	 * Load and draw a subset of a map.
	 * 
	 * @param newMap
	 *            the map to load.
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
		for (MouseListener list : getMouseListeners()) {
			if (list instanceof SelectionListener) {
				((SelectionListener) list).clearSelection();
			}
		}
			removeAll();
			locCache.clear();
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					repaint();
				}
			});
			setLayout(new GridLayout(Math.min(newMap.rows(),
					Math.max(0, maxRow + 1 - minRow)), 0));
		dimensions = new VisibleDimensions(Math.max(0, minRow), Math.min(
				newMap.rows(), maxRow + 1) - 1, Math.max(0, minCol), Math.min(
				newMap.cols(), maxCol + 1) - 1);
		final int rows = Math.min(newMap.rows(), maxRow + 1);
		final int cols = Math.min(newMap.cols(), maxCol + 1);
		for (int row = Math.max(0, minRow); row < rows; row++) {
				for (int col = Math.max(0, minCol); col < cols; col++) {
					addTile(new GUITile(newMap.getTile(row, col))); // NOPMD
				}
			}
			map = newMap;
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					repaint();
				}
			});
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
	 * A cache of the GUITiles given just their position.
	 */
	private final Map<Point, GUITile> locCache = new HashMap<Point, GUITile>();

	/**
	 * Set up a new GUI tile.
	 * 
	 * @param tile
	 *            the GUI tile to set up.
	 */
	private void addTile(final GUITile tile) {
		for (MouseListener list : getMouseListeners()) {
			tile.addMouseListener(list);
		}
		add(tile);
		locCache.put(PointFactory.point(tile.getTile().getRow(), tile.getTile().getCol()), tile);
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
			final SPMap temp = map;
			loadMap(secondaryMap);
			secondaryMap = temp;
			repaint();
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
				secondaryMap.getTile(selection.getRow(), selection.getCol())
						.update(map.getTile(selection.getRow(),
								selection.getCol()));
	}
	/**
	 * @param coords a set of coordinates
	 * @return the GUITile at those coordinates, if any.
	 */
	public GUITile getTile(final Point coords) {
		return locCache.get(coords);
	}
	/**
	 * @param coords a set of coordinates
	 * @return the tile at those coordinates in the secondary map
	 */
	public Tile getSecondaryTile(final Point coords) {
		return secondaryMap.getTile(coords.row(), coords.col());
	}
}
