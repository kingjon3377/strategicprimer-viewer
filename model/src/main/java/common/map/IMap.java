package common.map;

import common.entity.EntityIdentifier;
import common.entity.IEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * A game-world within the game. TODO: terrain information, etc. TODO: Add nullness annotations
 */
public interface IMap {
	@Nullable IEntity getEntity(@NotNull EntityIdentifier id);

	@NotNull Collection<IEntity> getAllEntities();

	/**
	 * Map regions' geometry should be scaled uniformly. Invariant: No region overlaps another (sharing an edge is
	 * fine), and no two regions have the same ID number.
	 */
	@NotNull Collection<MapRegion> getRegions();

	/**
	 * The players in the map.
	 *
	 * TODO: Move the specialized functionality up to IMap etc., so users can't determine the player collection is
	 * mutable and modify it in place?
	 */
	@NotNull IPlayerCollection getPlayers();

	/**
	 * TODO: Do we want to have some notion of "copy for whom" in this version of the API?
	 *
	 * @return a deep copy of the map
	 */
	@NotNull IMap copy();
}
