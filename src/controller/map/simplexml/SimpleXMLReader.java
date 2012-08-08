package controller.map.simplexml;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Deque;
import java.util.LinkedList;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.IMap;
import model.map.MapView;
import model.map.PlayerCollection;
import util.IteratorWrapper;
import util.Warning;
import controller.map.IMapReader;
import controller.map.ISPReader;
import controller.map.SPFormatException;
import controller.map.misc.FileOpener;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;
import controller.map.simplexml.node.AbstractChildNode;
import controller.map.simplexml.node.AbstractXMLNode;
import controller.map.simplexml.node.NodeFactory;

/**
 * An XML-map reader that just converts the XML into XMLNodes, which then
 * convert themselves into the map.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
 */
@Deprecated
public class SimpleXMLReader implements IMapReader, ISPReader {
	/**
	 * @param file the name of a file
	 * @param warner the Warning instance to use for warnings.
	 * @return the map contained in that file
	 * @throws IOException on I/O error
	 * @throws XMLStreamException if the XML isn't well-formed
	 * @throws SPFormatException if the data is invalid
	 */
	@Override
	public MapView readMap(final String file, final Warning warner)
			throws IOException, XMLStreamException, SPFormatException {
		final Reader istream = new FileOpener().createReader(file);
		try {
			return readMap(file, istream, warner);
		} finally {
			istream.close();
		}
	}

	/**
	 * @param reflection whether to try the reflection-based verion of the node
	 *        factory method
	 * @param file the name of a file
	 * @param warner the Warning instance to use for warnings.
	 * @return the map contained in that file
	 * @throws IOException on I/O error
	 * @throws XMLStreamException if the XML isn't well-formed
	 * @throws SPFormatException if the data is invalid
	 */
	public IMap readMap(final String file, final boolean reflection,
			final Warning warner) throws IOException, XMLStreamException,
			SPFormatException {
		final FileReader istream = new FileReader(file);
		try {
			return readMap(file, istream, reflection, warner);
		} finally {
			istream.close();
		}
	}

	/**
	 * @param file the name of the file being read from
	 * @param istream a reader from which to read the XML
	 * @param warner the Warning instance to use for warnings.
	 * @return the map contained in that stream
	 * @throws XMLStreamException if XML isn't well-formed.
	 * @throws SPFormatException if the data is invalid.
	 */
	@Override
	public MapView readMap(final String file, final Reader istream,
			final Warning warner) throws XMLStreamException, SPFormatException {
		return readMap(file, istream, false, warner);
	}

	/**
	 * @param reflection whether to try the reflection-based verion of the node
	 *        factory method
	 * @param file the name of the file being read from
	 * @param istream a reader from which to read the XML
	 * @param warner the Warning instance to use for warnings.
	 * @return the map contained in that stream
	 * @throws XMLStreamException if XML isn't well-formed.
	 * @throws SPFormatException if the data is invalid.
	 */
	public MapView readMap(final String file, final Reader istream,
			final boolean reflection, final Warning warner)
			throws XMLStreamException, SPFormatException {
		return readXML(file, istream, MapView.class, reflection, warner);
	}

	/**
	 * Use readMap if you want a map; this is public primarily for testing
	 * purposes.
	 *
	 * @param <T> The type of the object the XML represents
	 * @param file the name of the file being read from
	 * @param istream a reader from which to read the XML
	 * @param type The type of the object the XML represents
	 * @param warner a Warning instance to use for warnings
	 * @return the object contained in that stream
	 * @throws XMLStreamException if XML isn't well-formed.
	 * @throws SPFormatException if the data is invalid.
	 */
	@Override
	public <T> T readXML(final String file, final Reader istream,
			final Class<T> type, final Warning warner)
			throws XMLStreamException, SPFormatException {
		return readXML(file, istream, type, true, warner);
	}

	/**
	 * Use readMap if you want a map; this is public primarily for testing
	 * purposes.
	 *
	 * @param <T> The type of the object the XML represents
	 * @param reflection whether to try the reflection-based verion of the node
	 *        factory method
	 * @param file the name of the file being read from
	 * @param istream a reader from which to read the XML
	 * @param type The type of the object the XML represents
	 * @param warner a Warning instance to use for warnings
	 * @return the object contained in that stream
	 * @throws XMLStreamException if XML isn't well-formed.
	 * @throws SPFormatException if the data is invalid.
	 */
	@Override
	public <T> T readXML(final String file, final Reader istream,
			final Class<T> type, final boolean reflection, final Warning warner)
			throws XMLStreamException, SPFormatException {
		final RootNode<T> root = new RootNode<T>(type);
		final Deque<AbstractXMLNode> stack = new LinkedList<AbstractXMLNode>();
		stack.push(root);
		final IteratorWrapper<XMLEvent> eventReader = new IteratorWrapper<XMLEvent>(
				new IncludingIterator(file, XMLInputFactory.newInstance()
						.createXMLEventReader(istream)));
		for (final XMLEvent event : eventReader) {
			if (event.isStartElement()) {
				// ESCA-JAVA0177:
				AbstractXMLNode node;
				try {
					node = parseTag(event.asStartElement(), reflection,
							getFileName(eventReader), warner); // NOPMD
				} catch (final InstantiationException e) {
					throw new IllegalStateException(e);
				} catch (final IllegalAccessException e) {
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
		root.canonicalize(warner);
		root.checkNode(warner, new IDFactory());
		return root.getRootNode().produce(new PlayerCollection(), warner);
	}

	/**
	 * Turn a tag and its contents (properties) into a Node.
	 *
	 * @param element the tag
	 * @param reflection whether we should try the version of the NodeFactory
	 *        method that uses reflection
	 * @param file the current filename
	 * @param warner a Warning instance to use if necessary
	 * @return the equivalent node.
	 * @throws SPFormatException on unexpected or illegal XML.
	 * @throws IllegalAccessException thrown by reflection
	 * @throws InstantiationException thrown by reflection
	 */
	private static AbstractXMLNode parseTag(final StartElement element,
			final boolean reflection, final String file, final Warning warner)
			throws SPFormatException, InstantiationException,
			IllegalAccessException {
		final AbstractChildNode<?> node = reflection ? NodeFactory
				.createReflection(element.getName().getLocalPart(), element
						.getLocation().getLineNumber(), file, warner)
				: NodeFactory.create(element.getName().getLocalPart(), element
						.getLocation().getLineNumber(), file, warner);
		final IteratorWrapper<Attribute> attributes = new IteratorWrapper<Attribute>(
				element.getAttributes());
		for (final Attribute att : attributes) {
			node.addProperty(att.getName().getLocalPart(), att.getValue(),
					warner);
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

	/**
	 * @param stream an Iterable
	 * @return the file name we're working from, if its iterator is an
	 *         IncludingIterator, or the empty string.
	 */
	private static String getFileName(final Iterable<XMLEvent> stream) {
		if (stream.iterator() instanceof IncludingIterator) {
			return ((IncludingIterator) stream.iterator()).getFile(); // NOPMD
		} else {
			return "";
		}
	}
}
