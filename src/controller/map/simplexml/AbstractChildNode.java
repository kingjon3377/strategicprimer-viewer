package controller.map.simplexml;

import java.util.HashMap;
import java.util.Map;

/**
 * A class representing an XML tag and its descendants.
 * @param <T> the business-logic type this will eventually get turned into.
 * @author Jonathan Lovelace
 *
 */
public abstract class AbstractChildNode<T> extends AbstractXMLNode {
	/**
	 * The properties on this node.
	 */
	private final Map<String, String> properties = new HashMap<String, String>();
	/**
	 * Add a property.
	 * @param property the property to add.
	 * @param value its value
	 */
	public final void addProperty(final String property, final String value) {
		properties.put(property, value);
	}
	/**
	 * @param property a property
	 * @return whether the node contains it
	 */
	public final boolean hasProperty(final String property) {
		return properties.containsKey(property);
	}
	/**
	 * @param property a property
	 * @return its value
	 */
	public final String getProperty(final String property) {
		return properties.get(property);
	}
	/**
	 * Convert the Node to its equivalent business-logic type.
	 * @return the business-logic object represented by the node.
	 * @throws SPFormatException if the data isn't legal.
	 */
	public abstract T produce() throws SPFormatException;
}
