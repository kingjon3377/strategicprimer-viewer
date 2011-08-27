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
import controller.map.MapVersionException;
import controller.map.simplexml.node.TileNode;
import controller.map.stax.MapReader;

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
		final MapReader oldReader = new MapReader();
		final SimpleXMLReader newReader = new SimpleXMLReader();
		try {
		for (final String arg : args) {
			try {
				compareReaders(out, arg, oldReader, newReader);
			} catch (XMLStreamException e) {
				logger.log(Level.SEVERE, "XMLStreamException (probably badly formed input) in " + arg, e);
				continue;
			} catch (FileNotFoundException e) {
				logger.log(Level.SEVERE, arg + " not found", e);
				continue;
			} catch (IOException e) {
				logger.log(Level.SEVERE, "I/O error while parsing" + arg, e);
				continue;
			} catch (SPFormatException e) {
				logger.log(Level.SEVERE, "New reader claims invalid SP map data in " + arg, e);
				continue;
			} catch (MapVersionException e) {
				logger.log(Level.SEVERE, "Map version too old for old-style reader in file " + arg, e);
				continue; 
			} 
		}
		} finally {
		out.close();
		}
	}
	// ESCA-JAVA0160:
	/**
	 * Compare two readers on a single file.
	 * @param out the stream to write results on
	 * @param arg the file to try them on
	 * @param oldReader the old reader
	 * @param newReader the new reader
	 * @throws XMLStreamException if either reader claims badly formed input
	 * @throws FileNotFoundException if either reader can't find the file
	 * @throws IOException on other I/O error in either reader
	 * @throws SPFormatException if the new reader claims invalid data
	 * @throws MapVersionException if the old reader can't handle that map version
	 */
	private static void compareReaders(final PrintWriter out,
			final String arg, final MapReader oldReader,
			final SimpleXMLReader newReader) throws XMLStreamException,
			FileNotFoundException, IOException, SPFormatException, MapVersionException {
		final long startOne = System.nanoTime();
		final SPMap map1 = oldReader.readMap(arg); 
		final long endOne = System.nanoTime();
		out.print("Old method took ");
		out.print((endOne - startOne));
		out.println(" time-units.");
		final long startTwo = System.nanoTime();
		final SPMap map2 = newReader.readMap(arg); 
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
}
