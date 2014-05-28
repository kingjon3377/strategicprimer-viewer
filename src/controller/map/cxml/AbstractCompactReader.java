package controller.map.cxml;

import static java.lang.String.format;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.HasImage;

import org.eclipse.jdt.annotation.Nullable;

import util.EqualsAny;
import util.NullCleaner;
import util.Warning;
import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;

// ESCA-JAVA0011: Doesn't contain an abstract method
// because we moved it up to the interface.
/**
 * A superclass to provide helper methods.
 *
 * @param <T> a type parameter, since we now "implement" the interface
 *
 * @author Jonathan Lovelace
 *
 */
public abstract class AbstractCompactReader<T> implements CompactReader<T> {
	/**
	 * The string to use instead of the tag in exceptions when the tag is null.
	 */
	private static final String NULL_TAG = "a null tag";
	/**
	 * The amount of buffer memory to allocate for the name of each tag. We're
	 * aiming to err on the side of overestimation, to avoid resizing the
	 * buffer.
	 */
	private static final int MAX_TAG_LEN = 10;
	/**
	 * Do not instantiate directly.
	 */
	protected AbstractCompactReader() {
		// Nothing to do
	}

	/**
	 * Require that an element be one of the specified tags.
	 *
	 * @param element the element to check
	 * @param tags the tags we accept here
	 */
	protected static void requireTag(final StartElement element,
			final String... tags) {
		final String localName = element.getName().getLocalPart();
		if (localName == null) {
			final int line = element.getLocation().getLineNumber();
			final String base =
					format("Null tag on line %d, expected one of the following: ",
							Integer.valueOf(line));
			final int len = base.length() + tags.length
					* MAX_TAG_LEN; // Overestimate just in case
			final StringBuilder sbuild = new StringBuilder(len)
					.append(base);
			for (final String tag : tags) {
				sbuild.append(tag);
				sbuild.append(", ");
			}
			throw new IllegalArgumentException(sbuild.toString());
		} else if (!(EqualsAny.equalsAny(localName, tags))) {
			final int line = element.getLocation().getLineNumber();
			final String base = format(
					"Unexpected tag %s on line %d, expected one of the following: ",
					localName, Integer.valueOf(line));
			final int len = base.length() + tags.length
					* MAX_TAG_LEN; // Overestimate just in case
			final StringBuilder sbuild = new StringBuilder(len)
					.append(base);
			for (final String tag : tags) {
				sbuild.append(tag);
				sbuild.append(", ");
			}
			throw new IllegalArgumentException(sbuild.toString());
		}
	}

	/**
	 * Get a parameter from the XML.
	 *
	 * @param element the current tag
	 * @param param the parameter to get
	 * @return the value for that parameter
	 * @throws SPFormatException if the tag doesn't have that parameter.
	 */
	protected static String getParameter(final StartElement element,
			final String param) throws SPFormatException {
		final Attribute attr = element.getAttributeByName(new QName(param));
		final String local = element.getName().getLocalPart();
		if (attr == null) {
			throw new MissingPropertyException(tagOrNull(local), param, element
					.getLocation().getLineNumber());
		} else {
			final String value = attr.getValue();
			if (value == null) {
				throw new MissingPropertyException(tagOrNull(local), param,
						element.getLocation().getLineNumber());
			} else {
				return value;
			}
		}
	}

	/**
	 * Get a parameter from the XML.
	 *
	 * @param element the current tag
	 * @param param the parameter to get
	 * @param defaultValue the value to return if the tag doesn't have that
	 *        parameter
	 * @return the value for that parameter
	 */
	protected static String getParameter(final StartElement element,
			final String param, final String defaultValue) {
		final Attribute attr = element.getAttributeByName(new QName(param));
		if (attr == null) {
			return defaultValue; // NOPMD
		} else {
			return valueOrDefault(attr.getValue(), defaultValue);
		}
	}

	/**
	 * Require a non-empty parameter.
	 *
	 * @param element the current tag
	 * @param param the parameter to require
	 * @param mandatory whether this is a requirement, or merely a
	 *        recommendation.
	 * @param warner the Warning instance to use for the warning.
	 * @throws SPFormatException if mandatory and missing
	 */
	protected static void requireNonEmptyParameter(final StartElement element,
			final String param, final boolean mandatory, final Warning warner)
			throws SPFormatException {
		if (getParameter(element, param, "").isEmpty()) {
			final String local = tagOrNull(element.getName().getLocalPart());
			final SPFormatException except = new MissingPropertyException(
					local, param, element.getLocation().getLineNumber());
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
	protected static void spinUntilEnd(final QName tag,
			final Iterable<XMLEvent> reader) throws SPFormatException {
		for (final XMLEvent event : reader) {
			if (event.isStartElement()) {
				throw new UnwantedChildException(tagOrNull(tag.getLocalPart()),
						tagOrNull(event.asStartElement().getName()
								.getLocalPart()), event.getLocation()
								.getLineNumber());
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
	protected static int getOrGenerateID(final StartElement element,
			final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		// ESCA-JAVA0177:
		final int retval; // NOPMD
		if (hasParameter(element, "id")) {
			retval = idFactory.register(Integer.parseInt(getParameter(element,
					"id")));
		} else {
			final String tag = element.getName().getLocalPart();
			warner.warn(new MissingPropertyException(tagOrNull(tag), "id",
					element.getLocation().getLineNumber()));
			retval = idFactory.createID();
		}
		return retval;
	}

	/**
	 * @param element the current tag
	 * @param param the parameter we want
	 * @return whether the tag has that parameter
	 */
	protected static boolean hasParameter(final StartElement element,
			final String param) {
		return element.getAttributeByName(new QName(param)) != null;
	}

	/**
	 * @param stream a source of XMLEvents
	 * @return the file currently being read from if it's an
	 *         {@link IncludingIterator}, or the empty string otherwise.
	 */
	protected static String getFile(final Iterable<XMLEvent> stream) {
		final Iterator<?> iter = stream.iterator();
		if (iter instanceof IncludingIterator) {
			return ((IncludingIterator) iter).getFile(); // NOPMD
		} else {
			return "";
		}
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
	protected static String getParamWithDeprecatedForm(final StartElement element,
			final String preferred, final String deprecated,
			final Warning warner) throws SPFormatException {
		final Attribute prefProp = element.getAttributeByName(new QName(
				preferred));
		final Attribute deprProp = element.getAttributeByName(new QName(
				deprecated));
		final String local = tagOrNull(element.getName().getLocalPart());
		final MissingPropertyException exception = new MissingPropertyException(
				local, preferred, element.getLocation().getLineNumber());
		if (prefProp == null && deprProp == null) {
			throw exception;
		} else if (prefProp == null) {
			warner.warn(new DeprecatedPropertyException(local, deprecated,
					preferred, element.getLocation().getLineNumber()));
			final String value = deprProp.getValue();
			if (value == null) {
				throw exception;
			}
			return value; // NOPMD
		} else {
			final String prefValue = prefProp.getValue();
			if (prefValue == null) {
				if (deprProp == null) {
					throw exception;
				} else {
					final String deprValue = deprProp.getValue();
					if (deprValue == null) {
						throw exception;
					} else {
						return deprValue; // NOPMD
					}
				}
			} else {
				return prefValue;
			}
		}
	}

	/**
	 * @param tabs a nonnegative integer
	 * @return that many tabs
	 * TODO: This should probably take Appendable and write the tabs directly.
	 */
	protected static String indent(final int tabs) {
		final StringBuilder buf = new StringBuilder();
		for (int i = 0; i < tabs; i++) {
			buf.append('\t');
		}
		return NullCleaner.assertNotNull(buf.toString());
	}

	/**
	 * @param obj an object being written out that might have a custom image
	 * @return the XML for the image if it does, or the empty string if not
	 */
	protected static String imageXML(final HasImage obj) {
		final String image = obj.getImage();
		if (image.isEmpty() || image.equals(obj.getDefaultImage())) {
			return ""; // NOPMD
		} else {
			return " image=\"" + image + "\"";
		}
	}

	/**
	 * Do not call this on anything that actually might be null; that will cause
	 * an assertion failure and thus a crash.
	 *
	 * @param qname a QName that should never be null but Eclipse thinks might
	 *        be
	 * @return it, after asserting it is not null
	 */
	protected static QName assertNotNullQName(@Nullable final QName qname) {
		return NullCleaner.assertNotNull(qname);
	}

	/**
	 * @param element
	 *            a StartElement, known from the guarantees of the documentation
	 *            of the API to not be null, but which Eclipse is convinced
	 *            might be
	 * @return it, after asserting it is not null
	 */
	protected static StartElement assertNotNullStartElement(
			@Nullable final StartElement element) {
		return NullCleaner.assertNotNull(element);
	}
	/**
	 * @param tag the name of a tag, which may be null
	 * @return "a null tag" if null, or the tag name if not
	 */
	private static String tagOrNull(@Nullable final String tag) {
		return valueOrDefault(tag, NULL_TAG);
	}
	/**
	 * @param <T> the type of thing we're talking about here
	 * @param value a value
	 * @param def a default value
	 * @return value if it isn't null, or default if it is
	 */
	private static <T> T valueOrDefault(@Nullable final T value, final T def) {
		if (value == null) {
			return def; // NOPMD
		} else {
			return value;
		}
	}
}
