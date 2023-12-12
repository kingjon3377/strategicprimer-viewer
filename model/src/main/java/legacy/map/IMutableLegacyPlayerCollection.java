package legacy.map;

import common.map.IMutablePlayerCollection;
import common.map.Player;

/**
 * An interface for player collections that can be modified.
 */
public interface IMutableLegacyPlayerCollection extends ILegacyPlayerCollection, IMutablePlayerCollection {
	/**
	 * Clone the collection.
	 */
	@Override
	IMutableLegacyPlayerCollection copy();
}
