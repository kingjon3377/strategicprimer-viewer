package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.terrain.Forest;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node that will produce a Forest.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
 */
@Deprecated
public class ForestNode extends AbstractFixtureNode<Forest> {
	/**
	 * The name of the property saying what kind of trees.
	 */
	private static final String KIND_PROPERTY = "kind";

	/**
	 * Constructor.
	 */
	public ForestNode() {
		super(Forest.class);
	}

	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the Forest this node represents.
	 * @throws SPFormatException if the forest doesn't have a "kind" property.
	 */
	@Override
	public Forest produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		return new Forest(getProperty(KIND_PROPERTY), hasProperty("rows"),
				getProperty("file"));
	}

	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, "rows");
	}

	/**
	 * Check whether the node is valid. A forest is valid if it has a "kind"
	 * property and no children. TODO: add further properties.
	 *
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if any required properties are missing.
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren("forest");
		demandProperty("forest", KIND_PROPERTY, warner, false, false);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ForestNode";
	}
}
