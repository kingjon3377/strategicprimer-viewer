package drivers.exploration.old;

import org.jetbrains.annotations.Nullable;
import legacy.map.TileType;
import legacy.map.Point;
import legacy.map.MapDimensions;
import legacy.map.TileFixture;

import java.util.Set;

/**
 * An interface for encounter tables, for the now-nearly-defunct second model
 * of generating results. This class's methods produce data for the Judge's
 * use; to produce results for a player we would need to know the explorer's
 * Perception modifier and perhaps other data.
 */
public interface EncounterTable {
	enum TerrainModifier {
		Mountains,
		None
	}
	/**
	 * Generates an appropriate event, an "encounter." For {@link
	 * QuadrantTable}s this is always the same for each tile, for
	 * random-event tables the result is randomly selected from that table,
	 * and so on.
	 *
	 * @param point         The location of the tile in question.
	 * @param terrain       The terrain there. Null if unknown.
	 * @param terrainMod   Whether the tile is mountainous.
	 * @param fixtures      The fixtures on the tile, if any.
	 * @param mapDimensions The dimensions of the map.
	 */
	String generateEvent(Point point, @Nullable TileType terrain, TerrainModifier terrainMod,
						 Iterable<TileFixture> fixtures, MapDimensions mapDimensions);

	/**
	 * For table-debugging purposes, return the set of all events the table can return.
	 *
	 * TODO: Somehow restrict visibility of this
	 */
	Set<String> getAllEvents();
}
