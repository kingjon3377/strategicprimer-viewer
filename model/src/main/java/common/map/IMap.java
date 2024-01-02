package common.map;

import common.entity.EntityIdentifier;
import common.entity.IEntity;

import java.util.Collection;

/**
 * A game-world within the game. TODO: terrain information, etc.
 */
public interface IMap {
	IEntity getEntity(EntityIdentifier id);
	Collection<IEntity> getAllEntities();

	/**
	 * Map regions' geometry should be scaled uniformly. Invariant: No region overlaps another. (Sharing an edge is fine.)'
	 */
	Collection<MapRegion> getRegions();

	/**
	 * The players in the map.
	 *
	 * TODO: Move the specialized functionality up to IMap etc., so users can't determine the player collection is
	 * mutable and modify it in place?
	 */
	IPlayerCollection getPlayers();
	/**
	 * TODO: Do we want to have some notion of "copy for whom" in this version of the API?
	 *
	 * @return a deep copy of the map
	 */
	IMap copy();
}
