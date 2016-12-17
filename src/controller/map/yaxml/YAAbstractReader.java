package controller.map.yaxml;

import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.SPMalformedInputException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasImage;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.EqualsAny;
import util.LineEnd;
import util.Pair;
import util.Warning;

import static java.lang.String.format;

/**
 * A superclass to provide helper methods.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <T> the type of things the subclass of this can read and write
 * @author Jonathan Lovelace
 */
public abstract class YAAbstractReader<@NonNull T> implements YAReader<@NonNull T> {
	/**
	 * The Warning instance to use.
	 */
	private final Warning warner;
	/**
	 * The factory for ID numbers.
	 */
	private final IDRegistrar idf;
	/**
	 * A parser for numeric data.
	 */
	private static final NumberFormat NUM_PARSER = NumberFormat.getIntegerInstance();
	/**
	 * Patterns to match XML meta-characters, and their quoted forms.
	 */
	private static final List<Pair<Pattern, String>> QUOTING =
			Arrays.asList(Pair.of(Pattern.compile("&"), "&amp;"),
					Pair.of(Pattern.compile("<"), "&lt;"),
					Pair.of(Pattern.compile(">"), "&gt;"),
					Pair.of(Pattern.compile("\""), "&quot;"),
					Pair.of(Pattern.compile("'"), "&apos;"));

	/**
	 * @param warning the Warning instance to use
	 * @param idRegistrar the factory for ID numbers.
	 */
	protected YAAbstractReader(final Warning warning, final IDRegistrar idRegistrar) {
		warner = warning;
		idf = idRegistrar;
	}

	/**
	 * Require that an element be one of the specified tags.
	 *
	 * @param element the element to check
	 * @param parent  the parent tag
	 * @param tags    the tags we accept here
	 * @throws SPFormatException on something other than one of the tags we accept in a
	 *                           namespace we expect.
	 */
	protected static void requireTag(final StartElement element,
									 final QName parent, final String... tags)
			throws SPFormatException {
		if (!isSupportedNamespace(element.getName())) {
			throw new UnwantedChildException(parent, element,
													new IllegalArgumentException
															("Unrecognized namespace"));
		}
		final String localName = element.getName().getLocalPart();
		final int line = element.getLocation().getLineNumber();
		if (!EqualsAny.equalsAny(localName, tags)) {
			// While we'd like tests to exercise this, we're always careful
			// to only call readers when we know they support the tag ...
			final String message = Stream.concat(Stream.of(
					format("Unexpected tag %s on line %d, expected one of these: ",
							localName, Integer.valueOf(line))), Stream.of(tags))
										   .collect(Collectors.joining(", "));
			throw new UnwantedChildException(parent, element,
													new IllegalArgumentException(message));
		}
	}

	/**
	 * Get a parameter from the XML.
	 *
	 * @param element the current tag
	 * @param param   the parameter to get
	 * @return the value for that parameter
	 * @throws SPFormatException if the tag doesn't have that parameter.
	 */
	protected static String getParameter(final StartElement element,
										 final String param) throws SPFormatException {
		return Optional.ofNullable(getAttributeByName(element, param))
					   .map(Attribute::getValue)
					   .orElseThrow(() -> new MissingPropertyException(element, param));
	}

	/**
	 * Get a parameter from the XML.
	 *
	 * @param element      the current tag
	 * @param param        the parameter to get
	 * @param defaultValue the value to return if the tag doesn't have that parameter
	 * @return the value for that parameter
	 */
	protected static String getParameter(final StartElement element,
										 final String param, final String defaultValue) {
		return Optional.ofNullable(getAttributeByName(element, param))
					   .map(Attribute::getValue).orElse(defaultValue);
	}

	/**
	 * Require a non-empty parameter.
	 *
	 * @param element   the current tag
	 * @param param     the parameter to require
	 * @param mandatory whether this is a requirement, or merely a recommendation.
	 * @throws SPFormatException if mandatory and missing
	 */
	protected void requireNonEmptyParameter(final StartElement element,
												   final String param,
												   final boolean mandatory)
			throws SPFormatException {
		if (getParameter(element, param, "").isEmpty()) {
			final SPFormatException except = new MissingPropertyException(element,
																				 param);
			if (mandatory) {
				throw except;
			} else {
				warner.warn(except);
			}
		}
	}

	/**
	 * Whether the given tag is in a namespace we support.
	 * @param tag a tag
	 * @return whether it's in a namespace we support.
	 */
	protected static boolean isSupportedNamespace(final QName tag) {
		return EqualsAny.equalsAny(tag.getNamespaceURI(), ISPReader.NAMESPACE,
				XMLConstants.NULL_NS_URI);
	}

	/**
	 * Move along the stream until we hit an end element matching the start-element we're
	 * parsing, but object to any start elements.
	 *
	 * @param tag    the tag we're currently parsing
	 * @param reader the XML stream we're reading from
	 * @throws SPFormatException on unwanted child
	 */
	protected static void spinUntilEnd(final QName tag,
									   final Iterable<XMLEvent> reader)
			throws SPFormatException {
		for (final XMLEvent event : reader) {
			if (event.isStartElement() &&
						isSupportedNamespace(event.asStartElement().getName())) {
				throw new UnwantedChildException(tag, event.asStartElement());
			} else if (isMatchingEnd(tag, event)) {
				break;
			}
		}
	}

	/**
	 * Whether the given XML event is an end element matching the given tag.
	 * @param tag the tag we're looking for
	 * @param event an XML event
	 * @return true iff event is an EndElement whose name matches tag.
	 */
	protected static boolean isMatchingEnd(final QName tag, final XMLEvent event) {
		return event.isEndElement()
					   && tag.equals(event.asEndElement().getName());
	}

	/**
	 * If the specified tag has an ID as a property, return it; otherwise warn about its
	 * absence and generate one.
	 *
	 * @param element   the tag we're working with
	 * @return the ID the tag has if it has one, or otherwise a generated one.
	 * @throws SPFormatException on SP format problems reading the attribute
	 */
	protected int getOrGenerateID(final StartElement element)
			throws SPFormatException {
		if (hasParameter(element, "id")) {
			try {
				return idf.register(warner, NumberFormat.getIntegerInstance()
														  .parse(getParameter(element,
																  "id"))
														  .intValue());
			} catch (final NumberFormatException | ParseException except) {
				throw new MissingPropertyException(element, "id", except);
			}
		} else {
			warner.warn(new MissingPropertyException(element, "id"));
			return idf.createID();
		}
	}

	/**
	 * Get an attribute by name from the given element.
	 * @param element the current tag
	 * @param param   the parameter we want
	 * @return it if it's present in either the default namespace or our namespace, or
	 * null if not present
	 */
	@Nullable
	protected static Attribute getAttributeByName(final StartElement element,
												  final String param) {
		final Attribute retval =
				element.getAttributeByName(qname(param));
		if (retval == null) {
			return element.getAttributeByName(new QName(param));
		} else {
			return retval;
		}
	}
	/**
	 * @param tag a tag
	 * @return a QName combining it with our namespace
	 */
	protected static QName qname(final String tag) {
		return new QName(ISPReader.NAMESPACE, tag);
	}
	/**
	 * Whether the given element has the given parameter.
	 * @param element the current tag
	 * @param param   the parameter we want
	 * @return whether the tag has that parameter
	 */
	protected static boolean hasParameter(final StartElement element,
										  final String param) {
		return getAttributeByName(element, param) != null;
	}

	/**
	 * Get a parameter from an element in its preferred form, if present, or in its
	 * deprecated form, in which case fire a warning.
	 * @param element    the current tag
	 * @param preferred  the preferred name of the parameter
	 * @param deprecated the deprecated name of the parameter
	 * @return the value of the parameter, gotten from the preferred form if it has it,
	 * and from the deprecated form if the preferred form isn't there but it is.
	 * @throws SPFormatException if the element doesn't have that attribute
	 */
	protected String getParamWithDeprecatedForm(final StartElement element,
													   final String preferred,
													   final String deprecated)
			throws SPFormatException {
		final Optional<String> prefProp =
				Optional.ofNullable(getAttributeByName(element, preferred))
						.map(Attribute::getValue);
		Optional<String> deprecatedProp =
				Optional.ofNullable(getAttributeByName(element, deprecated))
						.map(Attribute::getValue);
		final MissingPropertyException exception =
				new MissingPropertyException(element, preferred);
		if (prefProp.isPresent()) {
			return prefProp.get();
		} else if (deprecatedProp.isPresent()) {
			warner.warn(new DeprecatedPropertyException(element, deprecated, preferred));
			return deprecatedProp.get();
		} else {
			throw exception;
		}
	}

	/**
	 * Append the given number of tabs to the stream.
	 * @param ostream the stream to write the tabs to
	 * @param tabs    a non-negative integer: how many tabs to add to the stream
	 * @throws IOException on I/O error writing to ostream
	 */
	protected static void indent(final Appendable ostream, final int tabs)
			throws IOException {
		for (int i = 0; i < tabs; i++) {
			ostream.append('\t');
		}
	}

	/**
	 * Write the image property to XML if the object's image is nonempty and not the
	 * default.
	 * @param obj an object being written out that might have a custom image
	 * @param ostream the stream to write to
	 * @throws IOException on I/O error while writing
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	protected static void writeImageXML(final Appendable ostream, final HasImage obj)
			throws IOException {
		final String image = obj.getImage();
		if (!image.equals(obj.getDefaultImage())) {
			writeNonemptyProperty(ostream, "image", image);
		}
	}
	/**
	 * Write a property to XML only if its value is nonempty.
	 * @param ostream the stream to write to
	 * @param name the name of the property to write if its value is nonempty
	 * @param value the value to write if nonempty
	 * @throws IOException on I/O error
	 */
	protected static void writeNonemptyProperty(final Appendable ostream,
												final String name, final String value)
			throws IOException {
		if (!value.isEmpty()) {
			writeProperty(ostream, name, value);
		}
	}

	/**
	 * Parse an integer.
	 *
	 * @param str      the text to parse
	 * @param location the current location in the document
	 * @return the result of parsing the text
	 * @throws SPFormatException if the string is non-numeric or otherwise malformed
	 */
	private static int parseInt(final String str, final Location location)
			throws SPFormatException {
		try {
			return NUM_PARSER.parse(str).intValue();
		} catch (final ParseException e) {
			throw new SPMalformedInputException(location, e);
		}
	}

	/**
	 * Parse an integer parameter.
	 *
	 * @param tag       the tag to get the parameter from
	 * @param parameter the name of the parameter
	 * @return the result of parsing the text
	 * @throws SPFormatException if the tag doesn't have that parameter or if its
	 * value is
	 *                           non-numeric or otherwise malformed
	 */
	protected static int getIntegerParameter(final StartElement tag,
											 final String parameter)
			throws SPFormatException {
		return parseInt(getParameter(tag, parameter), tag.getLocation());
	}

	/**
	 * Parse an integer parameter.
	 *
	 * @param tag          the tag to get the parameter from
	 * @param parameter    the name of the parameter
	 * @param defaultValue the default value to return if the parameter is missing
	 * @return the result of parsing the text
	 * @throws SPFormatException if the parameter's value is non-numeric or otherwise
	 *                           malformed
	 */
	protected static int getIntegerParameter(final StartElement tag,
											 final String parameter,
											 final int defaultValue)
			throws SPFormatException {
		final Optional<String> attr =
				Optional.ofNullable(getAttributeByName(tag, parameter))
											  .map(Attribute::getValue)
											  .filter(str -> !str.isEmpty());
		if (attr.isPresent()) {
			return parseInt(attr.get(), tag.getLocation());
		} else {
			return defaultValue;
		}
	}

	/**
	 * Write the necessary number of tab characters and a tag. Does not write the right
	 * bracket to close the tag.
	 *
	 * @param ostream the stream to write to
	 * @param tag     the tag to write
	 * @param indent  the indentation level
	 * @throws IOException on I/O error writing to stream
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	protected static void writeTag(final Appendable ostream, final String tag,
								   final int indent) throws IOException {
		indent(ostream, indent);
		ostream.append('<');
		ostream.append(simpleQuote(tag));
		if (indent == 0) {
			ostream.append(" xmlns=\"");
			ostream.append(ISPReader.NAMESPACE);
			ostream.append("\"");
		}
	}

	/**
	 * Write an XML property to the stream.
	 *
	 * @param ostream the stream to write to
	 * @param name	  the name of the property to write
	 * @param value	  the value to write
	 * @throws IOException on I/O error writing to stream
	 */
	protected static void writeProperty(final Appendable ostream, final String name,
										final String value) throws IOException {
		ostream.append(' ');
		ostream.append(simpleQuote(name));
		ostream.append("=\"");
		ostream.append(simpleQuote(value));
		ostream.append('"');
	}

	/**
	 * Write '>\n' to the stream.
	 * @param ostream the stream to write to.
	 * @throws IOException on I/O error while writing
	 */
	protected static void finishParentTag(final Appendable ostream) throws IOException {
		ostream.append('>');
		ostream.append(LineEnd.LINE_SEP);
	}

	/**
	 * Write ' />\n' to the stream.
	 * @param ostream the stream to write to.
	 * @throws IOException on I/O error while writing
	 */
	protected static void closeLeafTag(final Appendable ostream) throws IOException {
		ostream.append(" />");
		ostream.append(LineEnd.LINE_SEP);
	}

	/**
	 * Write a closing tag to the stream, followed by a newline.
	 * @param ostream the stream to write to
	 * @param indent how much, if any, to indent the closing tag
	 * @param tag the tag to close
	 * @throws IOException on I/O error while writing.
	 */
	protected static void closeTag(final Appendable ostream, final int indent,
								   final String tag) throws IOException {
		if (indent > 0) {
			indent(ostream, indent);
		}
		ostream.append("</");
		ostream.append(simpleQuote(tag));
		ostream.append('>');
		ostream.append(LineEnd.LINE_SEP);
	}

	/**
	 * Replace XML meta-characters in a string with their equivalents.
	 * @param text some text
	 * @return it, with all XML meta-characters replaced with their equivalents
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	protected static String simpleQuote(final String text) {
		String retval = text;
		for (final Pair<Pattern, String> pair : QUOTING) {
			final Matcher matcher = pair.first().matcher(retval);
			retval = matcher.replaceAll(pair.second());
		}
		return retval;
	}
}
