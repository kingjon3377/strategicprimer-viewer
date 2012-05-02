package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.events.AbstractTownEvent;
import model.map.events.CityEvent;
import model.map.events.FortificationEvent;
import model.map.events.TownEvent;
import model.map.events.TownSize;
import model.map.events.TownStatus;
import util.EqualsAny;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnsupportedTagException;
import controller.map.misc.IDFactory;

/**
 * A Node that produces a TownEvent.
 * 
 * TODO: The AbstractTownEvent hierarchy should be dismantled, and this with it.
 * TODO: Towns (at least active ones) should have names.
 * 
 * @author Jonathan Lovelace
 */
@Deprecated
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
	 * The property giving the ID number of the event.
	 */
	private static final String ID_PROPERTY = "id";
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
							: "", Long.parseLong(getProperty(ID_PROPERTY)));
		} else if ("fortification".equals(getProperty(KIND_PROPERTY))) {
			event = new FortificationEvent(
					TownStatus.parseTownStatus(getProperty(STATUS_PROP)),
					TownSize.parseTownSize(getProperty(SIZE_PROPERTY)),
					Integer.parseInt(getProperty(DC_PROPERTY)),
					hasProperty(NAME_PROPERTY) ? getProperty(NAME_PROPERTY)
							: "", Long.parseLong(getProperty(ID_PROPERTY)));
		} else if ("town".equals(getProperty(KIND_PROPERTY))) {
			event = new TownEvent(
					TownStatus.parseTownStatus(getProperty(STATUS_PROP)),
					TownSize.parseTownSize(getProperty(SIZE_PROPERTY)),
					Integer.parseInt(getProperty(DC_PROPERTY)),
					hasProperty(NAME_PROPERTY) ? getProperty(NAME_PROPERTY)
							: "", Long.parseLong(getProperty(ID_PROPERTY)));
		} else {
			throw new UnsupportedTagException(getProperty(KIND_PROPERTY), getLine());
		}
		return event;
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, STATUS_PROP, SIZE_PROPERTY, DC_PROPERTY, NAME_PROPERTY, ID_PROPERTY);
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
			if (hasProperty(SIZE_PROPERTY)) {
				if (hasProperty(STATUS_PROP)) {
					if (!hasProperty(NAME_PROPERTY)) {
						warner.warn(new MissingParameterException(
								getProperty(KIND_PROPERTY), "name", getLine()));
					}
					if (hasProperty(ID_PROPERTY)) {
						IDFactory.FACTORY.register(
								Long.parseLong(getProperty(ID_PROPERTY)));
					} else {
						warner.warn(new MissingParameterException(
								getProperty(KIND_PROPERTY), "id", getLine()));
						addProperty(ID_PROPERTY,
								Long.toString(IDFactory.FACTORY.getID()),
								warner);
					}
				} else {
					throw new MissingParameterException(
							getProperty(KIND_PROPERTY), STATUS_PROP, getLine());
				}
			} else {
				throw new MissingParameterException(getProperty(KIND_PROPERTY),
						SIZE_PROPERTY, getLine());
			}
		} else {
			throw new MissingParameterException(getProperty(KIND_PROPERTY),
					DC_PROPERTY, getLine());
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
