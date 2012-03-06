package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Ogre;
import controller.map.SPFormatException;

/**
 * A Node to represent an ogre.
 * @author Jonathan Lovelace
 *
 */
public class OgreNode extends AbstractFixtureNode<Ogre> {
	/**
	 * Constructor.
	 */
	public OgreNode() {
		super(Ogre.class);
	}
	/**
	 * @param players ignored
	 * @return the ogre this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Ogre produce(final PlayerCollection players) throws SPFormatException {
		return new Ogre();
	}
	/**
	 * Check the node for invalid data. An Ogre is valid if it has no children.
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Ogre shouldn't have children", getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return false;
	}
	/**
	 * @return a String representation of the node.
	 */
	@Override
	public String toString() {
		return "OgreNode";
	}
}
