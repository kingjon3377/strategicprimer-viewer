package common.map;

import common.entity.EntityIdentifier;
import common.entity.IEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SPMap implements IMutableMap {
	private final Map<EntityIdentifier, IEntity> entityMap = new HashMap<>();
	private final Map<Integer, MapRegion> regionMap = new HashMap<>();
	private final IMutablePlayerCollection players = new PlayerCollection();

	@Override
	public final IEntity getEntity(final EntityIdentifier id) {
		return entityMap.get(id);
	}

	@Override
	public final @NotNull Collection<IEntity> getAllEntities() {
		return entityMap.values();
	}

	@Override
	public final @NotNull Collection<MapRegion> getRegions() {
		return regionMap.values();
	}

	@Override
	public final @NotNull IPlayerCollection getPlayers() {
		return players.copy();
	}

	@Override
	public final void addMapRegion(final MapRegion region) {
		final Collection<MapRegion> temp = new ArrayList<>(regionMap.values());
		temp.add(region);
		if (MapRegion.areRegionsValid(temp)) {
			regionMap.put(region.getRegionId(), region);
		} else {
			throw new IllegalArgumentException("Added region must have unique ID and not overlap existing regions");
		}
	}

	@Override
	public final void removeMapRegion(final MapRegion region) {
		final MapRegion matching = regionMap.get(region.getRegionId());
		if (Objects.equals(region, matching)) {
			regionMap.remove(region.getRegionId(), matching);
		} else {
			throw new IllegalArgumentException("Region not present in the map");
		}
	}

	@Override
	public final void replaceMapRegion(final MapRegion toRemove, final MapRegion toAdd) {
		final Collection<MapRegion> temp = new ArrayList<>(regionMap.values());
		if (!temp.remove(toRemove)) {
			throw new IllegalArgumentException("Region to remove must exist in the map");
		}
		temp.add(toAdd);
		if (MapRegion.areRegionsValid(temp)) {
			if (toRemove.getRegionId() == toAdd.getRegionId()) {
				regionMap.replace(toRemove.getRegionId(), toRemove, toAdd);
			} else {
				regionMap.remove(toRemove.getRegionId(), toRemove);
				regionMap.put(toAdd.getRegionId(), toAdd);
			}
		} else {
			throw new IllegalArgumentException("Added region must have unique ID and not overlap existing regions");
		}
	}

	@Override
	public final void addEntity(final IEntity entity) {
		if (entityMap.containsKey(entity.getId())) {
			throw new IllegalArgumentException("Entity ID must be unique in the map");
		} else {
			entityMap.put(entity.getId(), entity);
		}
	}

	@Override
	public final void removeEntity(final IEntity entity) {
		if (!entityMap.remove(entity.getId(), entity)) {
			throw new IllegalArgumentException("Entity to remove must exist in the map");
		}
	}

	@Override
	public final void replaceEntity(final IEntity toRemove, final IEntity toAdd) {
		if (toRemove.getId().equals(toAdd.getId())) {
			if (!entityMap.replace(toRemove.getId(), toRemove, toAdd)) {
				throw new IllegalArgumentException("Entity to remove must exist in the map");
			}
		} else if (entityMap.containsKey(toAdd.getId())) {
			throw new IllegalArgumentException("Entity to add must not share an ID with any other entity in the map");
		} else if (Objects.equals(entityMap.get(toRemove.getId()), toRemove)) {
			entityMap.remove(toRemove.getId(), toRemove);
			entityMap.put(toAdd.getId(), toAdd);
		} else {
			throw new IllegalArgumentException("Entity to remove must exist in the map");
		}
	}

	@Override
	public final void addPlayer(final Player player) {
		players.add(player);
	}

	@Override
	public final void removePlayer(final Player player) {
		players.remove(player);
	}

	@Override
	public final void replacePlayer(final Player toRemove, final Player toAdd) {
		// TODO: Add replace() method to IMutablePlayerCollection?
		if (!Objects.equals(toRemove, players.getPlayer(toRemove.playerId()))) {
			throw new IllegalArgumentException("Player to remove must exist in the map");
		} else if (toRemove.playerId() != toAdd.playerId() &&
				!players.getPlayer(toAdd.playerId()).getName().isEmpty()) {
			// TODO: Make some way to either get the player in the collection, *or nothing if none*, or else test if a
			//  player exists in the collection
			throw new IllegalArgumentException("Player to add must not have an ID already in use after removal");
		} else {
			players.remove(toRemove);
			players.add(toAdd);
		}
	}

	@Override
	public final @NotNull IMutableMap copy() {
		final SPMap retval = new SPMap();
		entityMap.values().forEach(retval::addEntity);
		// Don't use addRegion() because precondition checking is expensive
		for (final MapRegion region : regionMap.values()) {
			retval.regionMap.put(region.getRegionId(), region);
		}
		players.forEach(retval::addPlayer);
		return retval;
	}
}
