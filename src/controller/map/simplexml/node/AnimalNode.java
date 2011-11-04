package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Animal;
import controller.map.SPFormatException;

/**
 * A Node to represent a(n) (group of) animal(s).
 * 
 * @author Jonathan Lovelace
 * 
 */
public class AnimalNode extends AbstractChildNode<Animal> {
	/**
	 * @param players
	 *            ignored
	 * @return the animal this represents
	 * @throws SPFormatException
	 *             if it's missing a required attribute
	 */
	@Override
	public Animal produce(final PlayerCollection players)
			throws SPFormatException {
		return new Animal(getProperty("kind"));
	}

	/**
	 * Check the node for invalid data. An Animal is valid if it has no children
	 * and has a "kind" property.
	 * 
	 * @throws SPFormatException
	 *             if the node contains invalid data.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Animal shouldn't have children",
					getLine());
		} else if (!hasProperty("kind")) {
			throw new SPFormatException("Animal must have \"kind\" property",
					getLine());
		}
	}

}
