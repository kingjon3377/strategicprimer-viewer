package model.map;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import util.Warning;
import controller.map.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;

// ESCA-JAVA0011:
/**
 * An abstract base class for this helper method.
 * 
 * @author Jonathan Lovelace
 * 
 */
public abstract class BaseTestFixtureSerialization { // NOPMD
	/**
	 * A helper method to simplify test boiler plate code.
	 * 
	 * @param <T>
	 *            The type of the object
	 * @param reader
	 *            the reader to parse the serialized form.
	 * @param orig
	 *            the object to serialize
	 * @param type
	 *            the type of the object
	 * @param reflection
	 *            whether to use reflection
	 * @return the result of deserializing the serialized form
	 * @throws SPFormatException
	 *             on SP XML problem
	 * @throws XMLStreamException
	 *             on XML reading problem
	 */
	protected <T extends XMLWritable> T helpSerialization(
			final SimpleXMLReader reader, final T orig, final Class<T> type,
			final boolean reflection) throws XMLStreamException,
			SPFormatException {
		return reader.readXML(new StringReader(orig.toXML()), type, reflection, warner);
	}
	/**
	 * Warning instance that makes warnings fatal.
	 */
	private final Warning warner = new Warning(Warning.Action.Die);
	/**
	 * @return the warning instance
	 */
	protected Warning warner() {
		return warner;
	}
}
