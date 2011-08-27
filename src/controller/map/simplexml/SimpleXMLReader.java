package controller.map.simplexml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.viewer.SPMap;
import util.IteratorWrapper;
import controller.map.MapReader;
import controller.map.MapVersionException;

/**
 * An XML-map reader that just converts the XML into XMLNodes, which then
 * convert themselves into the map.
 * 
 * @author Jonathan Lovelace
 * 
 */
@SuppressWarnings("deprecation")
public class SimpleXMLReader {
	/**
	 * @param file the name of a file
	 * @return the map contained in that file
	 * @throws IOException on I/O error
	 * @throws SPFormatException if the data is invalid
	 * @throws XMLStreamException if the XML isn't well-formed
	 */
	public SPMap readMap(final String file) throws IOException, XMLStreamException, SPFormatException {
		final FileInputStream istream = new FileInputStream(file);
		try {
			return readMap(istream);
		} finally {
		istream.close();
		}
	}
	/**
	 * @param istream a stream
	 * @return the map contained in that stream
	 * @throws XMLStreamException if XML isn't well-formed.
	 * @throws SPFormatException if the data is invalid.
	 */
	public SPMap readMap(final InputStream istream) throws XMLStreamException, SPFormatException {
		final RootNode root = new RootNode();
		final Deque<AbstractXMLNode> stack = new LinkedList<AbstractXMLNode>();
		stack.push(root);
		@SuppressWarnings("unchecked")
		final IteratorWrapper<XMLEvent> eventReader = new IteratorWrapper<XMLEvent>(
				XMLInputFactory.newInstance().createXMLEventReader(istream));
		for (XMLEvent event : eventReader) {
			if (event.isStartElement()) {
				final AbstractXMLNode node = parseTag(event.asStartElement());
				stack.peek().addChild(node);
				stack.push(node);
			} else if (event.isCharacters()) {
				if (stack.peek() instanceof TileNode) {
					((TileNode) stack.peek()).addText(event.asCharacters().getData());
				}
			} else if (event.isEndElement()) {
				stack.pop();
			}
		}
		root.canonicalize();
		root.checkNode();
		return root.getMapNode().produce(null);
	}
	/**
	 * Turn a tag and its contents (properties) into a Node.
	 * @param element the tag
	 * @return the equivalent node.
	 * @throws SPFormatException on unexpecte or illegal XML.
	 */
	private static AbstractXMLNode parseTag(final StartElement element) throws SPFormatException {
		final AbstractChildNode<?> node = NodeFactory.create(element.getName()
				.getLocalPart(), element.getLocation().getLineNumber());
		@SuppressWarnings("unchecked")
		final IteratorWrapper<Attribute> attributes = new IteratorWrapper<Attribute>(element.getAttributes());
		for (Attribute att : attributes) {
			node.addProperty(att.getName().getLocalPart(), att.getValue());
		}
		return node;
	}
	/**
	 * Driver method to compare the results of this reader with those of the previous reader.
	 * @param args The maps to test the two readers on.
	 */
	public static void main(final String[] args) {
		// ESCA-JAVA0266:
		final PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
		final Logger logger = Logger.getLogger(SimpleXMLReader.class.getName());
		try {
		for (final String arg : args) {
				final long startOne = System.nanoTime();
				// ESCA-JAVA0177:
				final SPMap map1; // NOPMD
				try {
					map1 = new MapReader().readMap(arg); // NOPMD
				} catch (XMLStreamException e) {
					logger.log(Level.SEVERE, "XMLStreamException (probably badly formed input) in " + arg, e);
					continue;
				} catch (IOException e) {
					logger.log(Level.SEVERE, "I/O error while parsing" + arg, e);
					continue;
				} catch (MapVersionException e) {
					logger.log(Level.SEVERE, "Map version too old for old-style reader in file " + arg, e);
					continue;
				} 
				final long endOne = System.nanoTime();
				out.print("Old method took ");
				out.print((endOne - startOne));
				out.println(" time-units.");
				final long startTwo = System.nanoTime();
				// ESCA-JAVA0177:
				final SPMap map2; // NOPMD
				try {
					map2 = new SimpleXMLReader().readMap(arg); // NOPMD
				} catch (FileNotFoundException e) {
					logger.log(Level.SEVERE, arg + " not found", e);
					continue;
				} catch (XMLStreamException e) {
					logger.log(Level.SEVERE, "XML stream exception in " + arg, e);
					continue;
				} catch (SPFormatException e) {
					logger.log(Level.SEVERE, "Invalid SP map data in " + arg, e);
					continue;
				} catch (IOException e) {
					logger.log(Level.SEVERE, "I/O error parsing " + arg, e);
					continue;
				}
				final long endTwo = System.nanoTime();
				out.print("New method took ");
				out.print((endTwo - startTwo));
				out.println(" time-units.");
				if (map1.equals(map2)) {
					out.println("Readers produce identical results.");
				} else {
					out.print("Readers differ on ");
					out.println(arg);
				}
		}
		} finally {
		out.close();
		}
	}
}
