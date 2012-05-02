package controller.map.converter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import util.IteratorWrapper;

/**
 * A class to convert a version-0 map to a version-1 map. As no reader currently
 * in the tree supports reading version 0 files, we have to handle that
 * ourselves, but fortunately not much requires changing.
 * 
 * We ignore namespaces, as I'm not sure quite how to handle them.
 * @author Jonathan Lovelace
 * 
 */
public class ZeroToOneConverter {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ZeroToOneConverter.class.getName());
	/** 
	 * @param stream a stream representing a SP map, format version 0
	 * @return the XML representing an equivalent map, format version 1.
	 */
	public String convert(final Iterable<XMLEvent> stream) {
		final StringBuilder builder = new StringBuilder();
		for (XMLEvent event : stream) {
			if (event.isStartElement()) {
				if ("tile".equalsIgnoreCase(event.asStartElement().getName()
						.getLocalPart())) {
					builder.append(convertTile(event.asStartElement(),
							iFactory(event
									.asStartElement()
									.getAttributes())));
				} else if ("map".equalsIgnoreCase(event.asStartElement().getName().getLocalPart())) {
					builder.append(convertMap(event.asStartElement(), 
							iFactory(event.asStartElement().getAttributes())));
				} else {
					builder.append(printStartElement(event.asStartElement()));
				}

			} else if (event.isCharacters()) {
				builder.append(event.asCharacters().getData().trim());
			} else if (event.isEndElement()) {
				builder.append(printEndElement(event.asEndElement()));
			} else if (event.isStartDocument()) {
				builder.append("<?xml version=\"1.0\"?>\n");
			} else if (event.isEndDocument()) {
				break;
			} else {
				LOGGER.warning("Unhandled element type " + event.getEventType());
			}
		}
		return builder.toString();
	}
	/**
	 * Convert the version attribute of the map.
	 * @param element the map element
	 * @param attrs its attributes
	 * @return the converted tag, in XML representation.
	 */
	private static String convertMap(final StartElement element,
			final Iterable<Attribute> attrs) {
		final StringBuilder builder = new StringBuilder();
		builder.append('<');
		builder.append(element.getName().getLocalPart());
		for (final Attribute attr : attrs) {
			if ("version".equalsIgnoreCase(attr.getName().getLocalPart())) {
				builder.append(" version=\"1\"");
			} else {
				builder.append(printAttribute(attr));
			}
		}
		builder.append('>');
		return builder.toString();
	}
	/**
	 * @param element the current element
	 * @param attrs its attributes.
	 * @return the converted tile, in XML representation
	 */
	private static String convertTile(final StartElement element,
			final Iterable<Attribute> attrs) {
		final StringBuilder builder = new StringBuilder();
		final Stack<Integer> events = new Stack<Integer>();
		builder.append('<');
		builder.append(element.getName().getLocalPart());
		for (final Attribute attr : attrs) {
			if ("event".equalsIgnoreCase(attr.getName().getLocalPart())) {
				events.push(Integer.valueOf(Integer.parseInt(attr.getValue())));
			} else {
				builder.append(printAttribute(attr));
			}
		}
		builder.append('>');
		while (!events.isEmpty()) {
			builder.append('\n');
			builder.append(getEventXML(events.pop()));
		}
		return builder.toString();
	}
	/**
	 * @param iter an iterator
	 * @return a wrapper
	 */
	private static Iterable<Attribute> iFactory(final Iterator<Attribute> iter) {
		return new IteratorWrapper<Attribute>(iter);
	}
	/**
	 * @param num an event number
	 * @return the XML representing it, or "" if none.
	 */
	private static String getEventXML(final Integer num) {
		return EQUIVS.containsKey(num) ? EQUIVS.get(num) : "";
	}
	/**
	 * Print an end element.
	 * @param element the element
	 * @return its XML representation.
	 */
	private static String printEndElement(final EndElement element) {
		return new StringBuilder("</").append(element.getName().getLocalPart())
				.append('>').toString();
	}
	/**
	 * Print a start element.
	 * @param element the element
	 * @return its XML representation.
	 */
	private static String printStartElement(final StartElement element) {
		final StringBuilder builder = new StringBuilder().append('<');
		builder.append(element.getName().getLocalPart());
		final Iterable<Attribute> attrs = new IteratorWrapper<Attribute>(// NOPMD
				element.getAttributes());
		for (final Attribute attr : attrs) {
				builder.append(printAttribute(attr));
		}
		builder.append('>');
		return builder.toString();
	}
	/**
	 * @param attr an attribute
	 * @return its XML representation
	 */
	private static String printAttribute(final Attribute attr) {
		return new StringBuilder().append(' ').append(attr.getName().getLocalPart())
				.append("=\"").append(attr.getValue()).append('"').toString();
	}
	/**
	 * A mapping from numeric events to XML representations of their version-1 equivalents.
	 */
	private static final Map<Integer, String> EQUIVS = new HashMap<Integer, String>();
	/**
	 * Add XML for the specified numbers.
	 * @param xml the XML to add
	 * @param nums the numbers to add it for 
	 */
	private static void addXML(final String xml, final int... nums) {
		for (int num : nums) {
			EQUIVS.put(Integer.valueOf(num), xml);
		}
	}
	static {
		addXML("<mineral kind=\"iron\" exposed=\"true\" dc=\"0\" />", 200, 206);
		addXML("<mineral kind=\"iron\" exposed=\"false\" dc=\"0\" />", 201, 202, 207, 208);
		addXML("<mineral kind=\"copper\" exposed=\"true\" dc=\"0\" />", 203, 209);
		addXML("<mineral kind=\"copper\" exposed=\"false\" dc=\"0\" />", 204, 205, 210, 211);
		addXML("<mineral kind=\"gold\" exposed=\"true\" dc=\"0\" />", 212);
		addXML("<mineral kind=\"gold\" exposed=\"false\" dc=\"0\" />", 213);
		addXML("<mineral kind=\"silver\" exposed=\"true\" dc=\"0\" />", 214);
		addXML("<mineral kind=\"silver\" exposed=\"false\" dc=\"0\" />", 215);
		addXML("<mineral kind=\"coal\" exposed=\"true\" dc=\"0\" />", 216, 219);
		addXML("<mineral kind=\"coal\" exposed=\"false\" dc=\"0\" />", 217, 218, 220, 221);
		addXML("<town status=\"active\" size=\"small\" dc=\"0\" />", 222);
		addXML("<town status=\"abandoned\" size=\"small\" dc=\"0\" />", 223, 227, 231);
		addXML("<fortification status=\"abandoned\" size=\"small\" dc=\"0\" />", 224, 228, 232);
		addXML("<town status=\"burned\" size=\"small\" dc=\"0\" />", 225, 229, 233);
		addXML("<fortification status=\"burned\" size=\"small\" dc=\"0\" />", 226, 230, 234);
		addXML("<battlefield dc=\"0\" />", 235, 236, 237, 238, 239, 240);
		addXML("<city status=\"ruined\" size=\"medium\" dc=\"0\" />", 241, 243);
		addXML("<fortification status=\"ruined\" size=\"medium\" dc=\"0\" />", 242, 244);
		addXML("<city status=\"ruined\" size=\"large\" dc=\"0\" />", 245);
		addXML("<fortification status=\"ruined\" size=\"large\" dc=\"0\" />", 246);
		addXML("<stone kind=\"limestone\" dc=\"0\" />", 247, 248, 249);
		addXML("<stone kind=\"marble\" dc=\"0\" />", 250, 251, 252);
		addXML("<cave dc=\"0\" />", 253, 254, 255);
	}
	/**
	 * Singleton instance, for use in the main method.
	 */
	private static final ZeroToOneConverter CONVERTER = new ZeroToOneConverter();
	/**
	 * Driver.
	 * @param args the filenames to try it on. Prints results to stdout.
	 */
	public static void main(final String[] args) {
		for (String arg : args) {
			// ESCA-JAVA0177:
			final Reader reader; // NOPMD
			try {
				reader = new FileReader(arg); // NOPMD
			} catch (final FileNotFoundException except) {
				LOGGER.log(Level.SEVERE, "File " + arg + " not found", except);
				continue;
			}
			try {
				System.out.println(CONVERTER
						.convert(new IteratorWrapper<XMLEvent>(XMLInputFactory // NOPMD
								.newInstance().createXMLEventReader(reader))));
			} catch (final XMLStreamException except) {
				LOGGER.log(Level.SEVERE, "XML error", except);
			} finally {
				try {
					reader.close();
				} catch (final IOException except) {
					LOGGER.log(Level.SEVERE, "I/O error closing file", except);
				}
			}
		}
	}
}
