package controller.map.simplexml.node;

import util.EqualsAny;
import util.Warning;
import model.map.PlayerCollection;
import model.map.events.AbstractTownEvent;
import model.map.events.CityEvent;
import model.map.events.FortificationEvent;
import model.map.events.TownEvent;
import model.map.events.TownSize;
import model.map.events.TownStatus;
import controller.map.SPFormatException;

/**
 * A Node that produces a TownEvent.
 * 
 * TODO: The AbstractTownEvent hierarchy should be dismantled, and this with it.
 * TODO: Towns (at least active ones) should have names.
 * 
 * @author Jonathan Lovelace
 */
public class TownEventNode extends AbstractFixtureNode<AbstractTownEvent> {
	/**
	 * Constructor.
	 */
	public TownEventNode() {
		super(AbstractTownEvent.class);
	}
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
	 * The property giving the town's name.
	 */
	private static final String NAME_PROPERTY = "name";

	/**
	 * @param players
	 *            the players in the map
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @return the TownEvent equivalent to this Node.
	 * @throws SPFormatException
	 *             if it includes malformed data
	 */
	@Override
	public AbstractTownEvent produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		// ESCA-JAVA0177:
		final AbstractTownEvent event; // NOPMD
		if ("city".equals(getProperty(KIND_PROPERTY))) {
			event = new CityEvent(
					TownStatus.parseTownStatus(getProperty(STATUS_PROP)),
					TownSize.parseTownSize(getProperty(SIZE_PROPERTY)),
					Integer.parseInt(getProperty(DC_PROPERTY)),
					hasProperty(NAME_PROPERTY) ? getProperty(NAME_PROPERTY)
							: "");
		} else if ("fortification".equals(getProperty(KIND_PROPERTY))) {
			event = new FortificationEvent(
					TownStatus.parseTownStatus(getProperty(STATUS_PROP)),
					TownSize.parseTownSize(getProperty(SIZE_PROPERTY)),
					Integer.parseInt(getProperty(DC_PROPERTY)),
					hasProperty(NAME_PROPERTY) ? getProperty(NAME_PROPERTY)
							: "");
		} else if ("town".equals(getProperty(KIND_PROPERTY))) {
			event = new TownEvent(
					TownStatus.parseTownStatus(getProperty(STATUS_PROP)),
					TownSize.parseTownSize(getProperty(SIZE_PROPERTY)),
					Integer.parseInt(getProperty(DC_PROPERTY)),
					hasProperty(NAME_PROPERTY) ? getProperty(NAME_PROPERTY)
							: "");
		} else {
			throw new SPFormatException("Unknown kind of event", getLine());
		}
		return event;
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, STATUS_PROP, SIZE_PROPERTY, DC_PROPERTY, NAME_PROPERTY);
	}
	
	/**
	 * Check the data for validity. A Town or similar is valid if it has no
	 * children and "kind", "dc", "size', and "status" properties.
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             if the data are invalid.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (hasProperty(DC_PROPERTY)) {
			if (hasProperty(SIZE_PROPERTY) && hasProperty(STATUS_PROP)) {
				if (!hasProperty(NAME_PROPERTY)) {
					warner.warn(new SPFormatException("Town-related events should have \"name\" property", getLine()));
				}
			} else {
				throw new SPFormatException(
						"Town-related events must have \"size\" and \"status\" properties",
						getLine());
			}
		} else {
			throw new SPFormatException(
					"Event must have \"dc\" property", getLine());
		}
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TownEventNode";
	}
}
