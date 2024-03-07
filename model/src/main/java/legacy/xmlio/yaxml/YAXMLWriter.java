package legacy.xmlio.yaxml;

import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.io.IOException;

import legacy.map.ILegacyMap;
import impl.xmlio.SPWriter;

import java.nio.file.Path;
import java.nio.file.Files;
import javax.xml.stream.XMLStreamException;

import lovelace.util.ThrowingConsumer;

/**
 * Sixth generation SP XML writer.
 */
public class YAXMLWriter implements SPWriter {
	private final YAReaderAdapter wrapped = new YAReaderAdapter();

	/**
	 * Write an object to a file.
	 *
	 * @param arg The file to write to
	 * @param obj The object to write
	 * @throws IOException on I/O error
	 */
	@Override
	public void writeSPObject(final Path arg, final Object obj) throws XMLStreamException, IOException {
		try (final BufferedWriter writer = Files.newBufferedWriter(arg, StandardCharsets.UTF_8)) {
			writeSPObject(writer::write, obj);
		}
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param arg The stream to write to
	 * @param obj The object to write
	 * @throws IOException on I/O error
	 */
	@Override
	public void writeSPObject(final ThrowingConsumer<String, IOException> arg, final Object obj) throws XMLStreamException, IOException {
		wrapped.write(arg, obj, 0);
	}

	/**
	 * Write a map to a file.
	 *
	 * @param arg The file to write to.
	 * @param map The map to write.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Path arg, final ILegacyMap map) throws XMLStreamException, IOException {
		writeSPObject(arg, map);
	}

	/**
	 * Write a map to a file or stream.
	 *
	 * @param arg The file to write to.
	 * @param map The map to write.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final ThrowingConsumer<String, IOException> arg, final ILegacyMap map)
			throws XMLStreamException, IOException {
		writeSPObject(arg, map);
	}
}
