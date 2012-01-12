package controller.map.simplexml.node;

import java.util.HashMap;
import java.util.Map;

import model.map.PlayerCollection;
import util.Warning;
import controller.map.SPFormatException;

/**
 * A class representing an XML tag and its descendants.
 * 
 * @param <T>
 *            the business-logic type this will eventually get turned into.
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
	 * 
	 * @param property
	 *            the property to add.
	 * @param value
	 *            its value
	 */
	public final void addProperty(final String property, final String value) {
		if ("line".equals(property)) {
			setLine(Integer.parseInt(value));
		} else if (canUse(property)) {
			properties.put(property, value);
		} else {
			Warning.warn(new SPFormatException("Don't know how to use property " + property, getLine()));
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
	 * Convert the Node to its equivalent business-logic type.
	 * 
	 * @param players
	 *            the players in the map. May be null for Nodes that don't use
	 *            it.
	 * @return the business-logic object represented by the node.
	 * @throws SPFormatException
	 *             if the data isn't legal.
	 */
	public abstract T produce(final PlayerCollection players)
			throws SPFormatException;

	/**
	 * Move everything---properties and children---to another Node.
	 * 
	 * @param dest
	 *            the destination node.
	 */
	protected final void moveEverythingTo(
			final AbstractChildNode<? extends T> dest) {
		moveChildrenTo(dest);
		for (String property : properties.keySet()) {
			dest.addProperty(property, properties.get(property));
		}
		properties.clear();
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	public abstract boolean canUse(final String property);
//	/**
//	 * @param obj an object
//	 * @return whether it's equal to this one
//	 */
//	@Override
//	public boolean equals(final Object obj) {
//		return super.equals(obj)
//				&& obj instanceof AbstractChildNode<?>
//				&& Arrays.equals(getClass().getTypeParameters(),
//						((AbstractChildNode<?>) obj).getClass()
//								.getTypeParameters()) && properties
//						.equals(((AbstractChildNode<?>) obj).properties);
//	}
//	/**
//	 * @return a hash value for this object
//	 */
//	@Override
//	public int hashCode() {
//		return super.hashCode() | Arrays.hashCode(getClass().getTypeParameters())
//				| properties.hashCode();
//	}
}
