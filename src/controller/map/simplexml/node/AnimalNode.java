package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Animal;
import util.EqualsAny;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A Node to represent a(n) (group of) animal(s).
 * 
 * @author Jonathan Lovelace
 * 
 */
@Deprecated
public class AnimalNode extends AbstractFixtureNode<Animal> {
	/**
	 * The name of the property saying what kind of animal this is.
	 */
	private static final String KIND_PROPERTY = "kind";
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
		return new Animal(getProperty(KIND_PROPERTY), hasProperty("traces"),
				hasProperty("talking")
						&& Boolean.parseBoolean(getProperty("talking")),
				Long.parseLong(getProperty("id")));
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
			throw new UnwantedChildException("animal", iterator().next().toString(),
					getLine());
		} else if (hasProperty(KIND_PROPERTY)) {
			if (hasProperty("id")) {
				IDFactory.FACTORY.register(Long.parseLong(getProperty("id")));
			} else {
				warner.warn(new MissingParameterException("animal", "id", getLine()));
				addProperty("id", Long.toString(IDFactory.FACTORY.getID()), warner);
			}
		} else {
			throw new MissingParameterException("animal", KIND_PROPERTY,
					getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, "traces", "talking", "id");
	}
	/**
	 * @return a String representation of the node.
	 */
	@Override
	public String toString() {
		return "AnimalNode";
	}
}
