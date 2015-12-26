package controller.map.cxml;

import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.SPMalformedInputException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasImage;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.EqualsAny;
import util.NullCleaner;
import util.Warning;

import static java.lang.String.format;

/**
 * A superclass to provide helper methods.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @param <T> a type parameter, since we now "implement" the interface
 * @author Jonathan Lovelace
 */
public abstract class AbstractCompactReader<@NonNull T>
		implements CompactReader<@NonNull T> {
	/**
	 * The string to use instead of the tag in exceptions when the tag is null.
	 */
	private static final String NULL_TAG = "a null tag";

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
	 * @param tags    the tags we accept here
	 */
	protected static void requireTag(final StartElement element,
									 final String... tags) {
		final String localName = element.getName().getLocalPart();
		final int line = element.getLocation().getLineNumber();
		if (localName == null) {
			throw new IllegalArgumentException(Stream.concat(
					Stream.of(
							format("Null tag on line %d, expected one of the " +
										   "following: ",
									Integer.valueOf(line))), Stream.of(tags))
													   .collect(
															   Collectors.joining(", ")));
		} else if (!EqualsAny.equalsAny(localName, tags)) {
			throw new IllegalArgumentException(Stream.concat(Stream.of(format(
					"Unexpected tag %s on line %d, expected one of the following: ",
					localName, Integer.valueOf(line))), Stream.of(tags))
													   .collect(Collectors.joining(", "))
			);
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
		final Attribute attr = element.getAttributeByName(new QName(param));
		final String local = tagOrNull(element.getName().getLocalPart());
		if (attr == null) {
			throw new MissingPropertyException(local, param, element.getLocation());
		} else {
			final String value = attr.getValue();
			if (value == null) {
				throw new MissingPropertyException(local, param, element.getLocation());
			} else {
				return value;
			}
		}
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
		final Attribute attr = element.getAttributeByName(new QName(param));
		if (attr == null) {
			return defaultValue; // NOPMD
		} else {
			return NullCleaner.valueOrDefault(attr.getValue(), defaultValue);
		}
	}

	/**
	 * Require a non-empty parameter.
	 *
	 * @param element   the current tag
	 * @param param     the parameter to require
	 * @param mandatory whether this is a requirement, or merely a recommendation.
	 * @param warner    the Warning instance to use for the warning.
	 * @throws SPFormatException if mandatory and missing
	 */
	protected static void requireNonEmptyParameter(final StartElement element,
												   final String param,
												   final boolean mandatory,
												   final Warning warner)
			throws SPFormatException {
		if (getParameter(element, param, "").isEmpty()) {
			final String local = tagOrNull(element.getName().getLocalPart());
			final SPFormatException except =
					new MissingPropertyException(local, param, element.getLocation());
			if (mandatory) {
				throw except;
			} else {
				warner.warn(except);
			}
		}
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
			if (event.isStartElement()) {
				throw new UnwantedChildException(tagOrNull(tag.getLocalPart()),
						                                tagOrNull(
						event.asStartElement().getName().getLocalPart()),
						                                event.getLocation());
			} else if (event.isEndElement()
							   && tag.equals(event.asEndElement().getName())) {
				break;
			}
		}
	}

	/**
	 * If the specified tag has an ID as a property, return it; otherwise warn about its
	 * absence and generate one.
	 *
	 * @param element   the tag we're working with
	 * @param warner    the Warning instance to send the warning on if the tag doesn't
	 *                  specify an ID
	 * @param idFactory the factory to register an existing ID with or get a new one from
	 * @return the ID the tag has if it has one, or otherwise a generated one.
	 * @throws SPFormatException on SP format problems reading the attribute
	 */
	protected static int getOrGenerateID(final StartElement element,
										 final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		if (hasParameter(element, "id")) {
			try {
				return idFactory.register(NumberFormat.getIntegerInstance()
												  .parse(getParameter(element, "id"))
												  .intValue());
			} catch (final NumberFormatException | ParseException except) {
				throw new MissingPropertyException(tagOrNull(
						element.getName().getLocalPart()), "id", element.getLocation(),
						                                  except);
			}
		} else {
			final String tag = element.getName().getLocalPart();
			warner.warn(new MissingPropertyException(tagOrNull(tag), "id",
					                                        element.getLocation()));
			return idFactory.createID();
		}
	}

	/**
	 * @param element the current tag
	 * @param param   the parameter we want
	 * @return whether the tag has that parameter
	 */
	protected static boolean hasParameter(final StartElement element,
										  final String param) {
		return element.getAttributeByName(new QName(param)) != null;
	}

	/**
	 * @param element    the current tag
	 * @param preferred  the preferred name of the parameter
	 * @param deprecated the deprecated name of the parameter
	 * @param warner     the warning instance to use
	 * @return the value of the parameter, gotten from the preferred form if it has it,
	 * and from the deprecated form if the preferred form isn't there but it is.
	 * @throws SPFormatException if the element doesn't have that attribute
	 */
	protected static String getParamWithDeprecatedForm(final StartElement element,
													   final String preferred,
													   final String deprecated,
													   final Warning warner)
			throws SPFormatException {
		final Attribute prefProp = element.getAttributeByName(new QName(preferred));
		final Attribute deprProp = element.getAttributeByName(new QName(deprecated));
		final String local = tagOrNull(element.getName().getLocalPart());
		final MissingPropertyException exception =
				new MissingPropertyException(local, preferred, element.getLocation());
		if ((prefProp == null) && (deprProp == null)) {
			throw exception;
		} else if (prefProp == null) {
			warner.warn(new DeprecatedPropertyException(local, deprecated, preferred,
					                                           element.getLocation()));
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
	 * @param ostream the stream to write the tabs to
	 * @param tabs a nonnegative integer: how many tabs to add to the stream
	 * @throws IOException on I/O error writing to ostream
	 */
	protected static void indent(final Appendable ostream, final int tabs) throws IOException {
		for (int i = 0; i < tabs; i++) {
			ostream.append('\t');
		}
	}

	/**
	 * @param obj an object being written out that might have a custom image
	 * @return the XML for the image if it does, or the empty string if not
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	protected static String imageXML(final HasImage obj) {
		final String image = obj.getImage();
		if (image.isEmpty() || image.equals(obj.getDefaultImage())) {
			return ""; // NOPMD
		} else {
			return " image=\"" + image + '"';
		}
	}

	/**
	 * @param tag the name of a tag, which may be null
	 * @return "a null tag" if null, or the tag name if not
	 */
	private static String tagOrNull(@Nullable final String tag) {
		return NullCleaner.valueOrDefault(tag, NULL_TAG);
	}

	/**
	 * A parser for numeric data.
	 */
	private static final NumberFormat NUM_PARSER = NullCleaner
														   .assertNotNull(NumberFormat
																				  .getIntegerInstance());


	/**
	 * Parse an integer.
	 *
	 * @param str  the text to parse
	 * @param location the current location in the document
	 * @return the result of parsing the text
	 * @throws SPFormatException if the string is nonnumeric or otherwise malformed
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
	 *                           nonnumeric or otherwise malformed
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
	 * @throws SPFormatException if the parameter's value is nonnumeric or otherwise
	 *                           malformed
	 */
	protected static int getIntegerParameter(final StartElement tag,
											 final String parameter,
											 final int defaultValue)
			throws SPFormatException {
		final Attribute attr = tag.getAttributeByName(new QName(parameter));
		if (attr == null) {
			return defaultValue; // NOPMD
		}
		final String val = attr.getValue();
		if ((val == null) || val.isEmpty()) {
			return defaultValue;
		} else {
			return parseInt(val, tag.getLocation());
		}
	}
}
