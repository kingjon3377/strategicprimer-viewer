package controller.map.readerng;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.Warning;

import controller.map.DeprecatedPropertyException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;

/**
 * A class for helper methods.
 * @author Jonathan Lovelace
 *
 */
public final class XMLHelper {
	/**
	 * Do not instantiate.
	 */
	private XMLHelper() {
		// Don't instantiate
	}
	/**
	 * @param startElement
	 *            a tag
	 * @param attribute
	 *            the attribute we want
	 * 
	 * @return the value of that attribute.
	 * @throws SPFormatException if the element doesn't have that attribute
	 */
	public static String getAttribute(final StartElement startElement,
			final String attribute) throws SPFormatException {
		final Attribute attr = startElement.getAttributeByName(new QName(
				attribute));
		if (attr == null) {
			throw new MissingParameterException(startElement.getName()
					.getLocalPart(), attribute, startElement.getLocation()
					.getLineNumber());
		}
		return attr.getValue();
	}
	/**
	 * Error message for unexpected tag.
	 */
	private static final String UNEXPECTED_TAG = "Unexpected tag ";

	/**
	 * @param elem
	 *            the element
	 * @param attr
	 *            the attribute we want
	 * @param defaultValue
	 *            the default value if the element doesn't have the attribute
	 * 
	 * @return the value of attribute if it exists, or the default
	 */
	public static String getAttributeWithDefault(final StartElement elem,
			final String attr, final String defaultValue) {
		final Attribute value = elem.getAttributeByName(new QName(attr));
		return (value == null) ? defaultValue : value.getValue();
	}
	
	/**
	 * @param element
	 *            the element
	 * @param preferred
	 *            the preferred name of the attribute
	 * @param deprecated
	 *            the deprecated name of the attribute * @throws
	 *            SPFormatException if the element doesn't have that attribute
	 * @param warner the warning instance to use
	 * @return the value of the attribute, gotten from the preferred form if it
	 *         has it, and from the deprecated form if the preferred form isn't
	 *         there but it is.
	 * @throws SPFormatException
	 *             if the element doesn't have that attribute
	 */
	public static String getAttributeWithDeprecatedForm(
			final StartElement element, final String preferred,
			final String deprecated, final Warning warner) throws SPFormatException {
		final Attribute prefAttr = element.getAttributeByName(new QName(preferred));
		final Attribute deprAttr = element.getAttributeByName(new QName(deprecated));
		if (prefAttr == null && deprAttr == null) {
			throw new MissingParameterException(element.getName().getLocalPart(), preferred, element.getLocation().getLineNumber());
		} else if (prefAttr == null) {
			warner.warn(new DeprecatedPropertyException(element.getName()
					.getLocalPart(), deprecated, preferred, element
					.getLocation().getLineNumber()));
			return deprAttr.getValue(); // NOPMD
		} else {
			return prefAttr.getValue();
		}
	}
	/**
	 * @param elem an element
	 * @param attr the attribute we want
	 * @return whether the element has that attribute
	 */
	public static boolean hasAttribute(final StartElement elem, final String attr) {
		return !(elem.getAttributeByName(new QName(attr)) == null); 
	}
	/**
	 * @param elem an element
	 * @param attrs a list of attributes
	 * @return false if at least one of them is missing, true otherwise
	 */
	public static boolean hasAttributes(final StartElement elem, final String... attrs) {
		for (String attr : attrs) {
			if (!hasAttribute(elem, attr)) {
				return false; // NOPMD
			}
		}
		return true;
	}

	/**
	 * Move along the stream until we hit an end element, but object to any
	 * start elements.
	 * 
	 * @param tag
	 *            what kind of tag we're in (for the error message)
	 * @param reader
	 *            the XML stream we're reading from
	 */
	public static void spinUntilEnd(final String tag, final Iterable<XMLEvent> reader) {
		for (final XMLEvent event : reader) {
			if (event.isStartElement()) {
				throw new IllegalStateException(UNEXPECTED_TAG
						+ event.asStartElement().getName().getLocalPart()
						+ ": " + tag + " can't contain anything yet");
			} else if (event.isEndElement()) {
				break;
			}
		}
	}
	/**
	 * Throw an exception (or warn) if the specified parameter is missing or empty.
	 * @param element the current element
	 * @param parameter the parameter to check
	 * @param mandatory whether we should throw or just warn
	 * @param warner the Warning instance to use if not mandatory 
	 * @throws SPFormatException if the parameter is mandatory and missing
	 */
	public static void requireNonEmptyParameter(final StartElement element,
			final String parameter, final boolean mandatory,
			final Warning warner) throws SPFormatException {
		if ("".equals(getAttributeWithDefault(element, parameter, ""))) {
			final SPFormatException except = new MissingParameterException(
					element.getName().getLocalPart(), parameter, element
							.getLocation().getLineNumber());
			if (mandatory) {
				throw except;
			} else {
				warner.warn(except);
			}
		}
	}
}
