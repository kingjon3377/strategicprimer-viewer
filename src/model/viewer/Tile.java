package model.viewer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import model.viewer.events.AbstractEvent;

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
	 * @param tileRow
	 *            The row number
	 * @param tileCol
	 *            The column number
	 * @param tileType
	 *            The tile type
	 */
	public Tile(final int tileRow, final int tileCol, final TileType tileType) {
		row = tileRow;
		col = tileCol;
		type = tileType;
		contents = new ArrayList<TileFixture>();
		event = -1;
	}

	/**
	 * Constructor.
	 * 
	 * @param tileRow
	 *            The row number
	 * @param tileCol
	 *            The column number
	 * @param tileType
	 *            The tile type
	 * @param newEvent
	 *            the event on the tile
	 */
	public Tile(final int tileRow, final int tileCol, final TileType tileType,
			final int newEvent) {
		row = tileRow;
		col = tileCol;
		type = tileType;
		contents = new ArrayList<TileFixture>();
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
	private final TileType type;

	/**
	 * @return the tile type
	 */
	public TileType getType() {
		return type;
	}
	/**
	 * The units, fortresses, and events on the tile.
	 */
	private final List<TileFixture> contents;
	/**
	 * FIXME: Should return a copy, not the real collection.
	 * 
	 * @return the contents of the tile
	 */
	public List<TileFixture> getContents() {
		return Collections.unmodifiableList(contents);
	}
	/**
	 * FIXME: Should return a copy, not the real collection.
	 * 
	 * @return the fortress(es) on the tile
	 */
	@Deprecated
	public List<Fortress> getForts() {
		final List<Fortress> retval = new ArrayList<Fortress>();
		for (TileFixture fix : contents) {
			if (fix instanceof Fortress) {
				retval.add((Fortress) fix);
			}
		}
		return retval;
	}

	/**
	 * Add a fortress to the tile.
	 * 
	 * @param fort
	 *            the fortress to add
	 */
	public void addFort(final Fortress fort) {
		contents.add(fort);
	}

	/**
	 * Remove a fortress from the tile.
	 * 
	 * @param fort
	 *            the fortress to remove
	 */
	public void removeFort(final Fortress fort) {
		contents.remove(fort);
	}

	/**
	 * FIXME: Should return a copy, not the real collection.
	 * 
	 * @return the units on the tile
	 */
	@Deprecated
	public List<Unit> getUnits() {
		final List<Unit> retval = new ArrayList<Unit>();
		for (TileFixture fix : contents) {
			if (fix instanceof Unit) {
				retval.add((Unit) fix);
			}
		}
		return retval;
	}

	/**
	 * Add a unit to the tile.
	 * 
	 * @param unit
	 *            the unit to add
	 */
	public void addUnit(final Unit unit) {
		contents.add(unit);
	}

	/**
	 * Remove a unit from the tile.
	 * 
	 * @param unit
	 *            the unit to remove
	 */
	public void removeUnit(final Unit unit) {
		contents.remove(unit);
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
		return this == obj || ((obj instanceof Tile) && row == ((Tile) obj).row
				&& col == ((Tile) obj).col && event == ((Tile) obj).event
				&& type.equals(((Tile) obj).type)
				&& contents.equals(((Tile) obj).contents)
				&& rivers.equals(((Tile) obj).rivers)
				&& tileText.equals(((Tile) obj).tileText));
	}

	/**
	 * @return a hash-code for the object
	 */
	@Override
	public int hashCode() {
		return row + col << 2 + event << 4 + type.ordinal() << 6 + contents
				.hashCode() << 8 + + rivers.hashCode() << 10 + tileText
				.hashCode() << 14;
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
		sbuilder.append(". Contents:");
		for (final TileFixture fix : contents) {
			sbuilder.append("\n\t\t");
			sbuilder.append(fix);
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
	 * @param river
	 *            a river to add
	 */
	public void addRiver(final River river) {
		rivers.add(river);
	}

	/**
	 * @param river
	 *            a river to remove
	 */
	public void removeRiver(final River river) {
		rivers.remove(river);
	}

	/**
	 * Text associated with the tile: encounter results, for instance.
	 */
	private String tileText = "";

	/**
	 * @param text
	 *            text associated with the tile
	 */
	public void setTileText(final String text) {
		tileText = ((text == null) ? "" : text);
	}

	/**
	 * @return any text associated with the tile
	 */
	public String getTileText() {
		return tileText;
	}

	/**
	 * Update with data from a tile in another map.
	 * 
	 * @param tile
	 *            the same tile in another map.
	 */
	public void update(final Tile tile) {
		contents.addAll(tile.contents);
		contents.retainAll(tile.contents);
		rivers.addAll(tile.rivers);
		rivers.retainAll(tile.rivers);
		tileText = tile.tileText;
	}
	/**
	 * Add an event to the tile. FIXME: implement.
	 * @param event the event to add.
	 */
	public void addEvent(final AbstractEvent event) {
		// TODO Auto-generated method stub
		
	}
}
