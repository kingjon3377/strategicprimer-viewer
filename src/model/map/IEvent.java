package model.map;

/**
 * An "event" --- something, other than a fortress, unit, or river, that can be
 * on a tile. Intended to replace legacy/numeric "events."
 *
 * @author Jonathan Lovelace
 *
 */
public interface IEvent extends TileFixture {
	/**
	 *
	 * @return the DC to discover the event.
	 */
	int getDC();

	/**
	 *
	 * @return exploration-result text for the event.
	 */
	String getText();
}
