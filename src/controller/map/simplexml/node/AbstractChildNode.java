package controller.map.simplexml.node;

import java.util.HashMap;
import java.util.Map;

import model.map.PlayerCollection;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnsupportedPropertyException;
import controller.map.UnwantedChildException;

/**
 * A class representing an XML tag and its descendants.
 * 
 * @param <T>
 *            the business-logic type this will eventually get turned into.
 * @author Jonathan Lovelace
 * 
 */
@Deprecated
public abstract class AbstractChildNode<T> extends AbstractXMLNode {
	/**
	 * Constructor.
	 * @param type the type of the object we'll produce.
	 */
	protected AbstractChildNode(final Class<T> type) {
		super();
		product = type;
	}
	/**
	 * The properties on this node.
	 */
	private final Map<String, String> properties = new HashMap<String, String>();

	/**
	 * Add a property.
	 * 
	 * @param property
	 *            the property to add.
	 * @param value
	 *            its value
	 * @param warner
	 *            the warning instance to use if this node doesn't know how to
	 *            use the property.
	 */
	public final void addProperty(final String property, final String value, final Warning warner) {
		if ("line".equals(property)) {
			setLine(Integer.parseInt(value));
		} else if (canUse(property)) {
			properties.put(property, value);
		} else {
			warner.warn(new UnsupportedPropertyException(toString(), property, getLine()));
		}
	}

	/**
	 * @param property
	 *            a property
	 * 
	 * @return whether the node contains it
	 */
	public final boolean hasProperty(final String property) {
		return properties.containsKey(property);
	}

	/**
	 * @param property
	 *            a property
	 * 
	 * @return its value
	 */
	public final String getProperty(final String property) {
		return properties.get(property);
	}
	/**
	 * @param property a property
	 * @param defaultValue what to return if we don't have that property
	 * @return the value of the property, or the default if we don't have it.
	 */
	protected final String getPropertyWithDefault(final String property, final String defaultValue) {
		return hasProperty(property) ? getProperty(property) : defaultValue;
	}
	/**
	 * Convert the Node to its equivalent business-logic type.
	 * 
	 * @param players
	 *            the players in the map. May be null for Nodes that don't use
	 *            it.
	 * @param warner a Warning instance to use for warnings
	 * @return the business-logic object represented by the node.
	 * @throws SPFormatException
	 *             if the data isn't legal.
	 */
	public abstract T produce(final PlayerCollection players, Warning warner)
			throws SPFormatException;

	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	public abstract boolean canUse(final String property);
	/**
	 * The type of child we produce, or a supertype thereof.
	 */
	private final Class<T> product;
	/**
	 * @return the type of child we produce, or a supertype thereof.
	 */
	public Class<T> getProduct() {
		return product;
	}
	/**
	 * A helper method that throws an exception if there are any child nodes.
	 * @param tag the current tag (for use in constructing the exception to throw)
	 * @throws SPFormatException if there is a child
	 */
	protected void forbidChildren(final String tag) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException(tag, iterator().next().toString(), getLine());
		}
	}
}
