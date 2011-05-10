package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import model.viewer.Tile;
import model.viewer.TileType;

/**
 * A popup menu to let the user change a tile's terrain type.
 * 
 * @author Jonathan Lovelace
 */
public class TerrainChangingMenu extends JPopupMenu implements ActionListener {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = -7592568002529958689L;
	/**
	 * Map of tile types and menu items representing them.
	 */
	private static final Map<TileType, JMenuItem> ITEM_MAP = new EnumMap<TileType, JMenuItem>(
			TileType.class);
	static {
		for (final TileType type : TileType.values()) {
			ITEM_MAP.put(type, new JMenuItem(type.toString())); // NOPMD
		}
	}

	/**
	 * Constructor
	 */
	public TerrainChangingMenu() {
		super();
		for (final JMenuItem item : ITEM_MAP.values()) {
			add(item);
			item.addActionListener(this);
		}
	}

	/**
	 * The tile whose terrain we might change.
	 */
	private GUITile tile;

	/**
	 * @param newTile
	 *            the tile whose terrain we might change
	 */
	public void setTile(final GUITile newTile) {
		tile = newTile;
	}

	/**
	 * Handle Menu selections.
	 * @param event the menu-item-selected event we're handling.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if (tile != null && event != null) {
			final Tile newTile = new Tile(tile.getTile().getRow(), tile
					.getTile().getCol(), TileType.valueOf(event
					.getActionCommand()), tile.getTile().getEvent());
			newTile.update(tile.getTile());
			tile.setTile(newTile);
			tile.repaint();
		}
	}
}
