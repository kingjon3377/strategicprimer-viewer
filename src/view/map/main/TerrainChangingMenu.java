package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import model.map.Tile;
import model.map.TileType;

/**
 * A popup menu to let the user change a tile's terrain type.
 * 
 * @author Jonathan Lovelace
 */
public class TerrainChangingMenu extends JPopupMenu implements ActionListener,
		PropertyChangeListener {
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
	 * Constructor.
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
	private Tile tile;

	/**
	 * @param newTile
	 *            the tile whose terrain we might change
	 */
	public void setTile(final Tile newTile) {
		tile = newTile;
	}

	/**
	 * Handle Menu selections.
	 * 
	 * @param event
	 *            the menu-item-selected event we're handling.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if (event != null) {
			tile.setType(TileType.valueOf(event.getActionCommand()));
			firePropertyChange("tile", null, tile);
		}
	}

	/**
	 * Listen for property changes.
	 * 
	 * @param evt
	 *            the property-change event to handle
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("tile".equals(evt.getPropertyName())
				&& evt.getNewValue() instanceof Tile) {
			tile = (Tile) evt.getNewValue();
		}
	}
}
