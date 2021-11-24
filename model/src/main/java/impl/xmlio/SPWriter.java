package impl.xmlio;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import common.map.IMapNG;

import lovelace.util.MalformedXMLException;

/**
 * An interface for map (and other SP XML) writers.
 */
public interface SPWriter {
	@FunctionalInterface
	static interface IOConsumer<Type> {
		void accept(Type item) throws IOException;
	}

	/**
	 * Write a map to file
	 * @param file The file or stream to write to.
	 * @param map The map to write
	 */
	void write(Path file, IMapNG map) throws MalformedXMLException, IOException;

	/**
	 * Write a map to a stream.
	 * @param stream The file or stream to write to.
	 * @param map The map to write
	 */
	void write(Consumer<String> stream, IMapNG map) throws MalformedXMLException;

	/**
	 * Write an object to a file.
	 */
	void writeSPObject(Path file, Object obj) throws MalformedXMLException, IOException;

	/**
	 * Write an object to a file or stream.
	 */
	void writeSPObject(Consumer<String> stream, Object obj) throws MalformedXMLException;
}
