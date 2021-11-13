package impl.xmlio;

import java.nio.file.Path;

import java.io.Reader;
import java.io.IOException;

import common.map.IMutableMapNG;

import common.xmlio.SPFormatException;
import common.xmlio.Warning;
import lovelace.util.MissingFileException;
import lovelace.util.MalformedXMLException;

import javax.xml.stream.XMLStreamException;

/**
 * An interface for map readers.
 */
public interface IMapReader {
	/**
	 * Read the map (view) contained in a file.
	 *
	 * @throws SPFormatException if the reader can't handle this map
	 *                           version, doesn't recognize the map format,
	 *                           or finds the file contains format errors
	 * @throws MissingFileException if the file does not exist
	 * @throws XMLStreamException on low-level XML errors
	 * @throws IOException on I/O errors not covered by {@link
	 *                     XMLStreamException} or {@link SPFormatException}
	 *
	 * @param file the file to read
	 * @param warner the Warning instance to use for warnings
	 */
	IMutableMapNG readMap(Path file, Warning warner)
		throws SPFormatException, MissingFileException, MalformedXMLException, IOException;

	/**
	 * Read a map from a {@link Reader}.
	 *
	 * @throws SPFormatException if the reader can't handle this map
	 *                           version, doesn't recognize the map format,
	 *                           or finds the file contains format errors
	 * @throws XMLStreamException on low-level XML errors
	 *
	 * @param file the name of the file the stream represents
	 * @param istream the reader to read from
	 * @param warner the Warning instance to use for warnings
	 */
	IMutableMapNG readMapFromStream(Path file, Reader istream, Warning warner)
		throws SPFormatException, MalformedXMLException, IOException;
}
