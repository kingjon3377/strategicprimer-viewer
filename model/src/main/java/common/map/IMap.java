package common.map;

import common.entity.EntityIdentifier;
import common.entity.IEntity;

/**
 * A game-world within the game. TODO: terrain information, number and arrangement of regions, etc.
 */
public interface IMap {
	IEntity getEntity(EntityIdentifier id);
}
