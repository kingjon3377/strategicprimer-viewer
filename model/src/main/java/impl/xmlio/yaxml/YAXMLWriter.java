package impl.xmlio.yaxml;

import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.io.IOException;

import common.map.IMapNG;
import impl.xmlio.SPWriter;
import java.nio.file.Path;
import java.nio.file.Files;
import lovelace.util.ThrowingConsumer;

/**
 * Sixth generation SP XML writer.
 */
public class YAXMLWriter implements SPWriter {
	private final YAReaderAdapter wrapped = new YAReaderAdapter();

	/**
	 * Write an object to a file.
	 *
	 * @throws IOException on I/O error
	 * @param arg The file to write to
	 * @param obj The object to write
	 */
	@Override
	public void writeSPObject(final Path arg, final Object obj) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(arg, StandardCharsets.UTF_8)) {
			writeSPObject(writer::write, obj);
		}
	}

	/**
	 * Write an object to a stream.
	 *
	 * @throws IOException on I/O error
	 * @param arg The stream to write to
	 * @param obj The object to write
	 */
	@Override
	public void writeSPObject(final ThrowingConsumer<String, IOException> arg, final Object obj) throws IOException {
		wrapped.write(arg, obj, 0);
	}

	/**
	 * Write a map to a file.
	 *
	 * @throws IOException on I/O error
	 * @param arg The file to write to.
	 * @param map The map to write.
	 */
	@Override
	public void write(final Path arg, final IMapNG map) throws IOException {
		writeSPObject(arg, map);
	}

	/**
	 * Write a map to a file or stream.
	 *
	 * @throws IOException on I/O error
	 * @param arg The file to write to.
	 * @param map The map to write.
	 */
	@Override
	public void write(final ThrowingConsumer<String, IOException> arg, final IMapNG map) throws IOException {
		writeSPObject(arg, map);
	}
}
