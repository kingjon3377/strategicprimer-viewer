package legacy.xmlio.fluidxml;

import common.xmlio.SPFormatException;
import common.xmlio.Warning;
import impl.xmlio.exceptions.DeprecatedPropertyException;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.UnsupportedPropertyException;
import impl.xmlio.exceptions.UnwantedChildException;
import legacy.idreg.IDRegistrar;
import legacy.map.HasImage;
import legacy.map.HasMutableImage;
import legacy.map.ILegacyPlayerCollection;
import legacy.map.Player;
import lovelace.util.IteratorWrapper;
import lovelace.util.LovelaceLogger;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static impl.xmlio.ISPReader.SP_NAMESPACE;

/* package */ abstract class FluidBase {
	private static final NumberFormat NUM_PARSER = NumberFormat.getIntegerInstance();

	protected FluidBase() {
	}

	/**
	 * Require that an XML tag be one of the specified tags.
	 *
	 * @param element The tag to check.
	 * @param parent  The parent tag.
	 * @param tags    The tags we accept here
	 * @throws UnwantedChildException on a tag other than one, or not in a namespace, we accept
	 */
	protected static void requireTag(final StartElement element, final @Nullable Path path, final QName parent,
	                                 final String... tags)
			throws UnwantedChildException {
		if (!SP_NAMESPACE.equals(element.getName().getNamespaceURI()) &&
				!XMLConstants.NULL_NS_URI.equals(element.getName().getNamespaceURI())) {
			throw UnwantedChildException.unexpectedNamespace(parent, path, element);
		}
		final String localName = element.getName().getLocalPart().toLowerCase();
		if (Stream.of(tags).noneMatch(localName::equalsIgnoreCase)) {
			throw UnwantedChildException.listingExpectedTags(parent, element, path, tags);
		}
	}

	/**
	 * Get a parameter from an XML tag by name. Returns null if not found
	 * in either the SP namespace or the default namespace.
	 *
	 * @param element The current XML tag
	 * @param param   The parameter we want
	 */
	private static @Nullable Attribute getAttributeByName(final StartElement element, final String param) {
		Attribute retval = element.getAttributeByName(new QName(SP_NAMESPACE, param));
		if (Objects.isNull(retval)) {
			retval = element.getAttributeByName(new QName(param));
		}
		return retval;
	}

	/**
	 * Get a parameter from the XML, returning the given default if the tag doesn't have that parameter.
	 *
	 * @param element The current tag.
	 * @param param   The parameter we want to get.
	 */
	protected static String getAttribute(final StartElement element, final String param, final String defaultValue) {
		final Attribute attr = getAttributeByName(element, param);
		String retval = null;
		if (Objects.nonNull(attr)) {
			retval = attr.getValue();
		}
		return Objects.requireNonNullElse(retval, defaultValue);
	}

	/**
	 * Get a parameter from the XML.
	 *
	 * @param element The current tag.
	 * @param param   The parameter we want to get.
	 * @throws MissingPropertyException if the tag doesn't have that parameter
	 */
	protected static String getAttribute(final StartElement element, final @Nullable Path file, final String param)
			throws MissingPropertyException {
		final Attribute attr = getAttributeByName(element, param);
		if (Objects.isNull(attr)) {
			throw new MissingPropertyException(element, file, param);
		}
		final String retval = attr.getValue();
		if (Objects.isNull(retval)) {
			throw new MissingPropertyException(element, file, param);
		} else {
			return retval;
		}
	}

	/**
	 * Get an attribute that should only contain "true" or "false" from the
	 * XML, returning the specified value if the attribute does not exist
	 * or has a non-boolean value. TODO: Audit callers: should provide their own Warning
	 *
	 * @param element      The current tag.
	 * @param param        The parameter we want to get
	 * @param defaultValue The value to return if the tag doesn't have that parameter
	 */
	protected static boolean getBooleanAttribute(final StartElement element, final @Nullable Path path,
	                                             final String param, final boolean defaultValue) {
		return getBooleanAttribute(element, path, param, defaultValue, Warning.WARN);
	}

	/**
	 * Get an attribute that should only contain "true" or "false" from the
	 * XML, returning the specified value if the attribute does not exist
	 * or has a non-boolean value.
	 *
	 * @param element      The current tag.
	 * @param param        The parameter we want to get
	 * @param defaultValue The value to return if the tag doesn't have that parameter
	 * @param warner       The {@link Warning} instance to use if the attribute was present but non-Boolean but a
	 *                     default was provided
	 */
	protected static boolean getBooleanAttribute(final StartElement element, final @Nullable Path path,
	                                             final String param, final boolean defaultValue, final Warning warner) {
		final Attribute attr = getAttributeByName(element, param);
		if (Objects.isNull(attr)) {
			return defaultValue;
		}
		final String val = attr.getValue();
		if (Objects.isNull(val) || val.isEmpty()) {
			return defaultValue;
		}
		if ("true".equalsIgnoreCase(val)) {
			return true;
		} else if ("false".equalsIgnoreCase(val)) {
			return false;
		} else {
			warner.handle(new MissingPropertyException(element, path, param,
					new IllegalArgumentException("Cannot parse boolean from " + val)));
			return defaultValue;
		}
	}

	/**
	 * Get an attribute that should only contain "true" or "false" from the
	 * XML. If not provided or not "true" or "false", throws an exception.
	 *
	 * @param element The current tag.
	 * @param param   The parameter we want to get
	 * @throws MissingPropertyException if the tag doesn't have that parameter and no default was provided
	 */
	protected static boolean getBooleanAttribute(final StartElement element, final @Nullable Path path,
	                                             final String param)
			throws MissingPropertyException {
		final Attribute attr = getAttributeByName(element, param);
		if (Objects.isNull(attr)) {
			throw new MissingPropertyException(element, path, param);
		}
		final String val = attr.getValue();
		if (Objects.isNull(val) || val.isEmpty()) {
			throw new MissingPropertyException(element, path, param);
		}
		if ("true".equalsIgnoreCase(val)) {
			return true;
		} else if ("false".equalsIgnoreCase(val)) {
			return false;
		} else {
			throw new MissingPropertyException(element, path, param,
					new IllegalArgumentException("Cannot parse boolean from " + val));
		}
	}

	/**
	 * Require (or recommend) that a parameter (to be subsequently
	 * retrieved via {@link #getAttribute}) be non-empty.
	 *
	 * @throws SPFormatException if mandatory and missing
	 * @param element   The current tag.
	 * @param param     The desired parameter.
	 * @param mandatory Whether this is a requirement. If true, we throw the exception; if false, we merely warn.
	 * @param warner    The Warning instance to use if non-mandatory.
	 *
	 *
	 * TODO: Split into "require" and "recommend" method rather than having Boolean parameter?
	 */
	protected static void requireNonEmptyAttribute(final StartElement element, final @Nullable Path path,
	                                               final String param, final boolean mandatory, final Warning warner)
			throws SPFormatException {
		if (getAttribute(element, param, "").isEmpty()) {
			final SPFormatException except = new MissingPropertyException(element, path, param);
			if (mandatory) {
				throw except;
			} else {
				warner.handle(except);
			}
		}
	}

	/**
	 * Whether the given XML element is a {@link StartElement} and in a namespace we support.
	 */
	protected static boolean isSPStartElement(final XMLEvent element) {
		return element instanceof final StartElement se &&
				(SP_NAMESPACE.equals(se.getName().getNamespaceURI()) ||
						XMLConstants.NULL_NS_URI.equals(se.getName().getNamespaceURI()));
	}

	/**
	 * Move along the stream until we hit an end element matching the given
	 * start-element, but object to any other start elements in our
	 * namespaces.
	 *
	 * @param tag    The tag the caller is currently parsing, whose matching end-tag we're looking for
	 * @param reader the stream of XML
	 * @throws UnwantedChildException on unwanted child tags
	 */
	protected static void spinUntilEnd(final QName tag, final @Nullable Path path, final Iterable<XMLEvent> reader)
			throws UnwantedChildException {
		for (final XMLEvent event : reader) {
			switch (event) {
				case final StartElement se when isSPStartElement(event) ->
						throw new UnwantedChildException(tag, se, path);
				case final EndElement ee when tag.equals(ee.getName()) -> {
					return;
				}
				default -> {
				}
			}
		}
	}

	/**
	 * Whether an XML tag has the given parameter.
	 *
	 * @param element The current tag
	 * @param param   The parameter we want
	 */
	protected static boolean hasAttribute(final StartElement element, final String param) {
		return Objects.nonNull(getAttributeByName(element, param));
	}

	/**
	 * If the specified tag has an ID as a property, return it; otherwise
	 * warn about its absence and generate one.
	 *
	 * @param element   The tag we're working with
	 * @param warner    The Warning instance to use if hte tag doesn't specify an ID
	 * @param idFactory The factory to use to register an existing ID or get a new one
	 * @throws MissingPropertyException on SP format problems reading the property
	 */
	protected static int getOrGenerateID(final StartElement element, final Warning warner, final @Nullable Path path,
										 final IDRegistrar idFactory) throws MissingPropertyException {
		if (hasAttribute(element, "id")) {
			try {
				return idFactory.register(
						NUM_PARSER.parse(getAttribute(element, path, "id")).intValue(),
						warner, Pair.with(path, element.getLocation()));
			} catch (final NumberFormatException | ParseException except) {
				throw new MissingPropertyException(element, path, "id", except);
			}
		} else {
			warner.handle(new MissingPropertyException(element, path, "id"));
			return idFactory.createID();
		}
	}

	/**
	 * If the given XML tag has the preferred parameter, return its value;
	 * if not, but it has the deprecated parameter, fire a warning but
	 * return its value; otherwise, throw an exception.
	 *
	 * @param element    The current tag
	 * @param file       The file containing the tag
	 * @param preferred  The preferred name of the parameter
	 * @param deprecated The deprecated name of the parameter
	 * @param warner     The Warning instance to use
	 * @throws MissingPropertyException if the tag has neither parameter
	 *
	 * TODO: Accept a default-value parameter and/or a type-conversion parameter
	 */
	protected static String getAttrWithDeprecatedForm(final StartElement element, final @Nullable Path file,
	                                                  final String preferred, final String deprecated,
	                                                  final Warning warner)
			throws MissingPropertyException {
		if (hasAttribute(element, preferred)) {
			return getAttribute(element, file, preferred);
		} else if (hasAttribute(element, deprecated)) {
			warner.handle(new DeprecatedPropertyException(element, file, deprecated, preferred));
			return getAttribute(element, file, deprecated);
		} else {
			throw new MissingPropertyException(element, file, preferred);
		}
	}

	/**
	 * Write the given number of tabs to the given stream.
	 *
	 * @param ostream The stream to write the tabs to.
	 * @param tabs    The number of tabs to write.
	 * @throws XMLStreamException on I/O error writing to the stream
	 */
	protected static void indent(final XMLStreamWriter ostream, final int tabs) throws XMLStreamException {
		if (tabs < 0) {
			throw new IllegalArgumentException("Cannot write a negative number of tabs.");
		}
		ostream.writeCharacters(System.lineSeparator());
		for (int i = 0; i < tabs; i++) {
			ostream.writeCharacters("\t");
		}
	}

	/**
	 * Write an attribute if its value is nonempty.
	 *
	 * @param ostream The stream to write to
	 * @param items   The names and values of the attributes to write.
	 * @throws XMLStreamException on I/O error
	 */
	@SafeVarargs
	protected static void writeNonEmptyAttributes(final XMLStreamWriter ostream,
												  final Pair<String, String>... items) throws XMLStreamException {
		for (final Pair<String, String> pair : items) {
			if (!pair.getValue1().isEmpty()) {
				ostream.writeAttribute(SP_NAMESPACE, pair.getValue0(),
						pair.getValue1());
			}
		}
	}

	/**
	 * If the object has a custom (non-default) image, write it to XML.
	 *
	 * @param ostream The stream to write to
	 * @param obj     The object being written out that might have a custom image
	 * @throws XMLStreamException on I/O error when writing
	 */
	protected static void writeImage(final XMLStreamWriter ostream, final HasImage obj)
			throws XMLStreamException {
		final String image = obj.getImage();
		if (!image.isEmpty() && !image.equals(obj.getDefaultImage())) {
			writeNonEmptyAttributes(ostream, Pair.with("image", image));
		}
	}

	/**
	 * Parse an integer, throwing an exception on non-numeric or otherwise malformed input.
	 *
	 * @param string   The text to parse
	 * @param location The current location in the XML.
	 * @throws ParseException if the string is non-numeric or otherwise malformed
	 */
	private static Integer parseInt(final String string, final Location location) throws ParseException {
		return NUM_PARSER.parse(string).intValue();
	}

	/**
	 * Parse an Integer parameter.
	 *
	 * TODO: Replace this with a conversion function passed to {@link #getAttribute}
	 *
	 * @param tag          The tag to get the parameter from
	 * @param parameter    The name of the desired parameter
	 * @param defaultValue The number to return if the parameter doesn't exist
	 */
	public static int getIntegerAttribute(final StartElement tag, final String parameter, final int defaultValue) {
		return getIntegerAttribute(tag, parameter, defaultValue, Warning.WARN);
	}

	/**
	 * Parse an Integer parameter.
	 *
	 * TODO: Replace this with a conversion function passed to {@link #getAttribute}
	 *
	 * @param tag          The tag to get the parameter from
	 * @param parameter    The name of the desired parameter
	 * @param defaultValue The number to return if the parameter doesn't exist
	 * @param warner       The {@link Warning} instance to use if input is malformed
	 */
	protected static int getIntegerAttribute(final StartElement tag, final String parameter, final int defaultValue,
											 final Warning warner) {
		final Attribute attr = getAttributeByName(tag, parameter);
		if (Objects.isNull(attr)) {
			return defaultValue;
		}
		final String val = attr.getValue();
		if (Objects.isNull(val)) {
			return defaultValue;
		}
		try {
			return parseInt(val, tag.getLocation());
		} catch (final ParseException except) {
			warner.handle(except);
			return defaultValue;
		}
	}

	/**
	 * Parse an Integer parameter.
	 *
	 * TODO: Replace this with a conversion function passed to {@link #getAttribute}
	 *
	 * @param tag       The tag to get the parameter from
	 * @param parameter The name of the desired parameter
	 * @throws MissingPropertyException if the tag doesn't have that parameter, or
	 *                           if its value is non-numeric or otherwise malformed
	 */
	public static int getIntegerAttribute(final StartElement tag, final @Nullable Path path,
	                                      final String parameter)
			throws MissingPropertyException {
		final Attribute attr = getAttributeByName(tag, parameter);
		if (Objects.isNull(attr)) {
			throw new MissingPropertyException(tag, path, parameter);
		}
		final String val = attr.getValue();
		if (Objects.isNull(val)) {
			throw new MissingPropertyException(tag, path, parameter);
		}
		try {
			return parseInt(val, tag.getLocation());
		} catch (final ParseException except) {
			throw new MissingPropertyException(tag, path, parameter, except);
		}
	}

	/**
	 * Parse an XML parameter whose value can be an Integer or a Decimal. FIXME: Inline (callers pass warner)
	 *
	 * TODO: Replace this with a conversion function passed to {@link #getAttribute}
	 *
	 * @param tag          The tag to get the parameter from
	 * @param parameter    The name of the desired parameter
	 * @param defaultValue The number to return if the parameter doesn't exist
	 * @throws MissingPropertyException if the tag's value is non-numeric or
	 *                           otherwise malformed
	 */
	protected static Number getNumericAttribute(final StartElement tag, final @Nullable Path path,
	                                            final String parameter, final Number defaultValue)
			throws MissingPropertyException {
		return getNumericAttribute(tag, path, parameter, defaultValue, Warning.WARN);
	}

	/**
	 * Parse an XML parameter whose value can be an Integer or a Decimal.
	 *
	 * TODO: Replace this with a conversion function passed to {@link #getAttribute}
	 *
	 * @param tag          The tag to get the parameter from
	 * @param parameter    The name of the desired parameter
	 * @param defaultValue The number to return if the parameter doesn't exist
	 * @param warner       The {@link Warning} instance to use if input is malformed
	 * @throws MissingPropertyException if the tag's value is non-numeric or
	 *                           otherwise malformed
	 */
	protected static Number getNumericAttribute(final StartElement tag, final @Nullable Path path,
	                                            final String parameter, final Number defaultValue,
	                                            final Warning warner)
			throws MissingPropertyException {
		final Attribute attr = getAttributeByName(tag, parameter);
		if (Objects.isNull(attr)) {
			return defaultValue;
		}
		final String val = attr.getValue();
		if (Objects.isNull(val)) {
			return defaultValue;
		}
		if (val.contains(".")) {
			try {
				return new BigDecimal(val);
			} catch (final NumberFormatException except) {
				throw new MissingPropertyException(tag, path, parameter, except);
			}
		} else {
			try {
				return parseInt(val, tag.getLocation());
			} catch (final ParseException except) {
				throw new MissingPropertyException(tag, path, parameter, except);
			}
		}
	}

	/**
	 * Parse an XML parameter whose value can be an Integer or a Decimal.
	 *
	 * TODO: Replace this with a conversion function passed to {@link #getAttribute}
	 *
	 * @param tag       The tag to get the parameter from
	 * @param parameter The name of the desired parameter
	 * @throws MissingPropertyException if the tag doesn't have that parameter, or
	 *                           if its value is non-numeric or otherwise malformed
	 */
	protected static Number getNumericAttribute(final StartElement tag, final @Nullable Path path,
	                                            final String parameter)
			throws MissingPropertyException {
		final Attribute attr = getAttributeByName(tag, parameter);
		if (Objects.isNull(attr)) {
			throw new MissingPropertyException(tag, path, parameter);
		}
		final String val = attr.getValue();
		if (Objects.isNull(val)) {
			throw new MissingPropertyException(tag, path, parameter);
		}
		if (val.contains(".")) {
			try {
				return new BigDecimal(val);
			} catch (final NumberFormatException except) {
				throw new MissingPropertyException(tag, path, parameter, except);
			}
		} else {
			try {
				return parseInt(val, tag.getLocation());
			} catch (final ParseException except) {
				throw new MissingPropertyException(tag, path, parameter, except);
			}
		}
	}

	/**
	 * Write the necessary number of tab characters and a tag.
	 *
	 * TODO: Split to "writeLeafTag" and "writeNonLeafTag"?
	 *
	 * @param ostream     The stream to write to
	 * @param tag         The tag to write
	 * @param indentation The indentation level. If positive, write a newline before indenting; if zero, write a
	 *                    default-namespace declaration.
	 * @param leaf        Whether to automatically close the tag
	 * @throws XMLStreamException on I/O error writing to the stream
	 */
	protected static void writeTag(final XMLStreamWriter ostream, final String tag, final int indentation,
								   final boolean leaf)
			throws XMLStreamException {
		if (indentation < 0) {
			throw new IllegalArgumentException("Indentation cannot be negative");
		}
		if (indentation > 0) {
			indent(ostream, indentation);
		}
		if (leaf) {
			ostream.writeEmptyElement(SP_NAMESPACE, tag);
		} else {
			ostream.writeStartElement(SP_NAMESPACE, tag);
		}
		if (indentation == 0) {
			ostream.writeDefaultNamespace(SP_NAMESPACE);
		}
	}

	/**
	 * Write attributes to XML.
	 *
	 * @param ostream    The stream to write to
	 * @param attributes The name and values of the attributes to write.
	 *                   Only supports String, boolean, and numeric attributes.
	 * @throws XMLStreamException on I/O error
	 */
	@SuppressWarnings("ChainOfInstanceofChecks")
	@SafeVarargs
	protected static void writeAttributes(final XMLStreamWriter ostream, final Pair<String, ?>... attributes)
			throws XMLStreamException {
		for (final Pair<String, ?> pair : attributes) {
			final String name = pair.getValue0();
			final Object item = pair.getValue1();
			if (item instanceof final String s) {
				ostream.writeAttribute(SP_NAMESPACE, name, s);
			} else if (item instanceof final BigDecimal bd) {
				if (bd.scale() <= 0) {
					ostream.writeAttribute(SP_NAMESPACE, name,
							Integer.toString(bd.intValue()));
				} else {
					ostream.writeAttribute(SP_NAMESPACE, name,
							bd.toPlainString());
				}
			} else if (item instanceof BigInteger || item instanceof Integer ||
					item instanceof Boolean || item instanceof Long) {
				ostream.writeAttribute(SP_NAMESPACE, name, item.toString());
			} else {
				LovelaceLogger.warning(
						"Unhandled attribute type %s in FluidBase.writeAttributes",
						item.getClass().getName());
				ostream.writeAttribute(SP_NAMESPACE, name, item.toString());
			}
		}
	}

	/**
	 * If the specified tag has an "owner" property, return the player it
	 * indicates; otherwise warn about its absence and return the
	 * "independent" player from the player collection.
	 *
	 * @param element The tag we're working with
	 * @param warner  The Warning instance to use
	 * @param players The collection of players to refer to
	 * @throws MissingPropertyException on SP format problems reading the attribute
	 */
	protected static Player getPlayerOrIndependent(final StartElement element, final @Nullable Path path,
	                                               final Warning warner, final ILegacyPlayerCollection players)
			throws MissingPropertyException {
		if (hasAttribute(element, "owner")) {
			return players.getPlayer(getIntegerAttribute(element, path, "owner"));
		} else {
			warner.handle(new MissingPropertyException(element, path, "owner"));
			return players.getIndependent();
		}
	}

	/**
	 * Set an object's image property if an image filename is specified in the XML.
	 *
	 * @param obj     The object in question
	 * @param element The current XML tag
	 * @param warner  The Warning instance to use if the object can't have an image but the XML specifies one
	 */
	protected static <Type> Type setImage(final Type obj, final StartElement element, final @Nullable Path path,
	                                      final Warning warner) {
		if (obj instanceof final HasMutableImage hmi) {
			hmi.setImage(getAttribute(element, "image", ""));
		} else if (hasAttribute(element, "image")) {
			warner.handle(new UnsupportedPropertyException(element, path, "image"));
		}
		return obj;
	}

	/**
	 * Get the text between here and the closing tag we're looking for; any
	 * intervening tags cause an exception.
	 *
	 * @param tag    The name of the tag whose closing tag we're waitng for
	 * @param stream The stream of XML elements to sift through
	 * @throws UnwantedChildException on unwanted intervening tags
	 */
	protected static String getTextUntil(final QName tag, final @Nullable Path path, final Iterable<XMLEvent> stream)
			throws UnwantedChildException {
		final StringBuilder builder = new StringBuilder();
		for (final XMLEvent event : stream) {
			switch (event) {
				case final StartElement se when isSPStartElement(event) ->
						throw new UnwantedChildException(tag, se, path);
				case final Characters c -> builder.append(c.getData());
				case final EndElement ee when tag.equals(ee.getName()) -> {
					return builder.toString().strip();
				}
				default -> {
				}
			}
		}
		return builder.toString().strip();
	}

	/**
	 * A helper method to allow the writer methods to require a specific type.
	 */
	protected static <Type> FluidXMLWriter<Object> castingWriter(final FluidXMLWriter<Type> wrapped,
																 final Class<Type> cls) {
		return (writer, obj, indent) -> {
			if (!cls.isInstance(obj)) {
				throw new IllegalArgumentException("Can only write " + cls.getName());
			}
			wrapped.write(writer, cls.cast(obj), indent);
		};
	}

	private static boolean isSupportedNamespace(final QName name) {
		return SP_NAMESPACE.equals(name.getNamespaceURI()) ||
				XMLConstants.NULL_NS_URI.equals(name.getNamespaceURI());
	}

	/**
	 * Warn if any unsupported attribute is on this tag.
	 */
	protected static void expectAttributes(final StartElement element, final @Nullable Path path, final Warning warner,
	                                       final String... attributes) {
		final List<String> local = Stream.of(attributes).map(String::toLowerCase).toList();
		for (final Attribute attribute : new IteratorWrapper<>(
				element.getAttributes())) {
			final QName name = attribute.getName();
			if (isSupportedNamespace(name) &&
					!local.contains(name.getLocalPart().toLowerCase())) {
				warner.handle(new UnsupportedPropertyException(element, path,
						name.getLocalPart()));
			}
		}
	}
}
