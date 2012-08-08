package model.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public final class Tile extends SimpleTile {
	/**
	 * Constructor.
	 *
	 * @param tileRow The row number
	 * @param tileCol The column number
	 * @param tileType The tile type
	 * @param filename the file this was loaded from
	 */
	public Tile(final int tileRow, final int tileCol, final TileType tileType,
			final String filename) {
		super(PointFactory.point(tileRow, tileCol), tileType, filename);
		// Can't be an otherwise-preferable TreeSet because of Java bug
		// #7030899: TreeSet ignores equals() entirely.
		contents = new HashSet<TileFixture>();
	}

	/**
	 * The units, fortresses, and events on the tile.
	 */
	private final Set<TileFixture> contents;

	/**
	 * @param fix something new on the tile
	 */
	public void addFixture(final TileFixture fix) {
		if (!((fix instanceof TextFixture) && ((TextFixture) fix).getText()
				.isEmpty())) {
			if (fix instanceof RiverFixture) {
				if (hasRiver()) {
					final RiverFixture rivers = getRivers();
					rivers.addRivers((RiverFixture) fix);
				} else if (!((RiverFixture) fix).getRivers().isEmpty()) {
					contents.add(fix);
				}
			} else {
				contents.add(fix);
			}
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
	 *
	 * @return the contents of the tile
	 */
	public Set<TileFixture> getContents() {
		return Collections.unmodifiableSet(contents);
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it is an identical tile
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| ((obj instanceof Tile) && super.equals(obj) && contents
						.equals(((Tile) obj).contents));
	}

	/**
	 *
	 * @return a hash-code for the object
	 */
	@Override
	public int hashCode() {
		return super.hashCode() + contents.hashCode() << 8;
	}

	/**
	 *
	 * @return a String representation of the tile
	 */
	@Override
	public String toString() {
		final StringBuilder sbuilder = new StringBuilder(super.toString());
		sbuilder.append(" Contents:");
		for (final TileFixture fix : contents) {
			sbuilder.append("\n\t\t");
			sbuilder.append(fix);
		}
		return sbuilder.toString();
	}

	/**
	 * @param river a river to add
	 */
	public void addRiver(final River river) {
		if (hasRiver()) {
			getRivers().addRiver(river);
		} else {
			addFixture(new RiverFixture(river));
			getRivers().setFile(getFile());
		}
	}

	/**
	 * @param river a river to remove
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
	 * @param tile the same tile in another map.
	 */
	@Override
	public void update(final SimpleTile tile) {
		super.update(tile);
		if (tile instanceof Tile) {
			final Set<TileFixture> unmatchedContents = new HashSet<TileFixture>(
					contents);
			unmatchedContents.removeAll(((Tile) tile).contents);
			for (final TileFixture local : unmatchedContents) {
				for (final TileFixture remote : ((Tile) tile).getContents()) {
					if (local.equalsIgnoringID(remote)) {
						removeFixture(local);
						addFixture(remote);
						break;
					}
				}
			}
		}
	}

	/**
	 * Write the tile to XML. Returns the empty string if the tile isn't visible
	 * and contains nothing.
	 *
	 * @deprecated Replaced by SPIntermediateRepresentation-based output
	 * @return an XML representation of the tile.
	 */
	@Override
	@Deprecated
	public String toXML() {
		if (isEmpty()) {
			return ""; // NOPMD
		} else {
			final StringBuilder sbuild = new StringBuilder("<tile ");
			sbuild.append(getLocation().toXML());
			if (!(TileType.NotVisible.equals(getTerrain()))) {
				sbuild.append(" kind=\"");
				sbuild.append(getTerrain().toXML());
				sbuild.append("\"");
			}
			sbuild.append(">");
			if (!contents.isEmpty()) {
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
	 *
	 * @return whether this tile is "empty".
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && getContents().isEmpty();
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
	 * Call hasRiver() before this, because this will throw
	 * IllegalStateException if we don't actually contain a river.
	 *
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
	 * @return whether it's a strict subset of this one, having no members this
	 *         one doesn't
	 */
	@Override
	public boolean isSubset(final SimpleTile obj) {
		if (super.isSubset(obj)) {
			if (contents.isEmpty()) {
				return true; // NOPMD
			} else if (obj instanceof Tile) {
				final Set<TileFixture> temp = new HashSet<TileFixture>(
						((Tile) obj).contents);
				temp.removeAll(contents);
				final List<TileFixture> tempList = new ArrayList<TileFixture>(
						temp);
				for (final TileFixture fix : tempList) {
					if (shouldSkip(fix)) {
						temp.remove(fix);
					}
				}
				if (!temp.isEmpty()) {
					SystemOut.SYS_OUT.print("\nExtra fixture in "
							+ getLocation().toString() + ":\t");
					for (final TileFixture fix : temp) {
						SystemOut.SYS_OUT.print(fix.toString());
					}
				}
				return temp.isEmpty(); // NOPMD
			} else {
				SystemOut.SYS_OUT
						.print("Other is a SimpleTile, this is not, at "
								+ getLocation().toString());
				return false; // NOPMD
			}
		} else {
			SystemOut.SYS_OUT.print("Type of " + getLocation().toString()
					+ " wrong\t");
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

	/**
	 * @return a clone of this object
	 */
	@Override
	public SimpleTile deepCopy() {
		final Tile retval = new Tile(getLocation().row(), getLocation().col(),
				getTerrain(), getFile());
		for (final TileFixture fix : contents) {
			retval.contents.add(fix.deepCopy());
		}
		return retval;
	}

	/**
	 * Set the file property of this tile and all its children to the specified
	 * value, recursively.
	 *
	 * @param value the value to set
	 */
	@Override
	public void setFile(final String value) {
		super.setFile(value);
		for (final TileFixture fix : contents) {
			fix.setFile(value);
		}
	}
}
