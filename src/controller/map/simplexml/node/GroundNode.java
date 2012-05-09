package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Ground;
import util.EqualsAny;
import util.Warning;
import controller.map.DeprecatedPropertyException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;
/**
 * A Node to produce a Ground fixture.
 * @author Jonathan Lovelace
 *
 */
@Deprecated
public class GroundNode extends AbstractFixtureNode<Ground> {
	/**
	 * The tag.
	 */
	private static final String TAG = "ground";
	/**
	 * The old, deprecated name of the property telling what kind of ground.
	 */
	private static final String OLD_KIND_PARAM = "ground";
	/**
	 * The name of the property that tells whether the ground is exposed or not.
	 */
	private static final String EXPOSED_PARAM = "exposed";
	/**
	 * The name of the property for what kind of ground.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Constructor.
	 */
	public GroundNode() {
		super(Ground.class);
	}
	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the Ground this node represents.
	 * @throws SPFormatException if the element was missing any required properties
	 */
	@Override
	public Ground produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new Ground(getProperty(KIND_PROPERTY), Boolean.parseBoolean(getProperty(EXPOSED_PARAM)));
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, EXPOSED_PARAM, OLD_KIND_PARAM);
	}
	/**
	 * Check whether the node is valid. A Ground is valid if it has "ground" and "exposed"
	 * properties and no children. TODO: add further properties.
	 * 
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @throws SPFormatException
	 *             if any required properties are missing.
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren(TAG);
		if (hasProperty(KIND_PROPERTY)) {
			demandProperty(TAG, EXPOSED_PARAM, warner, false, false);
		} else {
			if (hasProperty("ground")) {
				warner.warn(new DeprecatedPropertyException(TAG, OLD_KIND_PARAM,
						KIND_PROPERTY, getLine()));
				addProperty(KIND_PROPERTY, getProperty(OLD_KIND_PARAM), warner);
			} else {
				throw new MissingParameterException(TAG, KIND_PROPERTY,
						getLine());
			}
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "GroundNode";
	}
}
