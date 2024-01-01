package common.map;

import common.entity.EntityIdentifier;
import common.entity.IEntity;

/**
 * These mutators should only be called from a changeset or from initialization code (such as deserialization code
 * or test code).
 *
 * TODO: addMapRegion(), replaceMapRegion() (need precondition-checking algorithm first), for symmetry removeMapRegion()
 */
public interface IMutableMap extends IMap {
	/**
	 * Add the given entity to the map. Precondition: No entity with its ID exists in the map.
	 */
	void addEntity(IEntity entity);

	/**
	 * Remove the given entity from the map. Precondition: An "equal" entity exists in the map.
	 */
	void removeEntity(IEntity entity);

	/**
	 * Remove "toRemove" from the map and add "toAdd". Preconditions: An entity "equal" to "toRemove" exists in the map,
	 * and either no entity with the same ID as "toAdd" exists in the map, or "toRemove" is the only entity in the map
	 * with the same ID as "toAdd".
	 */
	void replaceEntity(IEntity toRemove, IEntity toAdd);

	/**
	 * Add the given player to the game. Precondition: No other player with the same player ID exists in the map.
	 */
	void addPlayer(Player player);

	/**
	 * Remove the given player from the game. Precondition: An "equal" player exists in the map.
	 */
	void removePlayer(Player player);

	/**
	 * Remove "toRemove" from the map and add "toAdd". Preconditions: A player "equal" to "toRemove" exists in the map,
	 * and either no player with the same ID as "toAdd" exists in the map, or "toRemove" is the only player in the map
	 * with the same ID as "toAdd".
	 */
	void replacePlayer(Player toRemove, Player toAdd);
}
