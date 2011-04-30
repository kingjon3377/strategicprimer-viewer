package model.viewer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * A tile in a map.
 * 
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
	 * 
	 * @param _row
	 *            The row number
	 * @param _col
	 *            The column number
	 * @param _type
	 *            The tile type
	 */
	public Tile(final int _row, final int _col, final TileType _type) {
		row = _row;
		col = _col;
		type = _type;
		forts = new ArrayList<Fortress>();
		units = new ArrayList<Unit>();
		event = -1;
	}

	/**
	 * Constructor.
	 * 
	 * @param _row
	 *            The row number
	 * @param _col
	 *            The column number
	 * @param _type
	 *            The tile type
	 * @param newEvent
	 *            the event on the tile
	 */
	public Tile(final int _row, final int _col, final TileType _type,
			final int newEvent) {
		row = _row;
		col = _col;
		type = _type;
		forts = new ArrayList<Fortress>();
		units = new ArrayList<Unit>();
		event = newEvent;
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
	 * The event on the tile, if any.
	 */
	private final int event;

	/**
	 * @return the column number
	 */
	public int getCol() {
		return col;
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
	 * @param _type
	 *            the tile type
	 */
	public void setType(final TileType _type) {
		type = _type;
	}

	/**
	 * The fortress(es) on the tile. FIXME: Should this be a Set?
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
	 * 
	 * @param fort
	 *            the fortress to add
	 */
	public void addFort(final Fortress fort) {
		forts.add(fort);
	}

	/**
	 * Remove a fortress from the tile.
	 * 
	 * @param fort
	 *            the fortress to remove
	 */
	public void removeFort(final Fortress fort) {
		forts.remove(fort);
	}

	/**
	 * FIXME: Should this be a Set? The units on the tile.
	 */
	private final ArrayList<Unit> units; // NOPMD

	/**
	 * FIXME: Should return a copy, not the real collection.
	 * 
	 * @return the units on the tile
	 */
	public List<Unit> getUnits() {
		return Collections.unmodifiableList(units);
	}

	/**
	 * Add a unit to the tile.
	 * 
	 * @param unit
	 *            the unit to add
	 */
	public void addUnit(final Unit unit) {
		units.add(unit);
	}

	/**
	 * Remove a unit from the tile.
	 * 
	 * @param unit
	 *            the unit to remove
	 */
	public void removeUnit(final Unit unit) {
		units.remove(unit);
	}

	/**
	 * @return the event on the tile
	 */
	public int getEvent() {
		return event;
	}

	/**
	 * @param obj
	 *            an object
	 * @return whether it is an identical tile
	 */
	@Override
	public boolean equals(final Object obj) {
		return (obj instanceof Tile) && row == ((Tile) obj).row
				&& col == ((Tile) obj).col && event == ((Tile) obj).event
				&& type.equals(((Tile) obj).type)
				&& forts.equals(((Tile) obj).forts)
				&& rivers.equals(((Tile) obj).rivers)
				&& tileText.equals(((Tile) obj).tileText)
				&& units.equals(((Tile) obj).units);
	}

	/**
	 * @return a hash-code for the object
	 */
	@Override
	public int hashCode() {
		return row + col << 2 + event << 4 + type.ordinal() << 6 + forts
				.hashCode() << 8 + units.hashCode() << 10 + rivers.hashCode() << 12 +
				tileText.hashCode() << 14;
	}
	/**
	 * @return a String representation of the tile
	 */
	@Override
	public String toString() {
		final StringBuilder sbuilder = new StringBuilder("");
		sbuilder.append('(');
		sbuilder.append(row);
		sbuilder.append(", ");
		sbuilder.append(col);
		sbuilder.append("): ");
		sbuilder.append(type);
		sbuilder.append(", event ");
		sbuilder.append(event);
		sbuilder.append(". Forts:");
		for (Fortress fort : forts) {
			sbuilder.append("\n\t\t");
			sbuilder.append(fort);
		}
		sbuilder.append("\n\tUnits:");
		for (Unit unit : units) {
			sbuilder.append("\n\t\t");
			sbuilder.append(unit);
		}
		return sbuilder.toString();
	}
	
	/**
	 * The river-directions on this tile.
	 */
	private final Set<River> rivers = EnumSet.noneOf(River.class);
	
	/**
	 * @return the river directions on this tile
	 */
	public Set<River> getRivers() {
		return EnumSet.copyOf(rivers);
	}
	
	/**
	 * @param river a river to add
	 * @return the result of the operation
	 */
	public boolean addRiver(final River river) {
		return rivers.add(river);
	}
	
	/**
	 * @param river a river to remove
	 * @return the result of the operation
	 */
	public boolean removeRiver(final River river) {
		return rivers.remove(river);
	}
	/**
	 * Text associated with the tile: encounter results, for instance.
	 */
	private String tileText = "";
	/**
	 * @param text text associated with the tile
	 */
	public void setTileText(final String text) {
		tileText = (text == null ? "" : text);
	}
	
	/**
	 * @return any text associated with the tile
	 */
	public String getTileText() {
		return tileText;
	}
}
