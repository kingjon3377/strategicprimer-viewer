package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Animal;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;

/**
 * A Node to represent a(n) (group of) animal(s).
 * 
 * @author Jonathan Lovelace
 * 
 */
public class AnimalNode extends AbstractFixtureNode<Animal> {
	/**
	 * Constructor.
	 */
	public AnimalNode() {
		super(Animal.class);
	}
	/**
	 * @param players
	 *            ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the animal this represents
	 * @throws SPFormatException
	 *             if it's missing a required attribute
	 */
	@Override
	public Animal produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		return new Animal(getProperty("kind"), hasProperty("traces"),
				hasProperty("talking")
						&& Boolean.parseBoolean(getProperty("talking")));
	}

	/**
	 * Check the node for invalid data. An Animal is valid if it has no children
	 * and has a "kind" property.
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             if the node contains invalid data.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Animal shouldn't have children",
					getLine());
		} else if (!hasProperty("kind")) {
			throw new SPFormatException("Animal must have \"kind\" property",
					getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, "kind", "traces", "talking");
	}
	/**
	 * @return a String representation of the node.
	 */
	@Override
	public String toString() {
		return "AnimalNode";
	}
}
