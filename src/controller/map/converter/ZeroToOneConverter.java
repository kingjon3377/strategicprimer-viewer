package controller.map.converter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.eclipse.jdt.annotation.Nullable;
import util.IteratorWrapper;
import util.NullCleaner;
import util.TypesafeLogger;

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
 * @author Jonathan Lovelace
 */
public final class ZeroToOneConverter {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
												 .getLogger(ZeroToOneConverter.class);

	/**
	 * A mapping from numeric events to XML representations of their version-1
	 * equivalents.
	 */
	private static final Map<Integer, String> EQUIVS = new HashMap<>();

	/**
	 * @param stream a stream representing a SP map, format version 0
	 * @param ostream the stream to write the equivalent map, format version 1, to
	 * @throws IOException on I/O error writing to ostream
	 */
	public static void convert(final Iterable<XMLEvent> stream, final Appendable ostream)
			throws IOException {
		for (final XMLEvent event : stream) {
			if (event.isStartElement()) {
				final StartElement selement =
						NullCleaner.assertNotNull(event.asStartElement());
				if ("tile".equalsIgnoreCase(selement.getName()
													.getLocalPart())) {
					//noinspection unchecked
					convertTile(ostream, selement,
							iFactory(selement.getAttributes()));
				} else if ("map".equalsIgnoreCase(selement
														  .getName().getLocalPart())) {
					//noinspection unchecked
					convertMap(ostream, selement,
							iFactory(selement.getAttributes()));
				} else {
					printStartElement(ostream, selement);
				}

			} else if (event.isCharacters()) {
				ostream.append(event.asCharacters().getData().trim());
			} else if (event.isEndElement()) {
				ostream.append(printEndElement(NullCleaner.assertNotNull(event
																				 .asEndElement())));
			} else if (event.isStartDocument()) {
				ostream.append("<?xml version=\"1.0\"?>\n");
			} else if (event.isEndDocument()) {
				break;
			} else {
				LOGGER.warning("Unhandled element type " + event.getEventType());
			}
		}
		ostream.append('\n');
	}

	/**
	 * Convert the version attribute of the map.
	 *
	 * @param element the map element
	 * @param attrs   its attributes
	 * @param ostream the stream to which to write the converted tag, in XML representation.
	 * @throws IOException on I/O error writing to ostream
	 */
	private static void convertMap(final Appendable ostream, final StartElement element,
									 final Iterable<Attribute> attrs) throws IOException {
		ostream.append('<');
		ostream.append(element.getName().getLocalPart());
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
	 * Used to throw ParseException if a tile has a nonnumeric 'event'; now merely logs
	 * that, I think.
	 *
	 * @param element the current element
	 * @param attrs   its attributes.
	 * @param ostream the stream to which to write the converted tile, in XML representation
	 * @throws NumberFormatException if a tile has a non-numeric 'event'
	 * @throws IOException on I/O error writing to ostream
	 */
	private static void convertTile(final Appendable ostream, final StartElement element,
									  final Iterable<Attribute> attrs)
			throws IOException {
		ostream.append('<');
		ostream.append(element.getName().getLocalPart());
		final Deque<Integer> events = new LinkedList<>();
		for (final Attribute attr : attrs) {
			if ("event".equalsIgnoreCase(attr.getName().getLocalPart())) {
				try {
					events.push(NullCleaner.assertNotNull(Integer.valueOf(NumberFormat
																				  .getIntegerInstance()
																				  .parse
																						   (attr.getValue())

																				  .intValue())));
				} catch (final ParseException e) {
					LOGGER.log(Level.SEVERE, "Non-numeric 'event'", e);
				}
			} else {
				ostream.append(printAttribute(attr));
			}
		}
		ostream.append('>');
		while (!events.isEmpty()) {
			ostream.append('\n');
			ostream.append(getEventXML(NullCleaner.assertNotNull(events.pop())));
		}
	}

	/**
	 * @param iter an iterator
	 * @return a wrapper
	 */
	private static Iterable<Attribute> iFactory(
													   @Nullable
													   final Iterator<Attribute> iter) {
		return new IteratorWrapper<>(iter);
	}

	/**
	 * @param num an event number
	 * @return the XML representing it, or "" if none.
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	private static String getEventXML(final Integer num) {
		if (EQUIVS.containsKey(num)) {
			return NullCleaner.assertNotNull(EQUIVS.get(num)); // NOPMD
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
		return printEndElementImpl(NullCleaner.assertNotNull(element.getName()
																	 .getLocalPart()));
	}

	/**
	 * Print an end element.
	 *
	 * @param elemStr the local part of the element
	 * @return its XML representation.
	 */
	private static String printEndElementImpl(final String elemStr) {
		return NullCleaner.assertNotNull(String.format("</%s>", elemStr));
	}

	/**
	 * Print a start element.
	 *
	 * @param element the element
	 * @param ostream the stream to which to write its XML representation.
	 * @throws IOException on I/O error writing to ostream
	 */
	private static void printStartElement(final Appendable ostream,
	                                      final StartElement element) throws IOException {
		ostream.append('<');
		ostream.append(element.getName().getLocalPart());
		// TODO: Use Stream API
		// getAttributes() isn't actually genericized, so diamond causes compile error
		//noinspection Convert2Diamond,unchecked
		for (final Attribute attr : new IteratorWrapper<Attribute>(element.getAttributes())) {
			ostream.append(printAttribute(attr));
		}
		ostream.append('>');
	}

	/**
	 * @param attr an attribute
	 * @return its XML representation
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	private static String printAttribute(final Attribute attr) {
		return NullCleaner.assertNotNull(String.format(" %s=\"%s\"",
				attr.getName().getLocalPart(), attr.getValue()));
	}

	/**
	 * Add XML for the specified numbers.
	 *
	 * @param xml  the XML to add
	 * @param nums the numbers to add it for
	 */
	private static void addXML(final String xml, final int... nums) {
		for (final int num : nums) {
			EQUIVS.put(NullCleaner.assertNotNull(Integer.valueOf(num)), xml);
		}
	}

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
	 * Driver.
	 *
	 * @param args the filenames to try it on. Prints results to stdout.
	 */
	public static void main(final String... args) {
		for (final String arg : args) {
			try (final Reader reader = new FileReader(arg)) { // NOPMD
				//noinspection unchecked
				convert(new IteratorWrapper<>(XMLInputFactory.newInstance()
						                              .createXMLEventReader(reader)),
						System.out);
			} catch (final FileNotFoundException except) {
				LOGGER.log(Level.SEVERE, "File " + arg + " not found", except);
			} catch (final XMLStreamException except) {
				LOGGER.log(Level.SEVERE, "XML error", except);
			} catch (final IOException except) {
				LOGGER.log(Level.SEVERE, "I/O error dealing with file " + arg,
						except);
			}
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ZeroToOneConverter";
	}
}
