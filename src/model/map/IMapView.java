package model.map;

/**
 * An interface for map-views.
 * @author Jonathan Lovelace
 * @deprecated the old API is deprecated in this branch
 */
@Deprecated
public interface IMapView extends IMap {

	/**
	 * @return the current turn
	 */
	int getCurrentTurn();

	/**
	 * TODO: How does this interact with changesets? This is primarily used
	 * (should probably *only* be used) in serialization.
	 *
	 * @return the map this wraps
	 */
	IMap getMap();

}