package common.map;

import common.entity.IEntity;

/**
 * These mutators should only be called from a changeset or from initialization code (such as deserialization code
 * or test code).
 */
public interface IMutableMap extends IMap {
	/**
	 * Add the given region to the map. Precondition: It does not overlap any existing region (sharing an edge is fine),
	 * and no region with its ID exists in the map.
	 */
	void addMapRegion(MapRegion region);

	/**
	 * Remove the given region from the map. Precondition: An "equal" region exists in the map.
	 */
	void removeMapRegion(MapRegion region);

	/**
	 * Remove "toRemove" from the map and add "toAdd". Preconditions: A region equal to "toRemove" exists in the map,
	 * "toAdd" overlaps with either no region in the map or only with "toRemove", and either no region in the map has
	 * the same ID or only "toRemove" has the same ID.
	 */
	void replaceMapRegion(MapRegion toRemove, MapRegion toAdd);

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

	@Override
	IMutableMap copy();
}
