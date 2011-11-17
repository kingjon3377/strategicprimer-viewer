package controller.map.simplexml.node;

import util.EqualsAny;
import model.map.PlayerCollection;
import model.map.fixtures.Meadow;
import controller.map.SPFormatException;
/**
 * A Node to produce a Meadow.
 * @author Jonathan Lovelace
 *
 */
public class MeadowNode extends AbstractFixtureNode<Meadow> {
	/**
	 * Produce the Node.
	 * @param players ignored
	 * @return the Meadow this represents
	 * @throws SPFormatException if a required attribute is missing.
	 */
	@Override
	public Meadow produce(final PlayerCollection players) throws SPFormatException {
		return new Meadow(getProperty("kind"),
				"field".equals(getProperty("tag")),
				Boolean.parseBoolean(getProperty("cultivated")));
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, "kind", "tag", "cultivated");
	}
	/**
	 * Check whether the Node's data is valid. A Meadow is valid if it has no
	 * children and "tag" (should be generated by the factory, not in the XML),
	 * "kind", and "cultivated" properties.
	 * 
	 * @throws SPFormatException
	 *             on invalid data
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Fields and meadows shouldn't have children", getLine());
		} else if (hasProperty("tag")) {
			if (!hasProperty("cultivated") || !hasProperty("kind")) {
				throw new SPFormatException(
						"Fields and meadows must have \"cultivated\" and \"kind\" properties",
						getLine());
			}
		} else {
			throw new SPFormatException(
					"The Nodeactory should have generated a \"tag\" property to tell whether this is a field or a meadow",
					getLine());
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
