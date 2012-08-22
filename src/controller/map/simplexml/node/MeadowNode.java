package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.resources.FieldStatus;
import model.map.fixtures.resources.Meadow;
import util.EqualsAny;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to produce a Meadow.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
 *         FIXME: Groves have "wild", meadows have "cultivated".
 *
 */
@Deprecated
public class MeadowNode extends AbstractFixtureNode<Meadow> {
	/**
	 * The name of the property giving the status of the field.
	 */
	private static final String STATUS_PROP = "status";
	/**
	 * The name of the property saying whether or not the field or meadow is
	 * cultivated.
	 */
	private static final String CULTIVATED_PARAM = "cultivated";
	/**
	 * The name of the (factory-generated) property saying whether this is a
	 * meadow or a field.
	 */
	private static final String TAG_PROPERTY = "tag";
	/**
	 * The name of the property saying what kind of meadow or field it is.
	 */
	private static final String KIND_PROPERTY = "kind";

	/**
	 * Constructor.
	 */
	public MeadowNode() {
		super(Meadow.class);
	}

	/**
	 * Produce the Node.
	 *
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the Meadow this represents
	 * @throws SPFormatException if a required attribute is missing.
	 */
	@Override
	public Meadow produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final Meadow fix = new Meadow(getProperty(KIND_PROPERTY),
				"field".equals(getProperty(TAG_PROPERTY)),
				Boolean.parseBoolean(getProperty(CULTIVATED_PARAM)),
				Integer.parseInt(getProperty("id")),
				FieldStatus.parse(getProperty(STATUS_PROP)),
				getProperty("file"));
		return fix;
	}

	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, TAG_PROPERTY,
				CULTIVATED_PARAM, STATUS_PROP, "id");
	}

	/**
	 * Check whether the Node's data is valid. A Meadow is valid if it has no
	 * children and "tag" (should be generated by the factory, not in the XML),
	 * "kind", and "cultivated" properties.
	 *
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException on invalid data
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		try {
			demandProperty("meadow or field", TAG_PROPERTY, warner, false,
					false);
		} catch (final MissingParameterException except) {
			// The 'tag' property is supposed to be added by the NodeFactory; if
			// it isn't present, something is *very* wrong.
			throw new IllegalStateException(except);
		}
		forbidChildren(getProperty(TAG_PROPERTY));
		demandProperty(getProperty(TAG_PROPERTY), CULTIVATED_PARAM, warner,
				false, false);
		demandProperty(getProperty(TAG_PROPERTY), KIND_PROPERTY, warner, false,
				false);
		registerOrCreateID(getProperty(TAG_PROPERTY), idFactory, warner);
		try {
			demandProperty(getProperty(TAG_PROPERTY), STATUS_PROP, warner,
					false, false);
		} catch (final MissingParameterException except) {
			warner.warn(except);
			addProperty(STATUS_PROP,
					FieldStatus.random(Integer.parseInt(getProperty("id")))
							.toString(), warner);
		}
	}

	/**
	 * @return a String representation of the Node
	 */
	@Override
	public String toString() {
		return "MeadowNode";
	}
}
