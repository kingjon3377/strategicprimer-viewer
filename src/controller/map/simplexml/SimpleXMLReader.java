package controller.map.simplexml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.LinkedList;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.SPMap;
import util.IteratorWrapper;
import controller.map.IMapReader;
import controller.map.SPFormatException;
import controller.map.simplexml.node.AbstractChildNode;
import controller.map.simplexml.node.AbstractXMLNode;
import controller.map.simplexml.node.TileNode;

/**
 * An XML-map reader that just converts the XML into XMLNodes, which then
 * convert themselves into the map.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class SimpleXMLReader implements IMapReader {
	/**
	 * @param file
	 *            the name of a file
	 * @return the map contained in that file
	 * @throws IOException
	 *             on I/O error
	 * @throws SPFormatException
	 *             if the data is invalid
	 * @throws XMLStreamException
	 *             if the XML isn't well-formed
	 */
	@Override
	public SPMap readMap(final String file) throws IOException,
			XMLStreamException, SPFormatException {
		final FileInputStream istream = new FileInputStream(file);
		try {
			return readMap(istream);
		} finally {
			istream.close();
		}
	}

	/**
	 * @param istream
	 *            a stream
	 * @return the map contained in that stream
	 * @throws XMLStreamException
	 *             if XML isn't well-formed.
	 * @throws SPFormatException
	 *             if the data is invalid.
	 */
	@Override
	public SPMap readMap(final InputStream istream) throws XMLStreamException,
			SPFormatException {
		final RootNode root = new RootNode();
		final Deque<AbstractXMLNode> stack = new LinkedList<AbstractXMLNode>();
		stack.push(root);
		@SuppressWarnings("unchecked")
		final IteratorWrapper<XMLEvent> eventReader = new IteratorWrapper<XMLEvent>(
				XMLInputFactory.newInstance().createXMLEventReader(istream));
		for (final XMLEvent event : eventReader) {
			if (event.isStartElement()) {
				final AbstractXMLNode node = parseTag(event.asStartElement());
				stack.peek().addChild(node);
				stack.push(node);
			} else if (event.isCharacters()) {
				if (stack.peek() instanceof TileNode) {
					((TileNode) stack.peek()).addText(event.asCharacters()
							.getData());
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
	 * 
	 * @param element
	 *            the tag
	 * @return the equivalent node.
	 * @throws SPFormatException
	 *             on unexpecte or illegal XML.
	 */
	private static AbstractXMLNode parseTag(final StartElement element)
			throws SPFormatException {
		final AbstractChildNode<?> node = NodeFactory.create(element.getName()
				.getLocalPart(), element.getLocation().getLineNumber());
		@SuppressWarnings("unchecked")
		final IteratorWrapper<Attribute> attributes = new IteratorWrapper<Attribute>(
				element.getAttributes());
		for (final Attribute att : attributes) {
			node.addProperty(att.getName().getLocalPart(), att.getValue());
		}
		return node;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SimpleXMLReader";
	}

}
