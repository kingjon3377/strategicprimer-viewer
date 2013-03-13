package controller.map.cxml;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.EqualsAny;
import util.Warning;
import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;

/**
 * A superclass to provide helper methods.
 * @author Jonathan Lovelace
 *
 */
public abstract class AbstractCompactReader {
	/**
	 * Do not instantiate directly.
	 */
	protected AbstractCompactReader() {
		// Nothing to do
	}
	/**
	 * Require that an element be one of the specified tags.
	 * @param element the element to check
	 * @param tags the tags we accept here
	 */
	protected void requireTag(final StartElement element, final String... tags) {
		if (!(EqualsAny.equalsAny(element.getName().getLocalPart(), tags))) {
			final StringBuilder sbuild = new StringBuilder("Unexpected tag ");
			sbuild.append(element.getName().getLocalPart());
			sbuild.append(" on line ");
			sbuild.append(element.getLocation().getLineNumber());
			sbuild.append(", expected one of the following: ");
			for (String tag : tags) {
				sbuild.append(tag);
				sbuild.append(", ");
			}
			throw new IllegalArgumentException(sbuild.toString());
		}
	}
	/**
	 * Get a parameter from the XML.
	 * @param element the current tag
	 * @param param the parameter to get
	 * @return the value for that parameter
	 * @throws SPFormatException if the tag doesn't have that parameter.
	 */
	protected String getParameter(final StartElement element, final String param)
			throws SPFormatException {
		final Attribute attr = element.getAttributeByName(new QName(param));
		if (attr == null) {
			throw new MissingPropertyException(element.getName()
					.getLocalPart(), param, element.getLocation()
					.getLineNumber());
		} else {
			return attr.getValue();
		}
	}
	/**
	 * Get a parameter from the XML.
	 * @param element the current tag
	 * @param param the parameter to get
	 * @param defaultValue the value to return if the tag doesn't have that parameter
	 * @return the value for that parameter
	 */
	protected String getParameter(final StartElement element,
			final String param, final String defaultValue) {
		final Attribute attr = element.getAttributeByName(new QName(param));
		return attr == null ? defaultValue : attr.getValue();
	}
	/**
	 * Require a non-empty parameter.
	 * @param element the current tag
	 * @param param the parameter to require
	 * @param mandatory whether this is a requirement, or merely a recommendation.
	 * @param warner the Warning instance to use for the warning.
	 * @throws SPFormatException if mandatory and missing
	 */
	protected void requireNonEmptyParameter(final StartElement element,
			final String param, final boolean mandatory, final Warning warner)
			throws SPFormatException {
		if (getParameter(element, param, "").isEmpty()) {
			final SPFormatException except = new MissingPropertyException(
					element.getName().getLocalPart(), param, element
							.getLocation().getLineNumber());
			if (mandatory) {
				throw except;
			} else {
				warner.warn(except);
			}
		}
	}

	/**
	 * Move along the stream until we hit an end element matching the
	 * start-element we're parsing, but object to any start elements.
	 *
	 * @param tag the tag we're currently parsing
	 * @param reader the XML stream we're reading from
	 * @throws SPFormatException on unwanted child
	 */
	public static void spinUntilEnd(final QName tag,
			final Iterable<XMLEvent> reader) throws SPFormatException {
		for (final XMLEvent event : reader) {
			if (event.isStartElement()) {
				throw new UnwantedChildException(tag.getLocalPart(), event
						.asStartElement().getName().getLocalPart(), event
						.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& tag.equals(event.asEndElement().getName())) {
				break;
			}
		}
	}

	/**
	 * If the specified tag has an ID as a property, return it; otherwise warn
	 * about its absence and generate one.
	 *
	 * @param element the tag we're working with
	 * @param warner the Warning instance to send the warning on if the tag
	 *        doesn't specify an ID
	 * @param idFactory the factory to register an existing ID with or get a new
	 *        one from
	 * @return the ID the tag has if it has one, or otherwise a generated one.
	 * @throws SPFormatException on SP format problems reading the attribute
	 */
	protected int getOrGenerateID(final StartElement element,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		// ESCA-JAVA0177:
		final int retval; // NOPMD
		if (hasParameter(element, "id")) {
			retval = idFactory.register(Integer.parseInt(getParameter(element,
					"id")));
		} else {
			warner.warn(new MissingPropertyException(element.getName()
					.getLocalPart(), "id", element.getLocation()
					.getLineNumber()));
			retval = idFactory.createID();
		}
		return retval;
	}
	/**
	 * @param element the current tag
	 * @param param the parameter we want
	 * @return whether the tag has that parameter
	 */
	protected boolean hasParameter(final StartElement element, final String param) {
		return !(element.getAttributeByName(new QName(param)) == null);
	}
	/**
	 * @param stream a source of XMLEvents
	 * @return the file currently being read from if it's an
	 *         {@link IncludingIterator}, or the empty string otherwise.
	 */
	protected String getFile(final Iterable<XMLEvent> stream) {
		return stream.iterator() instanceof IncludingIterator ? ((IncludingIterator) stream
				.iterator()).getFile() : "";
	}
	/**
	 * @param element the current tag
	 * @param preferred the preferred name of the parameter
	 * @param deprecated the deprecated name of the parameter
	 * @param warner the warning instance to use
	 * @return the value of the parameter, gotten from the preferred form if it
	 *         has it, and from the deprecated form if the preferred form isn't
	 *         there but it is.
	 *
	 * @throws SPFormatException if the element doesn't have that attribute
	 */
	public static String getParameterWithDeprecatedForm(
			final StartElement element, final String preferred,
			final String deprecated, final Warning warner)
			throws SPFormatException {
		final Attribute prefProp = element.getAttributeByName(new QName(
				preferred));
		final Attribute deprProp = element.getAttributeByName(new QName(
				deprecated));
		if (prefProp == null && deprProp == null) {
			throw new MissingPropertyException(element.getName()
					.getLocalPart(), preferred, element.getLocation()
					.getLineNumber());
		} else if (prefProp == null) {
			warner.warn(new DeprecatedPropertyException(element.getName()
					.getLocalPart(), deprecated, preferred, element
					.getLocation().getLineNumber()));
			return deprProp.getValue(); // NOPMD
		} else {
			return prefProp.getValue();
		}
	}
	/**
	 * @param tag a tag
	 * @return whether we support it
	 */
	public abstract boolean isSupportedTag(final String tag);
	/**
	 * @param tabs a nonnegative integer
	 * @return that many tabs
	 */
	protected String indent(final int tabs) {
		final StringBuilder buf = new StringBuilder();
		for (int i = 0; i < tabs; i++) {
			buf.append('\t');
		}
		return buf.toString();
	}
}
