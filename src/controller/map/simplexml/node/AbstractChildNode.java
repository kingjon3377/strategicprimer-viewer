package controller.map.simplexml.node;

import java.util.HashMap;
import java.util.Map;

import model.map.PlayerCollection;
import util.Warning;
import controller.map.DeprecatedPropertyException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnsupportedPropertyException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

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
		} else if ("file".equals(property) || canUse(property)) {
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
	
	/**
	 * A helper method to register the ID if present or generate a new one (and
	 * save it in the appropriate parameter) if not.
	 * 
	 * @param tag
	 *            the current tag
	 * @param idFactory
	 *            the factory to register the ID with or generate a new one
	 *            from.
	 * @param warner
	 *            the Waring instance to use if necessary.
	 */
	protected void registerOrCreateID(final String tag, final IDFactory idFactory, final Warning warner) {
		if (hasProperty("id")) {
			idFactory.register(Integer.parseInt(getProperty("id")));
		} else {
			warner.warn(new MissingParameterException(tag, "id", getLine()));
			addProperty("id", Integer.toString(idFactory.createID()), warner);
		}
	}
	/**
	 * Object (warn or throw an exception) if the specified property is missing.
	 * @param tag the current tag
	 * @param property the property to look for
	 * @param warner the Warning instance to use if necessary
	 * @param warning whether to just warn (as opposed to aborting)
	 * @param emptyOK whether an empty value is OK (if not, they count as missing)
	 * @throws SPFormatException if the property is missing and warning is false
	 */
	protected void demandProperty(final String tag, final String property,
			final Warning warner, final boolean warning, final boolean emptyOK)
			throws SPFormatException {
		if (!hasProperty(property) || ((!emptyOK) && getProperty(property).isEmpty())) {
			final SPFormatException except = new MissingParameterException(tag, property, getLine());
			if (warning) {
				warner.warn(except);
			} else {
				throw except;
			}
		}
	}
	/**
	 * Handle deprecated name for a property.
	 * @param tag the current tag
	 * @param preferred the preferred name for the tag
	 * @param deprecated the deprecated name for the tag
	 * @param warner the Warning instance to use
	 * @param required whether the node *must* have one of the properties
	 * @param emptyValid whether to treat empty values as valid values
	 * @throws SPFormatException if the property is required but absent
	 */
	protected void handleDeprecatedProperty(final String tag,
			final String preferred, final String deprecated,
			final Warning warner, final boolean required,
			final boolean emptyValid) throws SPFormatException {
		if (!hasProperty(preferred) || (!emptyValid && getProperty(preferred).isEmpty())) {
			if (hasProperty(deprecated) && (emptyValid || !getProperty(deprecated).isEmpty())) {
				warner.warn(new DeprecatedPropertyException(tag, deprecated, preferred, getLine()));
				addProperty(preferred, getProperty(deprecated), warner);
			} else {
				final SPFormatException except = new MissingParameterException(tag, preferred, getLine());
				if (required) {
					throw except;
				} else {
					warner.warn(except);
				}
			}
		} 
	}
}
