package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Ogre;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to represent an ogre.
 * @author Jonathan Lovelace
 *
 */
@Deprecated
public class OgreNode extends AbstractFixtureNode<Ogre> {
	/**
	 * Constructor.
	 */
	public OgreNode() {
		super(Ogre.class);
	}
	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the ogre this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Ogre produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new Ogre(Integer.parseInt(getProperty("id")), getProperty("file"));
	}
	/**
	 * Check the node for invalid data. An Ogre is valid if it has no children.
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren("ogre");
		registerOrCreateID("ogre", idFactory, warner);
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return "id".equals(property);
	}
	/**
	 * @return a String representation of the node.
	 */
	@Override
	public String toString() {
		return "OgreNode";
	}
}
