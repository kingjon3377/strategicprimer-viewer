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

import model.map.PlayerCollection;
import model.map.SPMap;
import util.IteratorWrapper;
import controller.map.IMapReader;
import controller.map.SPFormatException;
import controller.map.simplexml.node.AbstractChildNode;
import controller.map.simplexml.node.AbstractXMLNode;
import controller.map.simplexml.node.NodeFactory;

/**
 * An XML-map reader that just converts the XML into XMLNodes, which then
 * convert themselves into the map.
 * 
 * TODO: This whole system needs thorough tests.
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
	 * @throws XMLStreamException
	 *             if the XML isn't well-formed
	 * @throws SPFormatException
	 *             if the data is invalid
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
	 * @param reflection whether to try the reflection-based verion of the node factory method
	 * @param file
	 *            the name of a file
	 * @return the map contained in that file
	 * @throws IOException
	 *             on I/O error
	 * @throws XMLStreamException
	 *             if the XML isn't well-formed
	 * @throws SPFormatException
	 *             if the data is invalid
	 */
	public SPMap readMap(final String file, final boolean reflection) throws IOException,
			XMLStreamException, SPFormatException {
		final FileInputStream istream = new FileInputStream(file);
		try {
			return readMap(istream, reflection);
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
		return readMap(istream, false);
	}

	/**
	 * @param reflection whether to try the reflection-based verion of the node factory method
	 * @param istream
	 *            a stream
	 * @return the map contained in that stream
	 * @throws XMLStreamException
	 *             if XML isn't well-formed.
	 * @throws SPFormatException
	 *             if the data is invalid.
	 */
	public SPMap readMap(final InputStream istream, final boolean reflection)
			throws XMLStreamException, SPFormatException {
		final RootNode root = new RootNode();
		final Deque<AbstractXMLNode> stack = new LinkedList<AbstractXMLNode>();
		stack.push(root);
		final IteratorWrapper<XMLEvent> eventReader = new IteratorWrapper<XMLEvent>(
				XMLInputFactory.newInstance().createXMLEventReader(istream));
		for (final XMLEvent event : eventReader) {
			if (event.isStartElement()) {
				// ESCA-JAVA0177:
				AbstractXMLNode node;
				try {
					node = parseTag(event.asStartElement(), reflection);
				} catch (InstantiationException e) {
					throw new IllegalStateException(e);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
				stack.peek().addChild(node);
				stack.push(node);
			} else if (event.isCharacters()) {
				if (stack.peek() instanceof ITextNode) {
					((ITextNode) stack.peek()).addText(event.asCharacters()
							.getData());
				}
			} else if (event.isEndElement()) {
				stack.pop();
			}
		}
		root.canonicalize();
		root.checkNode();
		return root.getMapNode().produce(new PlayerCollection());
	}

	/**
	 * Turn a tag and its contents (properties) into a Node.
	 * 
	 * @param element
	 *            the tag
	 * @param reflection whether we should try the version of the NodeFactory method that uses reflection
	 * @return the equivalent node.
	 * @throws SPFormatException
	 *             on unexpected or illegal XML.
	 * @throws IllegalAccessException thrown by reflection
	 * @throws InstantiationException thrown by reflection
	 */
	private static AbstractXMLNode parseTag(final StartElement element, final boolean reflection)
			throws SPFormatException, InstantiationException, IllegalAccessException {
		final AbstractChildNode<?> node = (reflection ? NodeFactory
				.createReflection(element.getName().getLocalPart(), element
						.getLocation().getLineNumber()) : NodeFactory.create(
				element.getName().getLocalPart(), element.getLocation()
						.getLineNumber()));
		final IteratorWrapper<Attribute> attributes = new IteratorWrapper<Attribute>(
				element.getAttributes());
		for (final Attribute att : attributes) {
			node.addProperty(att.getName().getLocalPart(), att.getValue());
		}
		return node;
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SimpleXMLReader";
	}

}
