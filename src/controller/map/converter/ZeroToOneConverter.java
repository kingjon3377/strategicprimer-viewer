package controller.map.converter;

import controller.map.iointerfaces.ISPReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.NoSuchFileException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.eclipse.jdt.annotation.Nullable;
import util.IteratorWrapper;
import util.LineEnd;
import util.TypesafeLogger;
import view.util.SystemOut;

/**
 * A class to convert a version-0 map to a version-1 map. As no reader currently in the
 * tree supports reading version 0 files, we have to handle that ourselves, but
 * fortunately not much requires changing.
 *
 * We ignore namespaces, as I'm not sure quite how to handle them.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ZeroToOneConverter {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			TypesafeLogger.getLogger(ZeroToOneConverter.class);
	/**
	 * A mapping from numeric events to XML representations of their version-1
	 * equivalents.
	 */
	private static final Map<Integer, String> EQUIVALENTS = new HashMap<>();

	static {
		addXML("<mineral kind=\"iron\" exposed=\"true\" dc=\"0\" />", 200, 206);
		addXML("<mineral kind=\"iron\" exposed=\"false\" dc=\"0\" />", 201,
				202, 207, 208);
		addXML("<mineral kind=\"copper\" exposed=\"true\" dc=\"0\" />", 203,
				209);
		addXML("<mineral kind=\"copper\" exposed=\"false\" dc=\"0\" />", 204,
				205, 210, 211);
		addXML("<mineral kind=\"gold\" exposed=\"true\" dc=\"0\" />", 212);
		addXML("<mineral kind=\"gold\" exposed=\"false\" dc=\"0\" />", 213);
		addXML("<mineral kind=\"silver\" exposed=\"true\" dc=\"0\" />", 214);
		addXML("<mineral kind=\"silver\" exposed=\"false\" dc=\"0\" />", 215);
		addXML("<mineral kind=\"coal\" exposed=\"true\" dc=\"0\" />", 216, 219);
		addXML("<mineral kind=\"coal\" exposed=\"false\" dc=\"0\" />", 217,
				218, 220, 221);
		addXML("<town status=\"active\" size=\"small\" dc=\"0\" />", 222);
		addXML("<town status=\"abandoned\" size=\"small\" dc=\"0\" />", 223,
				227, 231);
		addXML("<fortification status=\"abandoned\" size=\"small\" dc=\"0\" />",
				224, 228, 232);
		addXML("<town status=\"burned\" size=\"small\" dc=\"0\" />", 225, 229,
				233);
		addXML("<fortification status=\"burned\" size=\"small\" dc=\"0\" />",
				226, 230, 234);
		addXML("<battlefield dc=\"0\" />", 235, 236, 237, 238, 239, 240);
		addXML("<city status=\"ruined\" size=\"medium\" dc=\"0\" />", 241, 243);
		addXML("<fortification status=\"ruined\" size=\"medium\" dc=\"0\" />",
				242, 244);
		addXML("<city status=\"ruined\" size=\"large\" dc=\"0\" />", 245);
		addXML("<fortification status=\"ruined\" size=\"large\" dc=\"0\" />",
				246);
		addXML("<stone kind=\"limestone\" dc=\"0\" />", 247, 248, 249);
		addXML("<stone kind=\"marble\" dc=\"0\" />", 250, 251, 252);
		addXML("<cave dc=\"0\" />", 253, 254, 255);
	}

	/**
	 * Whether the specified tag is the desired tag, in a namespace we support.
	 * @param tag     the name of an XML tag
	 * @param desired the desired XML tag
	 * @return whether it matches, either in our namespace or the default namespace
	 */
	private static boolean isSpecifiedTag(final QName tag, final String desired) {
		return tag.equals(new QName(ISPReader.NAMESPACE, desired)) ||
					   tag.equals(new QName(desired));
	}

	/**
	 * Read version-0 XML from the input stream and write version-1 equivalent XML to
	 * the output stream.
	 * @param stream  a stream representing a SP map, format version 0
	 * @param ostream the stream to write the equivalent map, format version 1, to
	 * @throws IOException on I/O error writing to ostream
	 */
	public static void convert(final Iterable<XMLEvent> stream, final Appendable ostream)
			throws IOException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final StartElement startElement = event.asStartElement();
				if (isSpecifiedTag(startElement.getName(), "tile")) {
					//noinspection unchecked
					convertTile(ostream, startElement,
							iFactory(startElement.getAttributes()));
				} else if (isSpecifiedTag(startElement.getName(), "map")) {
					//noinspection unchecked
					convertMap(ostream, startElement,
							iFactory(startElement.getAttributes()));
				} else {
					printStartElement(ostream, startElement);
				}

			} else if (event.isCharacters()) {
				ostream.append(event.asCharacters().getData().trim());
			} else if (event.isEndElement()) {
				ostream.append(printEndElement(event.asEndElement()));
			} else if (event.isStartDocument()) {
				ostream.append("<?xml version=\"1.0\"?>").append(LineEnd.LINE_SEP);
			} else if (event.isEndDocument()) {
				break;
			} else {
				LOGGER.warning("Unhandled element type " + event.getEventType());
			}
		}
		ostream.append(LineEnd.LINE_SEP);
	}

	/**
	 * Convert the version attribute of the map.
	 *
	 * @param element the map element
	 * @param attrs   its attributes
	 * @param ostream the stream to which to write the converted tag, in XML
	 *                representation.
	 * @throws IOException on I/O error writing to ostream
	 */
	private static void convertMap(final Appendable ostream, final StartElement element,
								   final Iterable<Attribute> attrs) throws IOException {
		ostream.append('<');
		if (!XMLConstants.DEFAULT_NS_PREFIX.equals(element.getName().getNamespaceURI()
		)) {
			ostream.append(element.getName().getPrefix());
			ostream.append(':');
		}
		ostream.append(element.getName().getLocalPart());
		//noinspection unchecked Unavoidable: getNamespaces() isn't generified
		for (final Object namespace : new IteratorWrapper(element.getNamespaces())) {
			ostream.append(' ');
			ostream.append(namespace.toString());
		}
		for (final Attribute attr : attrs) {
			if ("version".equalsIgnoreCase(attr.getName().getLocalPart())) {
				ostream.append(" version=\"1\"");
			} else {
				ostream.append(printAttribute(attr));
			}
		}
		ostream.append('>');
	}

	/**
	 * Used to throw ParseException if a tile has a non-numeric 'event'; now merely logs
	 * that, I think.
	 *
	 * @param element the current element
	 * @param attrs   its attributes.
	 * @param ostream the stream to which to write the converted tile, in XML
	 *                representation
	 * @throws NumberFormatException if a tile has a non-numeric 'event'
	 * @throws IOException           on I/O error writing to ostream
	 */
	private static void convertTile(final Appendable ostream, final StartElement element,
									final Iterable<Attribute> attrs)
			throws IOException {
		ostream.append('<');
		if (!XMLConstants.DEFAULT_NS_PREFIX.equals(element.getName().getNamespaceURI()
		)) {
			ostream.append(element.getName().getPrefix());
			ostream.append(':');
		}
		ostream.append(element.getName().getLocalPart());
		final Deque<Integer> events = new LinkedList<>();
		for (final Attribute attr : attrs) {
			if ("event".equalsIgnoreCase(attr.getName().getLocalPart())) {
				try {
					events.push(Integer.valueOf(
							NumberFormat.getIntegerInstance().parse(attr.getValue())
									.intValue()));
				} catch (final ParseException e) {
					LOGGER.log(Level.SEVERE, "Non-numeric 'event'", e);
				}
			} else {
				ostream.append(printAttribute(attr));
			}
		}
		ostream.append('>');
		while (!events.isEmpty()) {
			ostream.append(LineEnd.LINE_SEP);
			ostream.append(getEventXML(events.pop()));
		}
	}

	/**
	 * An Iterable factory. (TODO: Do we really need this?)
	 * @param iter an iterator
	 * @return a wrapper
	 */
	private static Iterable<Attribute> iFactory(@Nullable
												final Iterator<Attribute> iter) {
		return new IteratorWrapper<>(iter);
	}

	/**
	 * Get the equivalent XML to a numerical "event" property.
	 * @param num an event number
	 * @return the XML representing it, or "" if none.
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	private static String getEventXML(final Integer num) {
		if (EQUIVALENTS.containsKey(num)) {
			return EQUIVALENTS.get(num);
		} else {
			return "";
		}
	}

	/**
	 * Print an end element.
	 *
	 * @param element the element
	 * @return its XML representation.
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	private static String printEndElement(final EndElement element) {
		if (XMLConstants.DEFAULT_NS_PREFIX.equals(element.getName().getNamespaceURI())) {
			return printEndElementImpl(
					element.getName().getLocalPart());
		} else {
			return printEndElementImpl(element.getName().getPrefix() + ':' +
											   element.getName().getLocalPart());
		}

	}

	/**
	 * Print an end element.
	 *
	 * @param elemStr the local part of the element
	 * @return its XML representation.
	 */
	private static String printEndElementImpl(final String elemStr) {
		return String.format("</%s>", elemStr);
	}

	/**
	 * Print a start element.
	 *
	 * @param element the element
	 * @param ostream the stream to which to write its XML representation.
	 * @throws IOException on I/O error writing to ostream
	 */
	private static void printStartElement(final Appendable ostream,
										  final StartElement element) throws
			IOException {
		ostream.append('<');
		if (!XMLConstants.DEFAULT_NS_PREFIX.equals(element.getName().getNamespaceURI()
		)) {
			ostream.append(element.getName().getPrefix());
			ostream.append(':');
		}
		ostream.append(element.getName().getLocalPart());
		// getAttributes() isn't actually genericized, so diamond causes compile error
		//noinspection Convert2Diamond,unchecked
		for (final Attribute attr : new IteratorWrapper<Attribute>(element.getAttributes
																				   ())) {
			ostream.append(printAttribute(attr));
		}
		ostream.append('>');
	}

	/**
	 * Write an attribute back to XML.
	 * @param attr an attribute
	 * @return its XML representation
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	private static String printAttribute(final Attribute attr) {
		return String.format(" %s=\"%s\"",
				attr.getName().getLocalPart(), attr.getValue());
	}

	/**
	 * Add XML for the specified numbers.
	 *
	 * @param xml     the XML to add
	 * @param numbers the numbers to add it for
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private static void addXML(final String xml, final int... numbers) {
		for (final int num : numbers) {
			EQUIVALENTS.put(Integer.valueOf(num), xml);
		}
	}

	/**
	 * Driver.
	 *
	 * @param args the filenames to try it on. Prints results to stdout.
	 */
	public static void main(final String... args) {
		for (final String arg : args) {
			//noinspection ObjectAllocationInLoop
			try (final Reader reader = new FileReader(arg)) {
				//noinspection unchecked,ObjectAllocationInLoop
				convert(new IteratorWrapper<>(XMLInputFactory.newInstance()
													  .createXMLEventReader(reader)),
						SystemOut.SYS_OUT);
			} catch (final FileNotFoundException | NoSuchFileException except) {
				LOGGER.log(Level.SEVERE, "File " + arg + " not found", except);
			} catch (final XMLStreamException except) {
				LOGGER.log(Level.SEVERE, "XML error", except);
			} catch (final IOException except) {
				//noinspection HardcodedFileSeparator
				LOGGER.log(Level.SEVERE, "I/O error dealing with file " + arg,
						except);
			}
		}
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ZeroToOneConverter";
	}
}
