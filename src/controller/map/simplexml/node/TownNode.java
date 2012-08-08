package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.events.TownEvent;
import model.map.events.TownSize;
import model.map.events.TownStatus;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node that produces a Town.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
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
	 * @param players the players in the map
	 * @param warner a Warning instance to use for warnings
	 * @return the TownEvent equivalent to this Node.
	 * @throws SPFormatException if it includes malformed data
	 */
	@Override
	public TownEvent produce(final PlayerCollection players,
			final Warning warner) throws SPFormatException {
		final TownEvent fix = new TownEvent(
				TownStatus.parseTownStatus(getProperty(STATUS_PROP)),
				TownSize.parseTownSize(getProperty(SIZE_PROPERTY)),
				Integer.parseInt(getProperty(DC_PROPERTY)),
				hasProperty(NAME_PROPERTY) ? getProperty(NAME_PROPERTY) : "",
				Integer.parseInt(getProperty(ID_PROPERTY)));
		if (hasProperty("file")) {
			fix.setFile(getProperty("file"));
		}
		return fix;
	}

	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, STATUS_PROP, SIZE_PROPERTY,
				DC_PROPERTY, NAME_PROPERTY, ID_PROPERTY);
	}

	/**
	 * Check the data for validity. A Town or similar is valid if it has no
	 * children and "dc", "size', and "status" properties.
	 *
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if the data are invalid.
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren(TAG);
		demandProperty(TAG, DC_PROPERTY, warner, false, false);
		demandProperty(TAG, SIZE_PROPERTY, warner, false, false);
		demandProperty(TAG, STATUS_PROP, warner, false, false);
		demandProperty(TAG, NAME_PROPERTY, warner, true, false);
		registerOrCreateID(TAG, idFactory, warner);
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
