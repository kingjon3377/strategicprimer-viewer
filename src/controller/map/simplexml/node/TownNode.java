package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.events.TownEvent;
import model.map.events.TownSize;
import model.map.events.TownStatus;
import util.EqualsAny;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node that produces a Town.
 * 
 * @author Jonathan Lovelace
 */
@Deprecated
public class TownNode extends AbstractFixtureNode<TownEvent> {
	/**
	 * The tag.
	 */
	private static final String TAG = "town";
	/**
	 * Constructor.
	 */
	public TownNode() {
		super(TownEvent.class);
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
	public TownEvent produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		return new TownEvent(
				TownStatus.parseTownStatus(getProperty(STATUS_PROP)),
				TownSize.parseTownSize(getProperty(SIZE_PROPERTY)),
				Integer.parseInt(getProperty(DC_PROPERTY)),
				hasProperty(NAME_PROPERTY) ? getProperty(NAME_PROPERTY) : "",
				Long.parseLong(getProperty(ID_PROPERTY)));
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, STATUS_PROP, SIZE_PROPERTY, DC_PROPERTY, NAME_PROPERTY, ID_PROPERTY);
	}
	
	/**
	 * Check the data for validity. A Town or similar is valid if it has no
	 * children and "dc", "size', and "status" properties.
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @throws SPFormatException
	 *             if the data are invalid.
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren(TAG);
		if (hasProperty(DC_PROPERTY)) {
			if (hasProperty(SIZE_PROPERTY)) {
				if (hasProperty(STATUS_PROP)) {
					if (!hasProperty(NAME_PROPERTY)) {
						warner.warn(new MissingParameterException(
								TAG, "name", getLine()));
					}
					if (hasProperty(ID_PROPERTY)) {
						idFactory.register(
								Long.parseLong(getProperty(ID_PROPERTY)));
					} else {
						warner.warn(new MissingParameterException(
								TAG, "id", getLine()));
						addProperty(ID_PROPERTY,
								Long.toString(idFactory.getID()),
								warner);
					}
				} else {
					throw new MissingParameterException(
							TAG, STATUS_PROP, getLine());
				}
			} else {
				throw new MissingParameterException(TAG,
						SIZE_PROPERTY, getLine());
			}
		} else {
			throw new MissingParameterException(TAG,
					DC_PROPERTY, getLine());
		}
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TownNode";
	}
}
