package model.map.fixtures.towns;

import model.map.TileFixture;

/**
 * An interface for towns and similar fixtures. Needed because we don't want
 * fortresses and villages to be Events.
 *
 * @author Jonathan Lovelace
 *
 */
public interface TownFixture extends TileFixture {

	/**
	 * @return the name of the town, fortress, or city.
	 */
	String name();

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

}
