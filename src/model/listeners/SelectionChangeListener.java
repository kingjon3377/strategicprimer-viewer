package model.listeners;

import java.util.EventListener;

import model.map.ITile;
import model.map.Point;

import org.eclipse.jdt.annotation.Nullable;

/**
 * An interface for objects that want to know when the selected tile, or its
 * location, changes.
 *
 * @author Jonathan Lovelace
 */
public interface SelectionChangeListener extends EventListener {
	/**
	 * The selected tile's location changed.
	 *
	 * @param old the previously selected location
	 * @param newPoint the newly selected location
	 */
	void selectedPointChanged(@Nullable final Point old, final Point newPoint);

	/**
	 * The selected tile changed.
	 *
	 * @param old the previously selected tile
	 * @param newTile the newly selected tile
	 */
	void selectedTileChanged(@Nullable final ITile old, final ITile newTile);
}
