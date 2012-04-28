package model.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.map.events.NothingEvent;
import model.map.fixtures.Animal;
import model.map.fixtures.CacheFixture;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import view.util.SystemOut;

/**
 * A tile in a map.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class Tile implements XMLWritable, Subsettable<Tile> {
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
		location = new Point(tileRow, tileCol);
		type = tileType;
		// Can't be an otherwise-preferable TreeSet because of Java bug #7030899: TreeSet ignores equals() entirely.
		contents = new HashSet<TileFixture>();
	}
	/**
	 * The tile's location.
	 */
	private final Point location;
	/**
	 * @return the tile's location
	 */
	public Point getLocation() {
		return location;
	}
	/**
	 * The tile type.
	 */
	private TileType type;

	/**
	 * 
	 * @return the kind of tile 
	 */
	public TileType getTerrain() {
		return type;
	}

	/**
	 * @param ttype
	 *            the tile's new terrain type
	 */
	public void setTerrain(final TileType ttype) {
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
				if (hasRiver()) {
					final RiverFixture rivers = getRivers();
					for (River river : (RiverFixture) fix) {
						rivers.addRiver(river);
					}
				} else if (!((RiverFixture) fix).getRivers().isEmpty()) {
					contents.add(fix);
				}
			} else {
				contents.add(fix);
			}
		}
	}

	/**
	 * @param fix
	 *            something to remove from the tile
	 */
	public void removeFixture(final TileFixture fix) {
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
				|| ((obj instanceof Tile) && location.equals(((Tile) obj).location)
						&& type.equals(((Tile) obj).type)
						&& contents.equals(((Tile) obj).contents));
	}

	/**
	 * 
	 * @return a hash-code for the object
	 */
	@Override
	public int hashCode() {
		return location.hashCode() + type.ordinal() << 6 + contents.hashCode() << 8;
	}

	/**
	 * 
	 * @return a String representation of the tile
	 */
	@Override
	public String toString() {
		final StringBuilder sbuilder = new StringBuilder("");
		sbuilder.append(location.toString());
		sbuilder.append(": ");
		sbuilder.append(type);
		sbuilder.append(". Contents:");
		for (final TileFixture fix : contents) {
			sbuilder.append("\n\t\t");
			sbuilder.append(fix);
		}
		return sbuilder.toString();
	}

	/**
	 * @param river
	 *            a river to add
	 */
	public void addRiver(final River river) {
		if (hasRiver()) {
			getRivers().addRiver(river);
		} else {
			addFixture(new RiverFixture(river));
		}
	}

	/**
	 * @param river
	 *            a river to remove
	 */
	public void removeRiver(final River river) {
		if (hasRiver()) {
			final RiverFixture rivers = getRivers();
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
		type = tile.type;
	}
	/**
	 * Write the tile to XML. Returns the empty string if the tile isn't visible and contains nothing.
	 * @return an XML representation of the tile.
	 */
	@Override
	public String toXML() {
		if (isEmpty()) {
			return ""; // NOPMD
		} else {
			final StringBuilder sbuild = new StringBuilder("<tile ");
			sbuild.append(location.toXML());
			if (!(TileType.NotVisible.equals(getTerrain()))) {
				sbuild.append(" kind=\"");
				sbuild.append(getTerrain().toXML());
				sbuild.append("\"");
			}
			sbuild.append(">");
			if ((!contents.isEmpty())) {
				sbuild.append('\n');
				for (final TileFixture fix : contents) {
					sbuild.append("\t\t\t");
					sbuild.append(fix.toXML());
					sbuild.append('\n');
				}
				sbuild.append("\t\t");
			}
			sbuild.append("</tile>");
			return sbuild.toString();
		}
	}
	/**
	 * A tile is "empty" if its tile type is NotVisible and it has no contents.
	 * @return whether this tile is "empty".
	 */
	public boolean isEmpty() {
		return TileType.NotVisible.equals(getTerrain()) && getContents().isEmpty();
	}
	/**
	 * @return whether we contain a RiverFixture
	 */
	public boolean hasRiver() {
		for (final TileFixture fix : contents) {
			if (fix instanceof RiverFixture) {
				return true; // NOPMD
			}
		}
		return false;
	}
	/**
	 * Call hasRiver() before this, because this will throw IllegalStateException if we don't actually contain a river.
	 * @return the RiverFixture that we contain
	 */
	public RiverFixture getRivers() {
		for (final TileFixture fix : contents) {
			if (fix instanceof RiverFixture) {
				return (RiverFixture) fix;
			}
		}
		throw new IllegalStateException("Didn't find a RiverFixture");
	}
	/**
	 * @param obj another Tile
	 * @return whether it's a strict subset of this one, having no members this one doesn't
	 */
	@Override
	public boolean isSubset(final Tile obj) {
		if (location.equals(obj.location) && type.equals(obj.type)) {
			final Set<TileFixture> temp = new HashSet<TileFixture>(obj.contents);
			temp.removeAll(contents);
			final List<TileFixture> tempList = new ArrayList<TileFixture>(temp);
			for (TileFixture fix : tempList) {
				if (shouldSkip(fix)) {
					temp.remove(fix);
				}
			}
			if (!temp.isEmpty()) {
				SystemOut.SYS_OUT.print("\nExtra fixture in " + location.toString() + ":\t");
				for (TileFixture fix : temp) {
					SystemOut.SYS_OUT.print(fix.toString());
				}
			}
			return temp.isEmpty(); // NOPMD
		} else {
			SystemOut.SYS_OUT.print("Type of " + location.toString() + " wrong\t");
			return false;
		}
	}

	/**
	 * @param fix a fixture
	 * @return whether strict-subset calculations should skip it.
	 */
	private static boolean shouldSkip(final TileFixture fix) {
		return fix instanceof CacheFixture || fix instanceof TextFixture
				|| (fix instanceof Animal && ((Animal) fix).isTraces());
	}
}
