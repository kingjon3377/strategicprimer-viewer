package model.map.fixtures.towns;

import model.map.HasName;
import model.map.HasOwner;
import model.map.Player;
import model.map.TileFixture;

/**
 * An interface for towns and similar fixtures. Needed because we don't want
 * fortresses and villages to be Events.
 *
 * @author Jonathan Lovelace
 *
 */
public interface ITownFixture extends TileFixture, HasName, HasOwner {

	/**
	 * @return the name of the town, fortress, or city.
	 */
	@Override
	String getName();

	/**
	 *
	 * @return the status of the town, fortress, or city
	 */
	TownStatus status();

	/**
	 *
	 * @return the size of the town, fortress, or city
	 */
	TownSize size();

	/**
	 * @return the player that owns the town, fortress, or city
	 */
	@Override
	Player getOwner();

}
