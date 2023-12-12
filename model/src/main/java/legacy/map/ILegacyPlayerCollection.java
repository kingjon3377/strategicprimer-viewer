package legacy.map;

import common.map.IPlayerCollection;
import common.map.Player;

/**
 * An interface for collections of players.
 */
public interface ILegacyPlayerCollection extends IPlayerCollection, Subsettable<Iterable<Player>> {
}
