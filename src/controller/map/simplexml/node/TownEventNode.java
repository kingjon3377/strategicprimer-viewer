package controller.map.simplexml.node;

import model.viewer.PlayerCollection;
import model.viewer.events.AbstractEvent.TownSize;
import model.viewer.events.AbstractEvent.TownStatus;
import model.viewer.events.AbstractTownEvent;
import model.viewer.events.CityEvent;
import model.viewer.events.FortificationEvent;
import model.viewer.events.TownEvent;
import controller.map.simplexml.AbstractChildNode;
import controller.map.simplexml.SPFormatException;

/**
 * A Node that produces a TownEvent.
 * 
 * @author Jonathan Lovelace
 */
public class TownEventNode extends AbstractChildNode<AbstractTownEvent> {
	/**
	 * The property of a town-like event saying how big it is.
	 */
	private static final String SIZE_PROPERTY = "size";
	/**
	 * The property of a town-like event saying what its status is.
	 */
	private static final String STATUS_PROP = "status";
	/**
	 * The property of an event saying how difficult it is to find it.
	 */
	private static final String DC_PROPERTY = "dc";
	/**
	 * The property of an Event saying what kind of event it is.
	 */
	private static final String KIND_PROPERTY = "kind";

	/**
	 * @param players
	 *            the players in the map
	 * @return the TownEvent equivalent to this Node.
	 * @throws SPFormatException
	 *             if it includes malformed data
	 */
	@Override
	public AbstractTownEvent produce(final PlayerCollection players)
			throws SPFormatException {
		// ESCA-JAVA0177:
		final AbstractTownEvent event; // NOPMD
		if ("city".equals(getProperty(KIND_PROPERTY))) {
			event = new CityEvent(
					TownStatus.parseTownStatus(getProperty(STATUS_PROP)),
					TownSize.parseTownSize(getProperty(SIZE_PROPERTY)),
					Integer.parseInt(getProperty(DC_PROPERTY)));
		} else if ("fortification".equals(getProperty(KIND_PROPERTY))) {
			event = new FortificationEvent(
					TownStatus.parseTownStatus(getProperty(STATUS_PROP)),
					TownSize.parseTownSize(getProperty(SIZE_PROPERTY)),
					Integer.parseInt(getProperty(DC_PROPERTY)));
		} else if ("town".equals(getProperty(KIND_PROPERTY))) {
			event = new TownEvent(
					TownStatus.parseTownStatus(getProperty(STATUS_PROP)),
					TownSize.parseTownSize(getProperty(SIZE_PROPERTY)),
					Integer.parseInt(getProperty(DC_PROPERTY)));
		} else {
			throw new SPFormatException("Unknown kind of event", getLine());
		}
		return event;
	}
	
	/**
	 * Check the data for validity. A Town or similar is valid if it has no
	 * children and "kind", "dc", "size', and "status" properties.
	 * 
	 * @throws SPFormatException if the data are invalid.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (hasProperty(KIND_PROPERTY) && hasProperty(DC_PROPERTY)) {
			if (!hasProperty(SIZE_PROPERTY) || !hasProperty(STATUS_PROP)) {
				throw new SPFormatException(
						"Town-related events must have \"size\" and \"status\" properties",
						getLine());
			}
		} else {
			throw new SPFormatException("Event must have \"kind\" and \"dc\" properties", getLine());
		}
	}

}
