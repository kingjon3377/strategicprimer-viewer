package model.map.events;

import model.map.HasImage;


/**
 * A deposit (always exposed for now) of stone.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class StoneEvent implements IEvent, HasImage {
	/**
	 * Constructor.
	 * 
	 * @param skind
	 *            the kind of stone
	 * @param discdc
	 *            the dc to discover the stone.
	 */
	public StoneEvent(final StoneKind skind, final int discdc) {
		super();
		stone = skind;
		dc = discdc;
	}

	/**
	 * What kind of stone this deposit is.
	 */
	private final StoneKind stone;

	/**
	 * 
	 * @return what kind of stone this deposit is.
	 */
	public StoneKind stone() {
		return stone;
	}

	/**
	 * The DC to discover the stone. TODO: Should perhaps be mutable.
	 */
	private final int dc; // NOPMD
	/**
	 * 
	 * @return the DC to discover the event.
	 */
	@Override
	public int getDC() {
		return dc;
	}

	/**
	 * 
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		final StringBuilder build = new StringBuilder("There is an exposed ");
		build.append(stone.toString());
		build.append(" deposit here.");
		return build.toString();
	}

	/**
	 * @param obj
	 *            an object
	 * 
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof StoneEvent
						&& ((StoneEvent) obj).stone.equals(stone) && ((StoneEvent) obj).dc == dc);
	}

	/**
	 * 
	 * @return a hash value for the event
	 */
	@Override
	public int hashCode() {
		return stone.hashCode() + dc << 3;
	}

	/**
	 * 
	 * @return a string representation of the object
	 */
	@Override
	public String toString() {
		return "A " + stone.toString() + " deposit, of DC " + dc;
	}
	/**
	 * @return an XML representation of the event
	 */
	@Override
	public String toXML() {
		return new StringBuilder("<stone stone=\"").append(stone.toString())
				.append("\" dc=\"").append(dc).append("\" />").toString();
	}
	/**
	 * @return the name of an image to represent the event
	 */
	@Override
	public String getImage() {
		return "stone.png";
	}
}
