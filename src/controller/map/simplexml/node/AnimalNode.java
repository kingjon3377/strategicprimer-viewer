package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Animal;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to represent a(n) (group of) animal(s).
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
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
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the animal this represents
	 * @throws SPFormatException if it's missing a required attribute
	 */
	@Override
	public Animal produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final Animal fix = new Animal(getProperty(KIND_PROPERTY),
				hasProperty("traces"), hasProperty("talking")
						&& Boolean.parseBoolean(getProperty("talking")),
				Integer.parseInt(getProperty("id")), getProperty("file"));
		return fix;
	}

	/**
	 * Check the node for invalid data. An Animal is valid if it has no children
	 * and has a "kind" property.
	 *
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if the node contains invalid data.
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren("animal");
		demandProperty("animal", KIND_PROPERTY, warner, false, false);
		registerOrCreateID("animal", idFactory, warner);
	}

	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, "traces",
				"talking", "id");
	}

	/**
	 * @return a String representation of the node.
	 */
	@Override
	public String toString() {
		return "AnimalNode";
	}
}
