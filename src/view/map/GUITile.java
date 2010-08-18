package view.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.EnumMap;

import javax.swing.JLabel;

import model.viewer.Fortress;
import model.viewer.Tile;
import model.viewer.TileType;
import model.viewer.Unit;

/**
 * A GUI representation of a tile. Information about what's on the tile should
 * be indicated by a small icon or by tooltip text.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class GUITile extends JLabel {
	/**
	 * Whether this is the currently selected tile.
	 */
	private boolean selected = false;
	/**
	 * The size of each GUI tile
	 */
	private static final int TILE_SIZE = 10;
	/**
	 * 
	 */
	private static final long serialVersionUID = 4047750632787337702L;
	/**
	 * The tile this GUI-tile represents
	 */
	private final Tile tile;

	/**
	 * @return the tile this GUI represents.
	 */
	public final Tile getTile() {
		return tile;
	}

	/**
	 * Constructor.
	 * 
	 * @param _tile
	 *            the tile this will represent
	 */
	public GUITile(final Tile _tile) {
		super();
		setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
		setMinimumSize(getPreferredSize());
		setMaximumSize(null);
		setToolTipText("Terrain: " + terrainText(_tile.getType())
				+ anyForts(_tile) + anyUnits(_tile) + anyEvent(_tile));
		tile = _tile;
	}

	/**
	 * Paint the tile
	 * 
	 * @param pen
	 *            the graphics context
	 */
	@Override
	public void paint(final Graphics pen) {
		super.paint(pen);
		final Color saveColor = pen.getColor();
		pen.setColor(colorMap.get(tile.getType()));
		pen.fillRect(0, 0, getWidth(), getHeight());
		pen.setColor(Color.BLACK);
		pen.drawRect(0, 0, getWidth(), getHeight());
		if (selected) {
			pen.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
		}
		if (tile.getForts().isEmpty()
				|| tile.getType().equals(TileType.NotVisible)) {
			if (!tile.getUnits().isEmpty()
					&& !tile.getType().equals(TileType.NotVisible)) {
				pen.setColor(PURPLE);
				pen.fillOval(getWidth() / 2, getHeight() / 2, getWidth() / 4,
						getHeight() / 4);
			}
		} else {
			pen.setColor(BROWN);
			pen.fillRect(getWidth() / 4 + 1, getHeight() / 4 + 1,
					getWidth() / 2, getHeight() / 2);
		}
		pen.setColor(saveColor);
	}

	private static final Color BROWN = new Color(160, 82, 45);
	private static final Color PURPLE = new Color(148, 0, 211);

	/**
	 * @param tile2
	 *            a tile
	 * @return a String representation of the units on the tile, if any,
	 *         preceded by a newline if there are any
	 */
	private static String anyUnits(final Tile tile2) {
		final StringBuffer retval = new StringBuffer("");
		if (tile2.getUnits().size() > 0) {
			retval.append('\n');
			for (Unit u : tile2.getUnits()) {
				if (retval.length() > 1) {
					retval.append(", ");
				}
				retval.append(u.getType());
				retval.append(" (Player ");
				retval.append(u.getOwner());
				retval.append(')');
			}
		}
		return retval.toString();
	}

	/**
	 * @param tile2
	 *            a tile
	 * @return A string representation of any fortresses on the tile. If there
	 *         are any, it is preceded by a newline.
	 */
	private static String anyForts(final Tile tile2) {
		final StringBuffer retval = new StringBuffer();
		if (tile2.getForts().size() > 0) {
			retval.append("\nForts belonging to players ");
			for (Fortress f : tile2.getForts()) {
				if (retval.charAt(retval.length() - 2) != 's') {
					retval.append(", ");
				}
				retval.append(f.getOwner());
			}
		}
		return retval.toString();
	}

	/**
	 * @param tile
	 *            a tile
	 * @return A string saying what the event on the tile is, if there is one,
	 *         or if not an empty string.
	 */
	private static String anyEvent(final Tile tile) {
		return (tile == null || tile.getEvent() == -1 ? "" : "\nEvent: "
				+ Integer.toString(tile.getEvent()));
	}

	/**
	 * @param type
	 *            a terrain type
	 * @return a String representation of that terrain type
	 */
	private static String terrainText(final TileType type) {
		if (DESCRIPTIONS.containsKey(type)) {
			return DESCRIPTIONS.get(type);
		} // else
		throw new IllegalArgumentException("Unknown terrain type");
	}

	/**
	 * Descriptions of the types.
	 */
	private static final EnumMap<TileType, String> DESCRIPTIONS = new EnumMap<TileType, String>(
			TileType.class);

	private static EnumMap<TileType, Color> colorMap = new EnumMap<TileType, Color>(
			TileType.class);
	// ESCA-JAVA0076:
	static {
		DESCRIPTIONS.put(TileType.BorealForest, "Boreal Forest");
		DESCRIPTIONS.put(TileType.Desert, "Desert");
		DESCRIPTIONS.put(TileType.Jungle, "Jungle");
		DESCRIPTIONS.put(TileType.Mountain, "Mountains");
		DESCRIPTIONS.put(TileType.NotVisible, "Unknown");
		DESCRIPTIONS.put(TileType.Ocean, "Ocean");
		DESCRIPTIONS.put(TileType.Plains, "Plains");
		DESCRIPTIONS.put(TileType.TemperateForest, "Temperate Forest");
		DESCRIPTIONS.put(TileType.Tundra, "Tundra");
		colorMap.put(TileType.BorealForest, new Color(72, 218, 164));
		colorMap.put(TileType.Desert, new Color(249, 233, 28));
		colorMap.put(TileType.Jungle, new Color(229, 46, 46));
		colorMap.put(TileType.Mountain, new Color(249, 137, 28));
		colorMap.put(TileType.NotVisible, new Color(255, 255, 255));
		colorMap.put(TileType.Ocean, new Color(0, 0, 255));
		colorMap.put(TileType.Plains, new Color(0, 117, 0));
		colorMap.put(TileType.TemperateForest, new Color(72, 250, 72));
		colorMap.put(TileType.Tundra, new Color(153, 153, 153));
	}

	/**
	 * @return whether this is the currently selected tile
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param sel
	 *            whether this is the currently selected tile
	 */
	public void setSelected(final boolean sel) {
		selected = sel;
		repaint();
	}
}
