package impl.xmlio;

import java.io.IOException;
import java.nio.file.Path;
import javax.xml.stream.XMLStreamException;

import lovelace.util.ThrowingConsumer;

import legacy.map.IMapNG;

/**
 * An interface for map (and other SP XML) writers.
 */
public interface SPWriter {
    /**
     * Write a map to file
     * @param file The file or stream to write to.
     * @param map The map to write
     */
    void write(Path file, IMapNG map) throws XMLStreamException, IOException;

    /**
     * Write a map to a stream.
     * @param stream The file or stream to write to.
     * @param map The map to write
     */
    void write(ThrowingConsumer<String, IOException> stream, IMapNG map) throws XMLStreamException, IOException;

    /**
     * Write an object to a file.
     */
    void writeSPObject(Path file, Object obj) throws XMLStreamException, IOException;

    /**
     * Write an object to a file or stream.
     */
    void writeSPObject(ThrowingConsumer<String, IOException> stream, Object obj) throws XMLStreamException, IOException;
}
