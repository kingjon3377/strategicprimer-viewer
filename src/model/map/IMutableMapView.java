package model.map;
/**
 * An interface for mutable map-views.
 * @author Jonathan Lovelace
 * @deprecated the old API is deprecated in this branch
 */
@Deprecated
public interface IMutableMapView extends IMutableMap, IMapView {

	/**
	 * Set the current player.
	 *
	 * @param current the new current player (number)
	 */
	void setCurrentPlayer(int current);

	/**
	 * Set the current turn.
	 *
	 * @param current the new current turn
	 */
	void setCurrentTurn(int current);

}