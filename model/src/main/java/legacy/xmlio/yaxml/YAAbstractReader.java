package legacy.xmlio.yaxml;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import org.javatuples.Pair;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Attribute;

import legacy.idreg.IDRegistrar;
import legacy.map.Point;
import legacy.map.HasImage;
import common.xmlio.Warning;
import common.xmlio.SPFormatException;
import impl.xmlio.exceptions.UnwantedChildException;
import impl.xmlio.exceptions.MissingPropertyException;
import impl.xmlio.exceptions.DeprecatedPropertyException;
import impl.xmlio.exceptions.UnsupportedPropertyException;
import impl.xmlio.exceptions.UnsupportedTagException;
import lovelace.util.IteratorWrapper;

import java.util.Objects;
import java.util.regex.Pattern;

import static impl.xmlio.ISPReader.SP_NAMESPACE;

import java.util.List;
import java.util.Collections;

import lovelace.util.ThrowingConsumer;

import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;

import static impl.xmlio.ISPReader.FUTURE_TAGS;

import java.util.stream.StreamSupport;

/**
 * A superclass for YAXML reader classes to provide helper methods.
 */
abstract class YAAbstractReader<Item, Value> implements YAReader<Item, Value> {
	/**
	 * A parser for numeric data, so integers can contain commas.
	 */
	protected static final NumberFormat NUM_PARSER = NumberFormat.getIntegerInstance();

	/**
	 * Patterns to match XML metacharacters, and their quoted forms.
	 */
	protected static final List<Pair<Pattern, String>> QUOTING =
			List.of(Pair.with(Pattern.compile("&"), "&amp;"), Pair.with(Pattern.compile("<"), "&lt;"),
					Pair.with(Pattern.compile(">"), "&gt;"));

	protected static final Pair<Pattern, String> QUOTE_DOUBLE_QUOTE =
			Pair.with(Pattern.compile("\""), "&quot;");

	protected static final Pair<Pattern, String> QUOTE_SINGLE_QUOTE =
			Pair.with(Pattern.compile("'"), "&apos;");

	/**
	 * Whether the given tag is in a namespace we support.
	 */
	protected static boolean isSupportedNamespace(final QName tag) {
		return SP_NAMESPACE.equals(tag.getNamespaceURI()) ||
				XMLConstants.NULL_NS_URI.equals(tag.getNamespaceURI());
	}

	/**
	 * Require that an element be one of the specified tags.
	 */
	protected static void requireTag(final StartElement element, final QName parent, final String... tags)
			throws SPFormatException {
		if (!isSupportedNamespace(element.getName())) {
			throw UnwantedChildException.unexpectedNamespace(parent, element);
		}
		final String elementTag = element.getName().getLocalPart();
		if (Stream.of(tags).noneMatch(elementTag::equalsIgnoreCase)) {
			// While we'd like tests to exercise this, we're always careful to only call
			// readers when we know they support the tag ...
			throw UnwantedChildException.listingExpectedTags(parent, element, tags);
		}
	}

	/**
	 * Require that an element be one of the specified tags.
	 */
	protected static void requireTag(final StartElement element, final QName parent, final Collection<String> tags)
			throws SPFormatException {
		if (!isSupportedNamespace(element.getName())) {
			throw UnwantedChildException.unexpectedNamespace(parent, element);
		}
		final String elementTag = element.getName().getLocalPart();
		if (tags.stream().noneMatch(elementTag::equalsIgnoreCase)) {
			// While we'd like tests to exercise this, we're always careful to only call
			// readers when we know they support the tag ...
			throw UnwantedChildException.listingExpectedTags(parent, element, tags);
		}
	}

	/**
	 * Create a {@link QName} for the given tag in our namespace.
	 */
	private static QName qname(final String tag) {
		return new QName(SP_NAMESPACE, tag);
	}

	/**
	 * Get an attribute by name from the given tag, if it's there.
	 */
	private static @Nullable Attribute getAttributeByName(final StartElement element, final String parameter) {
		final Attribute retval = element.getAttributeByName(qname(parameter));
		if (Objects.isNull(retval)) {
			return element.getAttributeByName(new QName(parameter));
		} else {
			return retval;
		}
	}

	/**
	 * Read a parameter (aka attribute aka property) from an XML tag.
	 */
	protected static String getParameter(final StartElement element, final String param) throws SPFormatException {
		final Attribute attr = getAttributeByName(element, param);
		if (!Objects.isNull(attr)) {
			final String retval = attr.getValue();
			if (!Objects.isNull(retval)) {
				return retval;
			}
		}
		throw new MissingPropertyException(element, param);
	}

	/**
	 * Read a parameter (aka attribute aka property) from an XML tag.
	 */
	protected static String getParameter(final StartElement element, final String param, final String defaultValue) {
		final Attribute attr = getAttributeByName(element, param);
		if (!Objects.isNull(attr)) {
			final String retval = attr.getValue();
			if (!Objects.isNull(retval)) {
				return retval;
			}
		}
		return defaultValue;
	}

	/**
	 * Whether the given XML event is an end element matching the given tag.
	 */
	protected static boolean isMatchingEnd(final QName tag, final XMLEvent event) {
		return event instanceof final EndElement ee && tag.equals(ee.getName());
	}

	/**
	 * Whether the given tag has the given parameter.
	 */
	protected static boolean hasParameter(final StartElement element, final String param) {
		return !Objects.isNull(getAttributeByName(element, param));
	}

	/**
	 * Append the given number of tabs to the stream.
	 */
	protected static void indent(final ThrowingConsumer<String, IOException> ostream, final int tabs)
			throws IOException {
		ostream.accept(String.join("", Collections.nCopies(tabs, "\t")));
	}

	/**
	 * Replace XML meta-characters in a string with their equivalents.
	 */
	protected static String simpleQuote(final String text) {
		String retval = text;
		for (final Pair<Pattern, String> pair : QUOTING) {
			retval = pair.getValue0().matcher(retval).replaceAll(pair.getValue1());
		}
		return retval;
	}

	/**
	 * Replace XML meta-characters in a string with their equivalents.
	 *
	 * @param delimiter The character that will mark the end of the string
	 *                  as far as XML is concerned.  If a single or double quote, that
	 *                  character will be encoded every time it occurs in the string; if a
	 *                  greater-than sign or an equal sign, both types of quotes will be; if
	 *                  a less-than sign, neither will be.
	 */
	protected static String simpleQuote(final String text, final char delimiter) {
		String retval = simpleQuote(text);
		if (delimiter == '"' || delimiter == '>' || delimiter == '=') {
			retval = QUOTE_DOUBLE_QUOTE.getValue0().matcher(retval)
					.replaceAll(QUOTE_DOUBLE_QUOTE.getValue1());
		}
		if (delimiter == '\'' || delimiter == '>' || delimiter == '=') {
			retval = QUOTE_SINGLE_QUOTE.getValue0().matcher(retval)
					.replaceAll(QUOTE_SINGLE_QUOTE.getValue1());
		}
		return retval;
	}

	/**
	 * Write a property to XML.
	 */
	protected static void writeProperty(final ThrowingConsumer<String, IOException> ostream, final String name,
	                                    final String val)
			throws IOException {
		ostream.accept(" %s=\"%s\"".formatted(simpleQuote(name, '='), simpleQuote(val, '"')));
	}

	/**
	 * Write a property to XML.
	 */
	protected static void writeProperty(final ThrowingConsumer<String, IOException> ostream, final String name,
	                                    final int val)
			throws IOException {
		writeProperty(ostream, name, Integer.toString(val));
	}

	/**
	 * Write a property to XML only if its value is nonempty.
	 */
	protected static void writeNonemptyProperty(final ThrowingConsumer<String, IOException> ostream, final String name,
	                                            final String val)
			throws IOException {
		if (!val.isEmpty()) {
			writeProperty(ostream, name, val);
		}
	}

	/**
	 * Write the image property to XML if the object's image is nonempty
	 * and differs from the default.
	 */
	protected static void writeImageXML(final ThrowingConsumer<String, IOException> ostream, final HasImage obj)
			throws IOException {
		final String image = obj.getImage();
		if (!image.equals(obj.getDefaultImage())) {
			writeNonemptyProperty(ostream, "image", image);
		}
	}

	/**
	 * Parse an integer. We use {@link NumberFormat} rather than {@link
	 * Integer#parseInt} because we want to allow commas in the input.
	 *
	 * TODO: Inline this into the caller or pass in information that lets
	 * us throw a more meaningful exception, so we can get rid of
	 * SPMalformedInputException
	 *
	 * @param location The current location in the document
	 * @throws ParseException on non-numeric input
	 */
	protected static int parseInt(final String string, final Location location) throws ParseException {
		return NUM_PARSER.parse(string).intValue();
	}

	/**
	 * Read a parameter from XML whose value must be an integer.
	 */
	protected static int getIntegerParameter(final StartElement element, final String parameter)
			throws SPFormatException {
		final Attribute attr = getAttributeByName(element, parameter);
		if (!Objects.isNull(attr)) {
			final String retval = attr.getValue();
			if (!Objects.isNull(retval)) {
				try {
					return parseInt(retval, element.getLocation());
				} catch (final ParseException except) {
					throw new MissingPropertyException(element, parameter, except);
				}
			}
		}
		throw new MissingPropertyException(element, parameter);
	}

	/**
	 * Read a parameter from XML whose value must be an integer.
	 */
	protected static int getIntegerParameter(final StartElement element, final String parameter, final int defaultValue)
			throws SPFormatException {
		final Attribute attr = getAttributeByName(element, parameter);
		if (!Objects.isNull(attr)) {
			final String retval = attr.getValue();
			if (!Objects.isNull(retval)) {
				try {
					return parseInt(retval, element.getLocation());
				} catch (final ParseException except) {
					throw new MissingPropertyException(element, parameter, except);
				}
			}
		}
		return defaultValue;
	}

	/**
	 * Read a parameter from XML whose value can be an Integer or a Decimal.
	 */
	protected static Number getNumericParameter(final StartElement element, final String parameter)
			throws SPFormatException {
		if (hasParameter(element, parameter)) {
			final String paramString = getParameter(element, parameter);
			if (paramString.contains(".")) {
				try {
					return new BigDecimal(paramString);
				} catch (final NumberFormatException except) {
					throw new MissingPropertyException(element, parameter, except);
				}
			} else {
				try {
					return Integer.parseInt(paramString);
				} catch (final NumberFormatException except) {
					throw new MissingPropertyException(element, parameter, except);
				}
			}
		} else {
			throw new MissingPropertyException(element, parameter);
		}
	}

	/**
	 * Read a parameter from XML whose value can be an Integer or a Decimal.
	 */
	protected static Number getNumericParameter(final StartElement element, final String parameter,
												final Number defaultValue) throws SPFormatException {
		if (hasParameter(element, parameter)) {
			final String paramString = getParameter(element, parameter);
			if (paramString.contains(".")) {
				try {
					return new BigDecimal(paramString);
				} catch (final NumberFormatException except) {
					throw new MissingPropertyException(element, parameter, except);
				}
			} else {
				try {
					return Integer.parseInt(paramString);
				} catch (final NumberFormatException except) {
					throw new MissingPropertyException(element, parameter, except);
				}
			}
		} else {
			return defaultValue;
		}
	}

	/**
	 * Write the necessary number of tab characters and a tag. Does not
	 * write the right-bracket to close the tag. If `tabs` is 0, emit a
	 * namespace declaration as well.
	 */
	protected static void writeTag(final ThrowingConsumer<String, IOException> ostream, final String tag,
	                               final int tabs)
			throws IOException {
		indent(ostream, tabs);
		ostream.accept("<%s".formatted(simpleQuote(tag, '>')));
		if (tabs == 0) {
			ostream.accept(" xmlns=\"%s\"".formatted(SP_NAMESPACE));
		}
	}

	/**
	 * Close a tag with a right-bracket and add a newline.
	 */
	protected static void finishParentTag(final ThrowingConsumer<String, IOException> ostream) throws IOException {
		ostream.accept(">" + System.lineSeparator());
	}

	/**
	 * Close a 'leaf' tag and add a newline.
	 */
	protected static void closeLeafTag(final ThrowingConsumer<String, IOException> ostream) throws IOException {
		ostream.accept(" />" + System.lineSeparator());
	}

	/**
	 * Write a closing tag to the stream, optionally indented, and followed by a newline.
	 */
	protected static void closeTag(final ThrowingConsumer<String, IOException> ostream, final int tabs,
	                               final String tag) throws IOException {
		if (tabs > 0) {
			indent(ostream, tabs);
		}
		ostream.accept("</%s>%n".formatted(simpleQuote(tag, '>')));
	}

	/**
	 * Parse a Point from a tag's properties.
	 */
	protected static Point parsePoint(final StartElement element) throws SPFormatException {
		return new Point(getIntegerParameter(element, "row"),
				getIntegerParameter(element, "column"));
	}

	/**
	 * The Warning instance to use.
	 */
	private final Warning warner;
	/**
	 * The factory for ID numbers
	 */
	private final IDRegistrar idf;

	protected YAAbstractReader(final Warning warning, final IDRegistrar idRegistrar) {
		warner = warning;
		idf = idRegistrar;
	}

	/**
	 * Warn about a not-yet-(fully-)supported tag.
	 */
	protected final void warnFutureTag(final StartElement tag) {
		warner.handle(UnsupportedTagException.future(tag));
	}

	/**
	 * Advance the stream until we hit an end element matching the given
	 * name, but object to any start elements.
	 */
	protected final void spinUntilEnd(final QName tag, final Iterable<XMLEvent> reader, final String... futureTags)
			throws SPFormatException {
		for (final XMLEvent event : reader) {
			if (event instanceof final StartElement se && isSupportedNamespace(se.getName())) {
				if (FUTURE_TAGS.stream().anyMatch(se.getName().getLocalPart()::equalsIgnoreCase)) {
					warner.handle(new UnwantedChildException(tag, se));
				} else {
					throw new UnwantedChildException(tag, se);
				}
			} else if (isMatchingEnd(tag, event)) {
				break;
			}
		}
	}

	/**
	 * Read a parameter from XML whose value must be a boolean.
	 *
	 * Note that unlike in Ceylon, we can't use {@link
	 * Boolean#parseBoolean} because it doesn't object to non-boolean
	 * input, just returns false for everything but "true".
	 */
	protected static boolean getBooleanParameter(final StartElement element, final String parameter)
			throws SPFormatException {
		final Attribute attr = getAttributeByName(element, parameter);
		if (!Objects.isNull(attr)) {
			final String val = attr.getValue();
			if ("true".equalsIgnoreCase(val)) {
				return true;
			} else if ("false".equalsIgnoreCase(val)) {
				return false;
			} else {
				throw new MissingPropertyException(element, parameter,
						new IllegalArgumentException("Boolean can only be true or false"));
			}
		}
		throw new MissingPropertyException(element, parameter);
	}

	/**
	 * Read a parameter from XML whose value must be a boolean.
	 */
	protected final boolean getBooleanParameter(final StartElement element, final String parameter,
	                                            final boolean defaultValue) {
		final Attribute attr = getAttributeByName(element, parameter);
		if (!Objects.isNull(attr)) {
			final String val = attr.getValue();
			if (!Objects.isNull(val) && !val.isEmpty()) { // TODO: Convert (inverted) to 'else' below
				if ("true".equalsIgnoreCase(val)) {
					return true;
				} else if ("false".equalsIgnoreCase(val)) {
					return false;
				} else {
					warner.handle(new MissingPropertyException(element, parameter,
							new IllegalArgumentException(
									"Boolean can only be true or false")));
				}
			}
		}
		return defaultValue;
	}

	/**
	 * Require that a parameter be present and non-empty.
	 *
	 * TODO: Try to avoid Boolean parameters
	 */
	protected final void requireNonEmptyParameter(final StartElement element, final String parameter,
	                                              final boolean mandatory) throws SPFormatException {
		if (getParameter(element, parameter, "").isEmpty()) {
			final SPFormatException except = new MissingPropertyException(element, parameter);
			if (mandatory) {
				throw except;
			} else {
				warner.handle(except);
			}
		}
	}

	/**
	 * Register the specified ID number, noting that it came from the
	 * specified location, and return it.
	 */
	protected final int registerID(final Integer id, final Location location) {
		return idf.register(id, warner, location);
	}

	/**
	 * If the specified tag has an ID as a property, return it; otherwise,
	 * warn about its absence and generate one.
	 */
	protected final int getOrGenerateID(final StartElement element) throws SPFormatException {
		if (hasParameter(element, "id")) {
			return registerID(getIntegerParameter(element, "id"), element.getLocation());
		} else {
			warner.handle(new MissingPropertyException(element, "id"));
			return idf.createID();
		}
	}

	/**
	 * Get a parameter from an element in its preferred form, if present,
	 * or in a deprecated form, in which case fire a warning.
	 */
	protected final String getParamWithDeprecatedForm(final StartElement element, final String preferred,
	                                                  final String deprecated) throws SPFormatException {
		final Attribute preferredProperty = getAttributeByName(element, preferred);
		if (!Objects.isNull(preferredProperty)) {
			final String retval = preferredProperty.getValue();
			if (!Objects.isNull(retval)) {
				return retval;
			}
		}
		final Attribute deprecatedProperty = getAttributeByName(element, deprecated);
		if (!Objects.isNull(deprecatedProperty)) {
			final String retval = deprecatedProperty.getValue();
			if (!Objects.isNull(retval)) {
				warner.handle(new DeprecatedPropertyException(element, deprecated,
						preferred));
				return retval;
			}
		}
		throw new MissingPropertyException(element, preferred);
	}

	/**
	 * Warn if any unsupported attribute is on this tag.
	 */
	protected final void expectAttributes(final StartElement element, final String... attributes) {
		final Set<String> local = Stream.of(attributes).map(String::toLowerCase)
				.collect(Collectors.toSet());
		for (final String attribute : StreamSupport.stream(
						new IteratorWrapper<>(
								element.getAttributes()).spliterator(), true)
				.map(Attribute::getName).filter(YAAbstractReader::isSupportedNamespace)
				.map(QName::getLocalPart).map(String::toLowerCase).toList()) {
			if (!local.contains(attribute)) {
				warner.handle(new UnsupportedPropertyException(element, attribute));
			}
		}
	}
}
