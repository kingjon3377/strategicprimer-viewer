package model.map;


/**
 * A view of a map. This is in effect an extension of SPMap that adds the
 * current turn, the current player, and eventually
 * changesets.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class MapView implements IMap {
	/**
	 * Constructor. We get the current-player *object* from the wrapped map.
	 * @param wrapped the map this wraps
	 * @param curPlayer the current player's number
	 * @param curTurn the current turn
	 * @param filename the file this is loaded from
	 */
	public MapView(final SPMap wrapped, final int curPlayer, final int curTurn, final String filename) {
		map = wrapped;
		player = map.getPlayers().getPlayer(curPlayer);
		player.setCurrent(true);
		turn = curTurn;
		file = filename;
	}
	/**
	 * The map we wrap.
	 */
	private final SPMap map;
	/**
	 * The current player.
	 */
	private Player player;
	/**
	 * The current turn.
	 */
	private int turn;
	/**
	 * @return an XML representation of the view.
	 */
	@Deprecated
	@Override
	public String toXML() {
		final StringBuilder builder = new StringBuilder("<view current_player=\"");
		builder.append(player.getPlayerId());
		builder.append("\" current_turn=\"");
		builder.append(turn);
		builder.append("\">\n");
		builder.append(map.toXML());
		builder.append("\n</view>");
		return builder.toString();
	}
	/**
	 * The file this view was read from.
	 */
	private String file;
	/**
	 * @return the file this was read from
	 */
	@Override
	public String getFile() {
		return file;
	}
	/**
	 * @param origFile the file this view was read from
	 */
	@Override
	public void setFile(final String origFile) {
		file = origFile;
	}
	
	/**
	 * Test whether another map or map view is a subset of this one. TODO: Check
	 * changesets.
	 * 
	 * @param obj the map to check
	 * @return whether it's a strict subset of this one
	 */
	@Override
	public boolean isSubset(final IMap obj) {
		return map.isSubset(obj);
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
	 * @return the map version of the wrapped map
	 */
	@Override
	public int getVersion() {
		return map.getVersion();
	}
	/**
	 * @return the number of rows in the wrapped map
	 */
	@Override
	public int rows() {
		return map.rows();
	}
	/**
	 * @return the number of columns in the wrapped map
	 */
	@Override
	public int cols() {
		return map.cols();
	}
	/**
	 * Add a player to the wrapped map. 
	 * @param newPlayer the player to add
	 */
	@Override
	public void addPlayer(final Player newPlayer) {
		map.addPlayer(newPlayer);
	}
	/**
	 * TODO: changesets affect this.
	 * @param row the row of a pair of coordinates
	 * @param col the column of a pair of coordinates
	 * @return the tile at those coordinates
	 */
	@Override
	public Tile getTile(final int row, final int col) {
		return map.getTile(row, col);
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
	 * @return the collection of tiles that make up the map.
	 */
	@Override
	public TileCollection getTiles() {
		return map.getTiles();
	}
	/**
	 * Set the current player.
	 * @param current the new current player (number)
	 */
	public void setCurrentPlayer(final int current) {
		player.setCurrent(false);
		player = map.getPlayers().getPlayer(current);
		player.setCurrent(true);
	}
	/**
	 * Set the current turn.
	 * @param current the new current turn
	 */
	public void setCurrentTurn(final int current) {
		turn = current;
	}
	/**
	 * TODO: sub-maps, changesets.
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof MapView && map.equals(((MapView) obj).map)
				&& player.equals(((MapView) obj).player)
				&& turn == ((MapView) obj).turn;
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
		StringBuilder builder = new StringBuilder("Map view at turn ");
		builder.append(turn);
		builder.append(":\nCurrent player:");
		builder.append(player);
		builder.append("\nMap:\n");
		builder.append(map);
		return builder.toString();
	}
	/**
	 * @return a clone of this object
	 */
	@Override
	public IMap deepCopy() {
		// TODO: changesets
		return new MapView((SPMap) map.deepCopy(), getPlayers()
				.getCurrentPlayer().getPlayerId(), getCurrentTurn(), getFile());
	}
	/**
	 * Set all children's file property to the specified value, recursively.
	 * @param value the value to set
	 */
	@Override
	public void setFileOnChildren(final String value) {
		map.setFile(value);
		map.setFileOnChildren(value);
	}
}
