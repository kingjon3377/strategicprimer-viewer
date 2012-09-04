package controller.map;

import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import model.map.XMLWritable;
import util.Warning;

/**
 * An interface for readers of any SP model type.
 *
 * @author Jonathan Lovelace
 *
 */
public interface ISPReader {
	/**
	 * Tags we expect to use in the future; they are skipped for now and we'll
	 * warn if they're used.
	 */
	List<String> FUTURE = Collections.unmodifiableList(Arrays
			.asList(new String[] { "future", "worker", "explorer", "building",
					"resource", "animal", "changeset", "change", "move",
					"work", "discover", "submap" }));

	/**
	 * @param <T> A supertype of the object the XML represents
	 * @param file the name of the file being read from
	 * @param istream a reader from which to read the XML
	 * @param type The type of the object the XML represents
	 * @param warner a Warning instance to use for warnings
	 * @return the object contained in that stream
	 * @throws XMLStreamException if XML isn't well-formed.
	 * @throws SPFormatException if the data is invalid.
	 */
	<T extends XMLWritable> T readXML(final String file, final Reader istream, final Class<T> type,
			final Warning warner) throws XMLStreamException, SPFormatException;
}
