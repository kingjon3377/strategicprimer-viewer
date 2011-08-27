package view.map.main;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
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
		final InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
		final ActionMap actionMap = getActionMap();
		actionMap.put("up", new AbstractAction() {
			@SuppressWarnings("synthetic-access") // NOPMD
			@Override
			public void actionPerformed(final ActionEvent event) {
				selListener.up();
			}
		});
		actionMap.put("down", new AbstractAction() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void actionPerformed(final ActionEvent event) {
				selListener.down();
			}
		});
		actionMap.put("left", new AbstractAction() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void actionPerformed(final ActionEvent event) {
				selListener.left();
			}
		});
		actionMap.put("right", new AbstractAction() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void actionPerformed(final ActionEvent event) {
				selListener.right();
			}
		});
		changeListener = details;
		setOpaque(true);
		loadMap(newMap);
		secondaryMap = new SPMap(newMap.rows(), newMap.cols());
	}

	/**
	 * The lowest row we draw.
	 */
	private int minimumRow;
	
	/**
	 * @return the lowest row we draw
	 */
	public int getMinimumRow() {
		return minimumRow;
	}
	
	/**
	 * The highest row we draw.
	 */
	private int maximumRow;
	
	/**
	 * @return the highest row we draw.
	 */
	public int getMaximumRow() {
		return maximumRow;
	}
		
	/**
	 * The lowest column we draw.
	 */
	private int minimumCol;
	
	/**
	 * @return the lowest column we draw
	 */
	public int getMinimumCol() {
		return minimumCol;
	}
	
	/**
	 * The highest column we draw.
	 */
	private int maximumCol;
	
	/**
	 * @return the highest column we draw.
	 */
	public int getMaximumCol() {
		return maximumCol;
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
			selListener.clearSelection();
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
			minimumRow = Math.max(0, minRow);
			minimumCol = Math.max(0, minCol);
			maximumRow = Math.min(newMap.rows(), maxRow + 1) - 1;
			maximumCol = Math.min(newMap.cols(), maxCol + 1) - 1;
			for (int row = Math.max(0, minRow); row < Math.min(newMap.rows(),
					maxRow + 1); row++) {
				for (int col = Math.max(0, minCol); col < Math.min(
						newMap.cols(), maxCol + 1); col++) {
					addTile(new GUITile(newMap.getTile(row, col)));
				}
			}
			map = newMap;
			changeListener.setMap(map);
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
		tile.addMouseListener(selListener);
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
	 * Needs to know when the map is changed.
	 */
	private final DetailPanel changeListener;
	/**
	 * @param coords a set of coordinates
	 * @return the GUITile at those coordinates, if any.
	 */
	public GUITile getTile(final Point coords) {
		return locCache.get(coords);
	}
}
