package model.map;

import java.io.PrintStream;

/**
 * A view of a map. This is in effect an extension of SPMap that adds the
 * current turn, the current player, and eventually changesets.
 *
 * @author Jonathan Lovelace
 *
 */
public class MapView implements IMap {
	/**
	 * Constructor. We get the current-player *object* from the wrapped map.
	 *
	 * @param wrapped the map this wraps
	 * @param curPlayer the current player's number
	 * @param curTurn the current turn
	 */
	public MapView(final SPMap wrapped, final int curPlayer, final int curTurn) {
		super();
		map = wrapped;
		map.getPlayers().getPlayer(curPlayer).setCurrent(true);
		turn = curTurn;
	}

	/**
	 * The map we wrap.
	 */
	private final SPMap map;
	/**
	 * The current turn.
	 */
	private int turn;

	/**
	 * Test whether another map or map view is a subset of this one. TODO: Check
	 * changesets.
	 *
	 * @param obj the map to check
	 * @return whether it's a strict subset of this one
	 * @param out the stream to write details to
	 */
	@Override
	public boolean isSubset(final IMap obj, final PrintStream out) {
		return map.isSubset(obj, out);
	}

	/**
	 * @param obj another map.
	 * @return the result of a comparison with it.
	 */
	@Override
	public int compareTo(final IMap obj) {
		return map.compareTo(obj);
	}

	/**
	 * Add a player to the wrapped map.
	 *
	 * @param newPlayer the player to add
	 */
	@Override
	public void addPlayer(final Player newPlayer) {
		map.addPlayer(newPlayer);
	}

	/**
	 * @param point a pair of coordinates
	 * @return the tile at those coordinates
	 */
	@Override
	public Tile getTile(final Point point) {
		return map.getTile(point);
	}

	/**
	 * @return the collection of players in the map
	 */
	@Override
	public PlayerCollection getPlayers() {
		return map.getPlayers();
	}

	/**
	 * TODO: changesets affect this.
	 *
	 * @return the collection of tiles that make up the map.
	 */
	@Override
	public TileCollection getTiles() {
		return map.getTiles();
	}

	/**
	 * Set the current player.
	 *
	 * @param current the new current player (number)
	 */
	public void setCurrentPlayer(final int current) {
		map.getPlayers().getCurrentPlayer().setCurrent(false);
		map.getPlayers().getPlayer(current).setCurrent(true);
	}

	/**
	 * Set the current turn.
	 *
	 * @param current the new current turn
	 */
	public void setCurrentTurn(final int current) {
		turn = current;
	}

	/**
	 * TODO: sub-maps, changesets.
	 *
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj || (obj instanceof MapView && map.equals(((MapView) obj).map)
				&& turn == ((MapView) obj).turn);
	}

	/**
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return map.hashCode();
	}

	/**
	 * @return the current turn
	 */
	public int getCurrentTurn() {
		return turn;
	}

	/**
	 * TODO: How does this interact with changesets? This is primarily used
	 * (should probably *only* be used) in serialization.
	 *
	 * @return the map this wraps
	 */
	public SPMap getMap() {
		return map;
	}

	/**
	 * @return a String representation of the view.
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder("Map view at turn ");
		builder.append(turn);
		builder.append(":\nCurrent player:");
		builder.append(map.getPlayers().getCurrentPlayer());
		builder.append("\nMap:\n");
		builder.append(map);
		return builder.toString();
	}
	/**
	 * @return The map's dimensions and version.
	 */
	@Override
	public MapDimensions getDimensions() {
		return map.getDimensions();
	}
}
