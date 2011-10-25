package model.map.events;

import model.map.TileFixture;

/**
 * An "event" --- something, other than a fortress, unit, or river, that can be
 * on a tile. Intended to replace legacy/numeric "events."
 * 
 * @author Jonathan Lovelace
 * 
 */
public abstract class AbstractEvent implements TileFixture {
	/**
	 * 
	 * @return the DC to discover the event.
	 */
	public abstract int getDC();

	/**
	 * 
	 * @return exploration-result text for the event.
	 */
	public abstract String getText();
}
