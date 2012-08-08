package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.River;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A node representing a river XML tag.
 * 
 * @author Jonathan Lovelace
 * 
 */
@Deprecated
public class RiverNode extends AbstractChildNode<River> {
	/**
	 * The name of the property giving the direction of the river.
	 */
	private static final String DIRECTION_PARAM = "direction";

	/**
	 * Constructor.
	 */
	public RiverNode() {
		super(River.class);
	}

	/**
	 * We don't *promise* to throw an exception here if there are unexpected
	 * children---still need to call checkNode().
	 * 
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return a River equivalent to this.
	 * @throws SPFormatException if this has unexpected children or doesn't have
	 *         needed data
	 */
	@Override
	public River produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final River fix = River.getRiver(getProperty(DIRECTION_PARAM));
		if (hasProperty("file")) {
			fix.setFile(getProperty("file"));
		}
		return fix;
	}

	/**
	 * Check the validity of the node. (TODO: eventually we may want to allow
	 * units or even fortresses, etc., in rivers.) A river is valid iff it has
	 * no children and has a direction.
	 * 
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if the River contains anythig.
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren("river");
		demandProperty("river", DIRECTION_PARAM, warner, false, false);
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "RiverNode";
	}

	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return DIRECTION_PARAM.equals(property);
	}

}
