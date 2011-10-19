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
	 * What kind of event this is.
	 */
	private final EventKind kind;

	/**
	 * 
	 * @return what kind of event this is
	 */
	public EventKind kind() {
		return kind;
	}

	/**
	 * This is just a superclass. Actual events must be a subclass.
	 * 
	 * @param eKind
	 *            What kind of event this is.
	 */
	protected AbstractEvent(final EventKind eKind) {
		kind = eKind;
	}

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
