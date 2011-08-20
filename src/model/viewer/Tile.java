package model.viewer;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import model.viewer.events.NothingEvent;

/**
 * A tile in a map.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class Tile {
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
		contents = new HashSet<TileFixture>();
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
	private final Set<TileFixture> contents;
	/**
	 * @param fix something new on the tile
	 */
	public void addFixture(final TileFixture fix) {
		if (!fix.equals(NothingEvent.NOTHING_EVENT)) {
		contents.add(fix);
		}
	}
	/**
	 * @param fix something to remove from the tile
	 */
	public void removeFixture(final TileFixture fix) {
		contents.remove(fix);
	}
	/**
	 * FIXME: Should return a copy, not the real collection.
	 * 
	 * @return the contents of the tile
	 */
	public Set<TileFixture> getContents() {
		return Collections.unmodifiableSet(contents);
	}

	/**
	 * @param obj
	 *            an object
	 * @return whether it is an identical tile
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj || ((obj instanceof Tile) && row == ((Tile) obj).row
				&& col == ((Tile) obj).col 
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
		return row + col << 2 + type.ordinal() << 6 + contents
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
}
