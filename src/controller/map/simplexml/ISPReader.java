package controller.map.simplexml;

import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import util.Warning;
import controller.map.SPFormatException;
/**
 * An interface for readers of any SP model type.
 * @author Jonathan Lovelace
 *
 */
public interface ISPReader {

	/**
	 * @param <T> The type of the object the XML represents
	 * @param istream
	 *            a reader from which to read the XML
	 * @param type The type of the object the XML represents
	 * @param warner a Warning instance to use for warnings
	 * @return the object contained in that stream
	 * @throws XMLStreamException
	 *             if XML isn't well-formed.
	 * @throws SPFormatException
	 *             if the data is invalid.
	 */
	<T> T readXML(final Reader istream, final Class<T> type,
			final Warning warner) throws XMLStreamException, SPFormatException;

}
