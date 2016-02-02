package controller.map.readerng;

import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.SPMalformedInputException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasImage;
import model.map.IMutablePlayerCollection;
import model.map.IPlayerCollection;
import model.map.Player;
import org.eclipse.jdt.annotation.NonNull;
import util.NullCleaner;
import util.Warning;

/**
 * An interface for *stateless* per-class XML readers/writers.
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
 * @param <T> The type of object the reader knows how to read
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public interface INodeHandler<@NonNull T> {
	/**
	 * @return the class this can write to a writer.
	 */
	Class<T> writtenClass();

	/**
	 * @return a list of the tags the reader can handle.
	 */
	List<String> understands();

	/**
	 * Parse an instance of the type from XML.
	 *
	 * @param element   the eleent to start parsing with
	 * @param stream    to get more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the produced type
	 * @throws SPFormatException on map format problems
	 */
	T parse(StartElement element, Iterable<XMLEvent> stream,
	        IMutablePlayerCollection players, Warning warner, IDFactory idFactory)
			throws SPFormatException;

	/**
	 * Create an intermediate representation to convert to XML.
	 *
	 * @param <S> the type of the object---it can be a subclass, to make the adapter
	 *            work.
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	<@NonNull S extends @NonNull T> SPIntermediateRepresentation write(@NonNull S obj);
	/**
	 * @param startElement a tag
	 * @param attribute    the attribute we want
	 * @return the value of that attribute.
	 * @throws SPFormatException if the element doesn't have that attribute
	 */
	default String getAttribute(final StartElement startElement,
	                                  final String attribute) throws SPFormatException {
		final Attribute attr = startElement.getAttributeByName(new QName(
				                                                                attribute));
		if ((attr == null) || (attr.getValue() == null)) {
			throw new MissingPropertyException(startElement, attribute);
		}
		return NullCleaner.assertNotNull(attr.getValue());
	}

	/**
	 * A variant of getAttribute() that returns a specified default value if the
	 * attribute
	 * is missing, instead of throwing an exception.
	 *
	 * @param elem         the element
	 * @param attr         the attribute we want
	 * @param defaultValue the default value if the element doesn't have the attribute
	 * @return the value of attribute if it exists, or the default
	 */
	default String getAttribute(final StartElement elem,
	                                  final String attr, final String defaultValue) {
		final Attribute value = elem.getAttributeByName(new QName(attr));
		if (value == null) {
			return defaultValue; // NOPMD
		} else {
			return NullCleaner.assertNotNull(value.getValue());
		}
	}

	/**
	 * @param element    the element
	 * @param preferred  the preferred name of the attribute
	 * @param deprecated the deprecated name of the attribute * @throws SPFormatException
	 *                   if the element doesn't have that attribute
	 * @param warner     the warning instance to use
	 * @return the value of the attribute, gotten from the preferred form if it has it,
	 * and from the deprecated form if the preferred form isn't there but it is.
	 * @throws SPFormatException if the element doesn't have that attribute
	 */
	default String getAttributeWithDeprecatedForm(
			                                                   final StartElement
					                                                   element,
			                                                   final String preferred,
			                                                   final String deprecated,
			                                                   final Warning warner)
			throws SPFormatException {
		final Attribute prefAttr = element.getAttributeByName(new QName(
				                                                               preferred));
		final Attribute deprAttr = element.getAttributeByName(new QName(
				                                                               deprecated));
		if ((prefAttr == null) && (deprAttr == null)) {
			throw new MissingPropertyException(element, preferred);
		} else if (prefAttr == null) {
			warner.warn(new DeprecatedPropertyException(element, deprecated, preferred));
			return NullCleaner.assertNotNull(deprAttr.getValue()); // NOPMD
		} else {
			return NullCleaner.assertNotNull(prefAttr.getValue());
		}
	}

	/**
	 * @param elem an element
	 * @param attr the attribute we want
	 * @return whether the element has that attribute
	 */
	default boolean hasAttribute(final StartElement elem,
	                                   final String attr) {
		return elem.getAttributeByName(new QName(attr)) != null;
	}

	/**
	 * Move along the stream until we hit an end element, but object to any start
	 * elements.
	 *
	 * @param tag    what kind of tag we're in (for the error message)
	 * @param reader the XML stream we're reading from
	 * @throws SPFormatException on unwanted child
	 */
	default void spinUntilEnd(final QName tag,
	                                final Iterable<XMLEvent> reader)
			throws SPFormatException {
		for (final XMLEvent event : reader) {
			if (event.isStartElement()) {
				throw new UnwantedChildException(tag, event.asStartElement());
			} else if (event.isEndElement()
					           && tag.equals(event.asEndElement().getName())) {
				break;
			}
		}
	}

	/**
	 * Throw an exception (or warn) if the specified parameter is missing or empty.
	 *
	 * @param element   the current element
	 * @param parameter the parameter to check
	 * @param mandatory whether we should throw or just warn
	 * @param warner    the Warning instance to use if not mandatory
	 * @throws SPFormatException if the parameter is mandatory and missing
	 */
	default void requireNonEmptyParameter(final StartElement element,
	                                            final String parameter,
	                                            final boolean mandatory,
	                                            final Warning warner)
			throws SPFormatException {
		if (getAttribute(element, parameter, "").isEmpty()) {
			final SPFormatException except =
					new MissingPropertyException(element, parameter);
			if (mandatory) {
				throw except;
			} else {
				warner.warn(except);
			}
		}
	}

	/**
	 * If the specified tag has an "owner" property, return the player it indicates;
	 * otherwise warn about its absence and return the "independent" player from the
	 * player collection.
	 *
	 * @param element the tag we're working with
	 * @param warner  the Warning instance to send the warning on
	 * @param players the collection of players to refer to
	 * @return a suitable player
	 * @throws SPFormatException on SP format problems reading the attribute.
	 */
	default Player getPlayerOrIndependent(final StartElement element,
	                                            final Warning warner,
	                                            final IPlayerCollection players)
			throws SPFormatException {
		final Player retval; // NOPMD
		if (hasAttribute(element, "owner")) {
			retval = players.getPlayer(getIntegerAttribute(element, "owner"));
		} else {
			warner.warn(new MissingPropertyException(element, "owner"));
			retval = players.getIndependent();
		}
		return retval;
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
	default int getOrGenerateID(final StartElement element,
	                            final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		final int retval; // NOPMD
		if (hasAttribute(element, "id")) {
			retval = idFactory.register(getIntegerAttribute(element, "id"));
		} else {
			warner.warn(new MissingPropertyException(element, "id"));
			retval = idFactory.createID();
		}
		return retval;
	}

	/**
	 * @param startElement a tag
	 * @param attribute    the attribute we want
	 * @return the value of that attribute.
	 * @throws SPFormatException if the element doesn't have that attribute
	 */
	default int getIntegerAttribute(final StartElement startElement,
	                                      final String attribute) throws SPFormatException {
		final Attribute attr = startElement.getAttributeByName(new QName(
				                                                                attribute));
		if ((attr == null) || (attr.getValue() == null)) {
			throw new MissingPropertyException(startElement, attribute);
		}
		try {
			return NullCleaner.assertNotNull(NumberFormat.getIntegerInstance())
					       .parse(attr.getValue()).intValue();
		} catch (final ParseException e) {
			throw new SPMalformedInputException(startElement.getLocation(), e);
		}
	}
	/**
	 * A variant of getAttribute() that returns a specified default value if the
	 * attribute
	 * is missing, instead of throwing an exception.
	 *
	 * @param elem         the element
	 * @param attr         the attribute we want
	 * @param defaultValue the default value if the element doesn't have the attribute
	 * @return the value of attribute if it exists, or the default
	 */
	default int getIntegerAttribute(final StartElement elem, final String attr,
	                                      final int defaultValue)
			throws SPFormatException {
		final Attribute value = elem.getAttributeByName(new QName(attr));
		if ((value == null) || value.getValue().isEmpty()) {
			return defaultValue; // NOPMD
		} else {
			try {
				return NullCleaner.assertNotNull(NumberFormat.getIntegerInstance())
						       .parse(value.getValue()).intValue();
			} catch (final ParseException e) {
				throw new SPMalformedInputException(elem.getLocation(), e);
			}
		}
	}

	/**
	 * @param stream a source of XMLEvents
	 * @return the file currently being read from if it's an {@link IncludingIterator}, or
	 * the empty string otherwise.
	 */
	default String getFile(final Iterable<XMLEvent> stream) {
		if (stream.iterator() instanceof IncludingIterator) {
			return ((IncludingIterator) stream.iterator()).getFile(); // NOPMD
		} else {
			return "";
		}
	}

	/**
	 * If there is an image attribute on the element, add that to the object being
	 * constructed. If not, do nothing.
	 *
	 * @param element the XML element being read
	 * @param obj     the object being constructed.
	 * @throws SPFormatException on SP format error
	 */
	default void addImage(final StartElement element, final HasImage obj)
			throws SPFormatException {
		if (hasAttribute(element, "image")) {
			obj.setImage(getAttribute(element, "image"));
		}
	}
	// FIXME: Add and use parsePoint(String rowTag, String colTag)
}
