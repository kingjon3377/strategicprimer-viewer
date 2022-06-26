package common.map;

import java.nio.file.Path;
import org.jetbrains.annotations.Nullable;

/**
 * A mutable map.
 */
public interface IMutableMapNG extends IMapNG {
	/**
	 * Add a player to the map.
	 */
	void addPlayer(Player player);

	/**
	 * The base terrain at any given point.
	 * @return the prior terrain, if any
	 */
	@Nullable
	TileType setBaseTerrain(Point location, @Nullable TileType terrain);

	/**
	 * Whether the given point is mountainous.
	 * @return the prior value of the property there
	 */
	boolean setMountainous(Point location, boolean mountainous);

	/**
	 * Add rivers at a location.
	 */
	void addRivers(Point location, River... addedRivers);

	/**
	 * Remove rivers at a location.
	 */
	void removeRivers(Point location, River... removedRivers);

	/**
	 * Set the road level at a location for a direction.
	 */
	void setRoadLevel(Point location, Direction direction, int quality);

	/**
	 * Add a tile fixture at the given location. Return whether the
	 * collection of fixtures has an additional member as a result of this
	 * operation; if the fixture was already present, or if it replaces one
	 * that was present, this returns false.
	 */
	boolean addFixture(Point location, TileFixture fixture);

	/**
	 * Remove a fixture from the given location.
	 */
	void removeFixture(Point location, TileFixture fixture);

	/**
	 * Set the current player.
	 */
	void setCurrentPlayer(Player currentPlayer);

	/**
	 * Set the current turn.
	 */
	void setCurrentTurn(int currentTurn);

	/**
	 * Set the file from which the map was loaded, or to which it should be saved.
	 *
	 * FIXME: Notify map metadata listeners when changed
	 *
	 * TODO: Now the type doesn't have to *exactly* match, should the parameter here really be nullable?
	 */
	void setFilename(@Nullable Path filename);

	/**
	 * Set whether the map has been modified since it was last saved.
	 *
	 * FIXME: Notify map metadata listeners when changed
	 */
	void setModified(boolean modified);

	/**
	 * Add a bookmark.
	 *
	 * @param point Where to place the bookmark
	 * @param player The player to place the bookmark for
	 */
	void addBookmark(Point point, Player player);

	/**
	 * Add a bookmark for the current player.
	 *
	 * @param point Where to place the bookmark
	 */
	default void addBookmark(final Point point) {
		addBookmark(point, getCurrentPlayer());
	}

	/**
	 * Remove a bookmark.
	 * @param point Where to remove the bookmark
	 * @param player The player to remove the bookmark for
	 */
	void removeBookmark(Point point, Player player);

	/**
	 * Remove a bookmark for the current player.
	 * @param point Where to remove the bookmark
	 */
	default void removeBookmark(final Point point) {
		removeBookmark(point, getCurrentPlayer());
	}

	/**
	 * Replace an existing fixture, "original", if present, with
	 * a new one, "replacement". If "original" was not present,
	 * add "replacement" anyway.  Order within the list of fixtures
	 * is of course not guaranteed, but subclass implementations are
	 * encouraged to use a replace-in-place operation to minimize churn in
	 * the XML serialized form.
	 *
	 * TODO: return boolean if the map was changed?
	 */
	default void replace(final Point location, final TileFixture original, final TileFixture replacement) {
		removeFixture(location, original);
		addFixture(location, replacement);
	}
}
