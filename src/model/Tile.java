package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * A tile in a map.
 * @author Jonathan Lovelace
 *
 */
public final class Tile implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8600736789182987551L;
	/**
	 * Constructor.
	 * @param _row The row number
	 * @param _col The column number
	 * @param _type The tile type
	 */
	public Tile(final int _row, final int _col, final TileType _type) {
		row = _row;
		col = _col;
		type = _type;
		forts = new ArrayList<Fortress>();
		units = new ArrayList<Unit>();
	}
	/**
	 * The row number.
	 */
	private final int row; 
	/**
	 * @return the row number
	 */
	public int getRow() {
		return row;
	}
	/**
	 * The column number.
	 */
	private final int col; 
	/**
	 * @return the column number
	 */
	public int getCol() {
		return col;
	}
	/**
	 * Possible tile types.
	 * @author Jonathan Lovelace
	 *
	 */
	public enum TileType { 
		/**
		 * Tundra.
		 */
		Tundra, 
		/**
		 * Desert.
		 */
		Desert, 
		/**
		 * Mountain.
		 */
		Mountain, 
		/**
		 * Boreal forest.
		 */
		BorealForest,
		/**
		 * Temperate forest.
		 */
		TemperateForest, 
		/**
		 * Ocean.
		 */
		Ocean, 
		/**
		 * Plains.
		 */
		Plains, 
		/**
		 * Jungle.
		 */
		Jungle,
		/**
		 * Not visible.
		 */
		NotVisible
	}
	/**
	 * The tile type.
	 */
	private TileType type;
	/**
	 * @return the tile type
	 */
	public TileType getType() {
		return type;
	}
	/**
	 * @param _type the tile type
	 */
	public void setType(final TileType _type) {
		type = _type;
	}
	/**
	 * The fortress(es) on the tile.
	 * FIXME: Should this be a Set?
	 */
	private final ArrayList<Fortress> forts; // NOPMD
	/**
	 * FIXME: Should return a copy, not the real collection.
	 * 
	 * @return the fortress(es) on the tile
	 */
	public List<Fortress> getForts() {
		return Collections.unmodifiableList(forts);
	}
	/**
	 * Add a fortress to the tile.
	 * @param fort the fortress to add
	 */
	public void addFort(final Fortress fort) {
		forts.add(fort);
	}
	/**
	 * Remove a fortress from the tile.
	 * @param fort the fortress to remove
	 */
	public void removeFort(final Fortress fort) {
		forts.remove(fort);
	}
	/**
	 * FIXME: Should this be a Set?
	 * The units on the tile.
	 */
	private final ArrayList<Unit> units; //NOPMD 
	/**
	 * FIXME: Should return a copy, not the real collection.
	 * @return the units on the tile
	 */
	public List<Unit> getUnits() {
		return Collections.unmodifiableList(units); 
	}
	/**
	 * Add a unit to the tile.
	 * @param unit the unit to add
	 */
	public void addUnit(final Unit unit) {
		units.add(unit);
	}
	/**
	 * Remove a unit from the tile.
	 * @param unit the unit to remove
	 */
	public void removeUnit(final Unit unit) {
		units.remove(unit);
	}
}

