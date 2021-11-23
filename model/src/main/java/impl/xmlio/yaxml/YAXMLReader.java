package impl.xmlio.yaxml;

import java.io.Reader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.StartElement;

import lovelace.util.IteratorWrapper;
import lovelace.util.MissingFileException;
import lovelace.util.MalformedXMLException;
import lovelace.util.TypesafeXMLEventReader;

import common.idreg.IDRegistrar;
import common.idreg.IDFactory;
import common.map.IMutableMapNG;
import impl.xmlio.IMapReader;
import impl.xmlio.ISPReader;
import common.xmlio.SPFormatException;
import common.xmlio.Warning;

import java.nio.charset.StandardCharsets;

/**
 * Sixth-generation SP XML reader.
 */
public class YAXMLReader implements IMapReader, ISPReader {
	/**
	 * Read an object from XML.
	 *
	 * @throws MalformedXMLException if the XML isn't well-formed
	 * @throws SPFormatException on SP XML format error
	 * @param file The file we're reading from
	 * @param istream The stream to read from
	 * @param warner The Warning instance to use for warnings
	 * @throws ClassCastException if reader does not produce the requested type
	 *
	 * TODO: take Class so we can check and throw a different exception? In
	 * Ceylon we did <code>assert (is Element retval</code>
	 */
	@Override
	public <Element> Element readXML(Path file, Reader istream, Warning warner)
			throws SPFormatException, MalformedXMLException, IOException {
		try (TypesafeXMLEventReader reader = new TypesafeXMLEventReader(istream)) {
			Iterable<XMLEvent> eventReader = new IteratorWrapper(reader);
			IDRegistrar idFactory = new IDFactory();
			for (XMLEvent event : eventReader) {
				if (event instanceof StartElement) { // TODO: Check namespace, surely?
					return (Element) new YAReaderAdapter(warner, idFactory)
						.parse((StartElement) event, new QName("root"), eventReader);
				}
			}
		}
		throw new MalformedXMLException("XML stream didn't contain a start element");
	}

	/**
	 * Read a map from a stream.
	 *
	 * @throws MalformedXMLException on malformed XML
	 * @throws SPFormatException on SP format problems
	 * @param file The file we're reading from
	 * @param istream The stream to read from
	 * @param warner The Warning instance to use for warnings
	 * @throws ClassCastException if reader does not produce the requested type
	 */
	@Override
	public IMutableMapNG readMapFromStream(Path file, Reader istream, Warning warner)
			throws SPFormatException, MalformedXMLException, IOException {
		return this.<IMutableMapNG>readXML(file, istream, warner);
	}

	/**
	 * Read a map from XML.
	 *
	 * @throws IOException on I/O error
	 * @throws MalformedXMLException on malformed XML
	 * @throws SPFormatException on SP format problems
	 * @param file The file to read from
	 * @param warner The Warning instance to use for warnings
	 */
	@Override
	public IMutableMapNG readMap(Path file, Warning warner)
			throws IOException, MalformedXMLException, SPFormatException, MissingFileException {
		try (Reader istream = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			return readMapFromStream(file, istream, warner);
		} catch (FileNotFoundException|NoSuchFileException except) {
			throw new MissingFileException(file, except);
		}
	}
}
