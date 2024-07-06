package legacy.xmlio.yaxml;

import common.xmlio.SPFormatException;

import javax.xml.stream.XMLStreamException;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import lovelace.util.ThrowingConsumer;

/**
 * An interface for XML readers that can read multiple related types, in the
 * sixth generation of SP XML I/O ("yet another SP XML reader").
 */
public interface YAReader<Item, Value> {
	/**
	 * Read an object from XML.
	 *
	 * @param element The element (XML tag) being parsed
	 * @param parent  The parent tag
	 * @param stream  The stream of XML events to read more from
	 * @throws SPFormatException on SP format errors
	 */
	Item read(StartElement element, QName parent, Iterable<XMLEvent> stream)
			throws SPFormatException, XMLStreamException;

	/**
	 * Write an object to the stream.
	 *
	 * @param ostream     The stream to write to
	 * @param obj         The object to write
	 * @param indentation The current indentation level
	 * @throws IOException on I/O error in writing
	 */
	void write(ThrowingConsumer<String, IOException> ostream, Value obj, int indentation) throws IOException;

	/**
	 * Whether we can read the given tag.
	 */
	boolean isSupportedTag(String tag);

	/**
	 * Whether we can write the given object.
	 */
	boolean canWrite(Object obj); // Should be "obj instanceof Value"

	/**
	 * Write the given object, when the caller knows the object is the
	 * right type but the typechecker doesn't. This will probably cause a
	 * ClassCastException if the types don't in fact match.
	 */
	default void writeRaw(final ThrowingConsumer<String, IOException> ostream, final Object obj, final int indentation)
			throws IOException {
		write(ostream, (Value) obj, indentation);
	}
}
