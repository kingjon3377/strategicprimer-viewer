package controller.map.readerng;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.HasImage;
import model.map.Player;
import model.map.PlayerCollection;

import org.eclipse.jdt.annotation.Nullable;

import util.Warning;
import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;

/**
 * A class for helper methods.
 *
 * @author Jonathan Lovelace
 *
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public final class XMLHelper {
	/**
	 * Do not instantiate.
	 */
	private XMLHelper() {
		// Don't instantiate
	}

	/**
	 * @param startElement a tag
	 * @param attribute the attribute we want
	 *
	 * @return the value of that attribute.
	 * @throws SPFormatException if the element doesn't have that attribute
	 */
	public static String getAttribute(final StartElement startElement,
			final String attribute) throws SPFormatException {
		final Attribute attr = startElement.getAttributeByName(new QName(
				attribute));
		if (attr == null || attr.getValue() == null) {
			final String local = startElement.getName().getLocalPart();
			assert local != null;
			throw new MissingPropertyException(local, attribute, startElement
					.getLocation().getLineNumber());
		}
		final String retval = attr.getValue();
		assert retval != null;
		return retval;
	}

	/**
	 * A variant of getAttribute() that returns a specified default value if the
	 * attribute is missing, instead of throwing an exception.
	 *
	 * @param elem the element
	 * @param attr the attribute we want
	 * @param defaultValue the default value if the element doesn't have the
	 *        attribute
	 *
	 * @return the value of attribute if it exists, or the default
	 */
	public static String getAttribute(final StartElement elem,
			final String attr, final String defaultValue) {
		final Attribute value = elem.getAttributeByName(new QName(attr));
		if (value == null) {
			return defaultValue; // NOPMD
		} else {
			final String retval = value.getValue();
			assert retval != null;
			return retval;
		}
	}

	/**
	 * @param element the element
	 * @param preferred the preferred name of the attribute
	 * @param deprecated the deprecated name of the attribute * @throws
	 *        SPFormatException if the element doesn't have that attribute
	 * @param warner the warning instance to use
	 * @return the value of the attribute, gotten from the preferred form if it
	 *         has it, and from the deprecated form if the preferred form isn't
	 *         there but it is.
	 *
	 * @throws SPFormatException if the element doesn't have that attribute
	 */
	public static String getAttributeWithDeprecatedForm(
			final StartElement element, final String preferred,
			final String deprecated, final Warning warner)
			throws SPFormatException {
		final Attribute prefAttr = element.getAttributeByName(new QName(
				preferred));
		final Attribute deprAttr = element.getAttributeByName(new QName(
				deprecated));
		final String local = element.getName().getLocalPart();
		assert local != null;
		if (prefAttr == null && deprAttr == null) {
			throw new MissingPropertyException(local, preferred, element
					.getLocation().getLineNumber());
		} else if (prefAttr == null) {
			warner.warn(new DeprecatedPropertyException(local, deprecated,
					preferred, element.getLocation().getLineNumber()));
			final String value = deprAttr.getValue();
			assert value != null;
			return value; // NOPMD
		} else {
			final String value = prefAttr.getValue();
			assert value != null;
			return value;
		}
	}

	/**
	 * @param elem an element
	 * @param attr the attribute we want
	 * @return whether the element has that attribute
	 */
	public static boolean hasAttribute(final StartElement elem,
			final String attr) {
		return !(elem.getAttributeByName(new QName(attr)) == null);
	}

	/**
	 * Move along the stream until we hit an end element, but object to any
	 * start elements.
	 *
	 * @param tag what kind of tag we're in (for the error message)
	 * @param reader the XML stream we're reading from
	 * @throws SPFormatException on unwanted child
	 */
	public static void spinUntilEnd(final QName tag,
			final Iterable<XMLEvent> reader) throws SPFormatException {
		for (final XMLEvent event : reader) {
			if (event.isStartElement()) {
				final String iLocal = event.asStartElement().getName()
						.getLocalPart();
				final String oLocal = tag.getLocalPart();
				assert iLocal != null && oLocal != null;
				throw new UnwantedChildException(oLocal, iLocal,
						event.getLocation().getLineNumber());
			} else if (event.isEndElement()
					&& tag.equals(event.asEndElement().getName())) {
				break;
			}
		}
	}

	/**
	 * Throw an exception (or warn) if the specified parameter is missing or
	 * empty.
	 *
	 * @param element the current element
	 * @param parameter the parameter to check
	 * @param mandatory whether we should throw or just warn
	 * @param warner the Warning instance to use if not mandatory
	 * @throws SPFormatException if the parameter is mandatory and missing
	 */
	public static void requireNonEmptyParameter(final StartElement element,
			final String parameter, final boolean mandatory,
			final Warning warner) throws SPFormatException {
		if (getAttribute(element, parameter, "").isEmpty()) {
			final String local = element.getName().getLocalPart();
			assert local != null;
			final SPFormatException except = new MissingPropertyException(
					local, parameter, element.getLocation().getLineNumber());
			if (mandatory) {
				throw except;
			} else {
				warner.warn(except);
			}
		}
	}

	/**
	 * If the specified tag has an "owner" property, return the player it
	 * indicates; otherwise warn about its absence and return the "independent"
	 * player from the player collection.
	 *
	 * @param element the tag we're working with
	 * @param warner the Warning instance to send the warning on
	 * @param players the collection of players to refer to
	 * @return a suitable player
	 * @throws SPFormatException on SP format problems reading the attribute.
	 */
	public static Player getPlayerOrIndependent(final StartElement element,
			final Warning warner, final PlayerCollection players)
			throws SPFormatException {
		// ESCA-JAVA0177:
		final Player retval; // NOPMD
		if (hasAttribute(element, "owner")) {
			retval = players.getPlayer(Integer.parseInt(getAttribute(element,
					"owner")));
		} else {
			final String local = element.getName().getLocalPart();
			assert local != null;
			warner.warn(new MissingPropertyException(local, "owner", element
					.getLocation().getLineNumber()));
			retval = players.getIndependent();
		}
		return retval;
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
	public static int getOrGenerateID(final StartElement element,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		// ESCA-JAVA0177:
		final int retval; // NOPMD
		if (hasAttribute(element, "id")) {
			retval = idFactory.register(Integer.parseInt(getAttribute(element,
					"id")));
		} else {
			final String local = element.getName().getLocalPart();
			assert local != null;
			warner.warn(new MissingPropertyException(local, "id", element
					.getLocation().getLineNumber()));
			retval = idFactory.createID();
		}
		return retval;
	}

	/**
	 * @param stream a source of XMLEvents
	 * @return the file currently being read from if it's an
	 *         {@link IncludingIterator}, or the empty string otherwise.
	 */
	public static String getFile(final Iterable<XMLEvent> stream) {
		return stream.iterator() instanceof IncludingIterator ? ((IncludingIterator) stream
				.iterator()).getFile() : "";
	}

	/**
	 * If there is an image attribute on the element, add that to the object
	 * being constructed. If not, do nothing.
	 *
	 * @param element the XML element being read
	 * @param obj the object being constructed.
	 * @throws SPFormatException on SP format error
	 */
	public static void addImage(final StartElement element, final HasImage obj)
			throws SPFormatException {
		if (hasAttribute(element, "image")) {
			obj.setImage(getAttribute(element, "image"));
		}
	}
	/**
	 * Do not pass in anything that might actually be null in real conditions;
	 * that will cause an assertion failure and thus a crash.
	 *
	 * @param <T> what's in the list
	 * @param list a list
	 * @return it, without any hint that it might be null.
	 */
	public static <T> List<T> assertNonNullList(@Nullable final List<T> list) {
		assert list != null;
		return list;
	}
	/**
	 * Do not pass in anything that might actually be null; that will cause an assertion failure and thus a crash.
	 * @param qname a qualified name
	 * @return it, without any hint that it might be null
	 */
	public static QName assertNonNullQName(@Nullable final QName qname) {
		assert qname != null;
		return qname;
	}
}
