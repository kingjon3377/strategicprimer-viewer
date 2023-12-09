package legacy.xmlio.yaxml;

import java.io.Reader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.StartElement;

import lovelace.util.IteratorWrapper;
import lovelace.util.TypesafeXMLEventReader;

import legacy.idreg.IDRegistrar;
import legacy.idreg.IDFactory;
import legacy.map.IMutableLegacyMap;
import legacy.xmlio.IMapReader;
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
     * @throws XMLStreamException if the XML isn't well-formed
     * @throws SPFormatException on SP XML format error
     * @param file The file we're reading from
     * @param istream The stream to read from
     * @param warner The Warning instance to use for warnings
     * @throws ClassCastException if reader does not produce the requested type
     *
     * TODO: take Class so we can check and throw a different exception? In
     * Ceylon we did {@code assert (is Element retval}
     */
    @Override
    public <Element> Element readXML(final Path file, final Reader istream, final Warning warner)
            throws SPFormatException, XMLStreamException, IOException {
        try (final TypesafeXMLEventReader reader = new TypesafeXMLEventReader(istream)) {
            final Iterable<XMLEvent> eventReader = new IteratorWrapper<>(reader);
            final IDRegistrar idFactory = new IDFactory();
            for (final XMLEvent event : eventReader) {
                if (event instanceof final StartElement se) { // TODO: Check namespace, surely?
                    return (Element) new YAReaderAdapter(warner, idFactory)
                            .parse(se, new QName("root"), eventReader);
                }
            }
        }
        throw new XMLStreamException("XML stream didn't contain a start element");
    }

    /**
     * Read a map from a stream.
     *
     * @throws XMLStreamException on malformed XML
     * @throws SPFormatException on SP format problems
     * @param file The file we're reading from
     * @param istream The stream to read from
     * @param warner The Warning instance to use for warnings
     * @throws ClassCastException if reader does not produce the requested type
     */
    @Override
    public IMutableLegacyMap readMapFromStream(final Path file, final Reader istream, final Warning warner)
            throws SPFormatException, XMLStreamException, IOException {
        return readXML(file, istream, warner);
    }

    /**
     * Read a map from XML.
     *
     * @throws IOException on I/O error
     * @throws XMLStreamException on malformed XML
     * @throws SPFormatException on SP format problems
     * @param file The file to read from
     * @param warner The Warning instance to use for warnings
     */
    @Override
    public IMutableLegacyMap readMap(final Path file, final Warning warner)
            throws SPFormatException, NoSuchFileException, XMLStreamException, IOException {
        try (final Reader istream = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return readMapFromStream(file, istream, warner);
        } catch (final FileNotFoundException except) {
            final NoSuchFileException wrapper = new NoSuchFileException(file.toString());
            wrapper.initCause(except);
            throw wrapper;
        }
    }
}
