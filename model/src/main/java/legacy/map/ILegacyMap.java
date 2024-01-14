package legacy.map;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

/**
 * An interface for game-world maps.
 *
 * This is the fourth-generation interface:
 *
 * <ul>
 * <li>The first map implementation modeled a map as a collection of Tile
 * objects, and exposed this in the interface that was extracted from it. That
 * approach proved expensive from a performance perspective, but changing it
 * required redesigning the interface.</li>
 * <li>The second-generation interface was designed so that an implementer
 * <em>could</em> use Tile objects, but callers whould be oblivious to that
 * detail. Instead, callers asked for the tile type, rivers, forest, mountain,
 * fixtures, etc., mapped to a given Point. The interface also included several
 * of the features that, in the first implementation, were in a MapView class
 * that wrapped the SPMap class.</li>
 * <li>The third generation was built using the same principles as the second,
 * but was adapted to use interfaces for which Ceylon provided "syntax sugar".</li>
 * <li>This fourth generation ports the third-generation interface back to Java.</li>
 * </ul>
 *
 * Mutator methods, including those used in constructing the map object, are
 * deliberately out of the scope of this interface.
 * @see IMutableLegacyMap
 */
public interface ILegacyMap extends Subsettable<ILegacyMap> {
	/**
	 * The dimensions (and version) of the map.
	 */
	MapDimensions getDimensions();

	/**
	 * (A view of) the players in the map.
	 */
	ILegacyPlayerCollection getPlayers();

	/**
	 * The locations in the map. This should <em>not</em> include locations
	 * outside the dimensions of the map even if callers have modified
	 * them, but <em>should</em> include all points within the dimensions of the
	 * map even if they are "empty".
	 */
	Iterable<Point> getLocations();

	/**
	 * A stream of the locations in the map, the same as provided in {@link #getLocations}.
	 */
	Stream<Point> streamLocations();

	/**
	 * The base terrain at the given location.
	 */
	@Nullable
	TileType getBaseTerrain(Point location);

	/**
	 * Whether the given location is mountainous.
	 */
	boolean isMountainous(Point location);

	/**
	 * The rivers in the map.
	 */
	Collection<River> getRivers(Point location);

	/**
	 * Roads in the map.
	 *
	 * FIXME: We don't want to return an interface that includes mutators
	 * TODO: Should we have Road objects instead?
	 * TODO: Make a RoadQuality enum
	 */
	Map<Direction, Integer> getRoads(Point location);

	/**
	 * The tile-fixtures at the various locations.
	 */
	Collection<TileFixture> getFixtures(Point location);

	/**
	 * A stream of all the tile-fixtures in all the locations in the map.
	 *
	 * TODO: Should this be extended to include non-"tile" fixtures inside fortresses and units?
	 */
	default Stream<TileFixture> streamAllFixtures() {
		return streamLocations().flatMap(l -> getFixtures(l).stream());
	}

	/**
	 * The current turn.
	 */
	int getCurrentTurn();

	/**
	 * The current player.
	 */
	Player getCurrentPlayer();

	/**
	 * The current player's bookmarks.
	 */
	Set<Point> getBookmarks();

	/**
	 * Bookmarks for another player.
	 */
	Set<Point> getBookmarksFor(Player player);

	/**
	 * All bookmarks at the given location.
	 */
	Collection<Player> getAllBookmarks(Point location);

	/**
	 * Clone the map.
	 *
	 * TODO: What should this do with the filename? Take a filename parameter?
	 *
	 * @param zero Whether to "zero" sensitive data TODO: move CopyBehavior elsewhere than IFixture
	 * @param player The player for whom the copied map is being prepared, if any."
	 */
	ILegacyMap copy(IFixture.CopyBehavior zero, @Nullable Player player);

	/**
	 * The file from which the map was loaded, or to which it should be saved, if known
	 */
	@Nullable
	Path getFilename();

	/**
	 * Whether the map has been modified since it was last saved.
	 */
	boolean isModified();

	/**
	 * A location is empty if it has no terrain, no Ground, no Forest, no
	 * rivers, no roads, no bookmarks, and no other fixtures
	 */
	default boolean isLocationEmpty(final Point location) {
		if (!Objects.isNull(getBaseTerrain(location))) {
			return false;
		} else if (isMountainous(location)) {
			return false;
		} else if (!getRivers(location).isEmpty()) {
			return false;
		} else if (!getRoads(location).isEmpty()) {
			return false;
		} else if (!getFixtures(location).isEmpty()) {
			return false;
		} else {
			return getAllBookmarks(location).isEmpty();
		}
	}
}
