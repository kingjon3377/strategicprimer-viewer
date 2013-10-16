package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import model.listeners.SelectionChangeListener;
import model.listeners.SelectionChangeSource;
import model.listeners.SelectionChangeSupport;
import model.listeners.VersionChangeListener;
import model.listeners.VersionChangeSource;
import model.map.Point;
import model.map.Tile;
import model.map.TileType;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A popup menu to let the user change a tile's terrain type.
 *
 * @author Jonathan Lovelace
 */
public class TerrainChangingMenu extends JPopupMenu implements ActionListener,
		VersionChangeListener, SelectionChangeSource, SelectionChangeListener {
	/**
	 * Constructor.
	 *
	 * @param version the map version
	 * @param list a PropertyChangeListener to listen to us
	 * @param initialTile the initial tile
	 * @param vsource a VersionChangeSource we should listen to
	 * @param ssource a SelectionChangeSource we should listen to
	 */
	public TerrainChangingMenu(final int version, final Tile initialTile,
			final SelectionChangeListener list,
			final VersionChangeSource vsource,
			final SelectionChangeSource ssource) {
		super();
		tile = initialTile;
		addSelectionChangeListener(list);
		updateForVersion(version);
		vsource.addVersionChangeListener(this);
		ssource.addSelectionChangeListener(this);
	}

	/**
	 * Update the menu for a new version.
	 *
	 * @param version the version
	 */
	private void updateForVersion(final int version) {
		for (final TileType type : TileType.valuesForVersion(version)) {
			final JMenuItem item = new JMenuItem(type.toString()); // NOPMD
			add(item);
			item.addActionListener(this);
		}
	}

	/**
	 * The tile whose terrain we might change.
	 */
	private Tile tile;

	/**
	 * @param newTile the tile whose terrain we might change
	 */
	public void setTile(final Tile newTile) {
		tile = newTile;
	}

	/**
	 * Handle Menu selections.
	 *
	 * @param event the menu-item-selected event we're handling.
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent event) {
		if (event != null) {
			tile.setTerrain(TileType.valueOf(event.getActionCommand()));
			scs.fireChanges(null, null, null, tile);
		}
	}

	/**
	 * @param old the previously selected version
	 * @param newVersion the newly selected version
	 */
	@Override
	public void changeVersion(final int old, final int newVersion) {
		updateForVersion(newVersion);
	}

	/**
	 * @param list a listener to add
	 */
	@Override
	public final void addSelectionChangeListener(
			final SelectionChangeListener list) {
		scs.addSelectionChangeListener(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public final void removeSelectionChangeListener(
			final SelectionChangeListener list) {
		scs.removeSelectionChangeListener(list);
	}

	/**
	 * The helper to handle selection-change listeners for us.
	 */
	private final SelectionChangeSupport scs = new SelectionChangeSupport();

	/**
	 * @param old ignored
	 * @param newPoint ignored
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point old,
			final Point newPoint) {
		// We only care about the tile itself
	}

	/**
	 * @param old the previously selected tile
	 * @param newTile the newly selected tile
	 */
	@Override
	public void selectedTileChanged(@Nullable final Tile old, final Tile newTile) {
		tile = newTile;
	}
}
