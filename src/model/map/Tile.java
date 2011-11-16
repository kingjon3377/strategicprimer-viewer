package model.map;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import model.map.events.NothingEvent;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;

/**
 * A tile in a map.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class Tile implements XMLWritable {
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
	 * 
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
	 * 
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
	 * 
	 * @return the tile type
	 */
	public TileType getType() {
		return type;
	}

	/**
	 * @param ttype
	 *            the tile's new type
	 */
	public void setType(final TileType ttype) {
		type = ttype;
	}

	/**
	 * The units, fortresses, and events on the tile.
	 */
	private final Set<TileFixture> contents;

	/**
	 * @param fix
	 *            something new on the tile
	 */
	public void addFixture(final TileFixture fix) {
		if (!fix.equals(NothingEvent.NOTHING_EVENT)
				&& !((fix instanceof TextFixture) && ((TextFixture) fix)
						.getText().isEmpty())) {
			if (fix instanceof RiverFixture) {
					for (River river : rivers) {
						((RiverFixture) fix).addRiver(river);
					}
					rivers = (RiverFixture) fix;
				}
			contents.add(fix);
		}
	}

	/**
	 * @param fix
	 *            something to remove from the tile
	 */
	public void removeFixture(final TileFixture fix) {
		if (rivers.equals(fix)) {
			rivers = new RiverFixture();
		}
		contents.remove(fix);
	}

	/**
	 * FIXME: Should return a copy, not the real collection.
	 * 
	 * 
	 * @return the contents of the tile
	 */
	public Set<TileFixture> getContents() {
		return Collections.unmodifiableSet(contents);
	}

	/**
	 * @param obj
	 *            an object
	 * 
	 * @return whether it is an identical tile
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| ((obj instanceof Tile) && row == ((Tile) obj).row
						&& col == ((Tile) obj).col
						&& type.equals(((Tile) obj).type)
						&& contents.equals(((Tile) obj).contents)
						&& (rivers == null ? ((Tile) obj).rivers == null
								: rivers.equals(((Tile) obj).rivers)));
	}

	/**
	 * 
	 * @return a hash-code for the object
	 */
	@Override
	public int hashCode() {
		return row + col << 2 + type.ordinal() << 6 + contents.hashCode() << 8 + +rivers
				.hashCode() << 10;
	}

	/**
	 * 
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
	private RiverFixture rivers = new RiverFixture();

	/**
	 * 
	 * @return the river directions on this tile
	 */
	public RiverFixture getRivers() {
		return (rivers == null ? new RiverFixture() : rivers);
	}

	/**
	 * @param river
	 *            a river to add
	 */
	public void addRiver(final River river) {
		if (rivers == null) {
			rivers = new RiverFixture();
		}
		if (!contents.contains(rivers)) {
			addFixture(rivers);
		}
		rivers.addRiver(river);
	}

	/**
	 * @param river
	 *            a river to remove
	 */
	public void removeRiver(final River river) {
		if (rivers != null) {
			rivers.removeRiver(river);
			if (rivers.getRivers().isEmpty()) {
				removeFixture(rivers);
			}
		}
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
		rivers.update(tile.rivers);
		type = tile.type;
	}
	/**
	 * Write the tile to XML. Returns the empty string if the tile isn't visible and contains nothing.
	 * @return an XML representation of the tile.
	 */
	@Override
	public String toXML() {
		if (TileType.NotVisible.equals(getType()) && !hasContents()) {
			return ""; // NOPMD
		} else {
			final StringBuilder sbuild = new StringBuilder("<tile row=\"");
			sbuild.append(row);
			sbuild.append("\" column=\"");
			sbuild.append(col);
			if (!(TileType.NotVisible.equals(getType()))) {
				sbuild.append("\" type=\"");
				sbuild.append(getType().toXML());
			}
			sbuild.append("\">");
			if (hasContents()) {
				sbuild.append('\n');
				for (final TileFixture fix : contents) {
					sbuild.append("\t\t\t");
					sbuild.append(fix.toXML());
					sbuild.append('\n');
				}
				for (final River river : rivers) {
					sbuild.append("\t\t\t");
					sbuild.append(river.toXML());
					sbuild.append('\n');
				}
				sbuild.append("\t\t");
			}
			sbuild.append("</tile>");
			return sbuild.toString();
		}
	}
	/**
	 * @return whether the tile has any contents.
	 */
	private boolean hasContents() {
		return (!contents.isEmpty());
	}
	/**
	 * A tile is "empty" if its tile type is NotVisible and it has no contents.
	 * @return whether this tile is "empty".
	 */
	public boolean isEmpty() {
		return TileType.NotVisible.equals(getType()) && getContents().isEmpty();
	}
}
