package model.map.fixtures;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import model.map.River;
import model.map.TileFixture;
/**
 * A Fixture to encapsulate the rivers on a tile, so we can show a chit for rivers.
 * @author Jonathan Lovelace
 *
 */
public class RiverFixture implements TileFixture, Iterable<River> {
	/**
	 * The Set we're using to hold the Rivers.
	 */
	private final Set<River> rivers = EnumSet.noneOf(River.class);
	/**
	 * @return an XML representation of the rivers on the tile.
	 */
	@Override
	public String toXML() {
		final StringBuilder sbuild = new StringBuilder();
		for (River river : rivers) {
			sbuild.append("\t\t\t");
			sbuild.append(river.toXML());
			sbuild.append('\n');
		}
		return sbuild.toString();
	}
	/**
	 * Add a river.
	 * @param river the river to add
	 */
	public void addRiver(final River river) {
		rivers.add(river);
	}
	/**
	 * Remove a river.
	 * @param river the river to remove
	 */
	public void removeRiver(final River river) {
		rivers.remove(river);
	}
	/**
	 * @return the river directions
	 */
	public Set<River> getRivers() {
		return EnumSet.copyOf(rivers);
	}
	/**
	 * @return an iterator over the rivers
	 */
	@Override
	public Iterator<River> iterator() {
		return rivers.iterator();
	}
	/**
	 * @param obj an object
	 * @return whether it's an identical RiverFixture
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof RiverFixture && ((RiverFixture) obj).rivers
						.equals(rivers));
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return rivers.hashCode();
	}
	/**
	 * Update to match the rivers in another RiverFixture.
	 * @param source the fixture to update from
	 */
	public void update(final RiverFixture source) {
		rivers.addAll(source.getRivers());
		rivers.retainAll(source.getRivers());
	}
}
