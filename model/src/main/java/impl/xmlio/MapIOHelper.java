package impl.xmlio;

import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.logging.Logger;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import java.io.Reader;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;

import impl.dbio.SPDatabaseWriter;
import impl.dbio.SPDatabaseReader;
import common.xmlio.SPFormatException;
import common.map.IMutableMapNG;
import common.map.IMapNG;
import impl.xmlio.fluidxml.SPFluidReader;
import impl.xmlio.yaxml.YAXMLWriter;
import common.xmlio.Warning;

/**
 * A helper to abstract the details of specific I/O implementations to shield
 * callers from them, and in particular to encapsulate the decision of which
 * implementation to use in one place.
 */
public final class MapIOHelper {
	/**
	 * A logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(MapIOHelper.class.getName());

	private MapIOHelper() {
	}

	/**
	 * The reader to use to read from XML. (The FluidXML implementation
	 * turned out to be significantly faster than YAXML, which was written
	 * to replace it ...)
	 */
	public static final IMapReader READER = new SPFluidReader();

	/**
	 * The writer to use to write to XML.
	 */
	public static final SPWriter WRITER = new YAXMLWriter();

	/**
	 * The writer to use to write to SQLite databases.
	 */
	public static final SPWriter DB_WRITER = new SPDatabaseWriter();

	/**
	 * The reader to use to read from SQLite databases.
	 */
	public static final IMapReader DB_READER = new SPDatabaseReader();

	/**
	 * Turn a series of Strings into a series of equvalent Paths.
	 */
	public static List<Path> namesToFiles(final String... names) {
		return Stream.of(names).map(Paths::get).collect(Collectors.toList());
	}

	/**
	 * Read a map from a file.
	 */
	public static IMutableMapNG readMap(final Path file)
			throws SPFormatException, IOException, NoSuchFileException, XMLStreamException {
		return readMap(file, Warning.WARN);
	}

	/**
	 * Read a map from a file.
	 */
	public static IMutableMapNG readMap(final Path file, final Warning warner)
			throws SPFormatException, IOException, NoSuchFileException, XMLStreamException {
		LOGGER.fine("In mapIOHelper.readMap");
		final IMutableMapNG retval;
		if (file.toString().endsWith(".db")) {
			LOGGER.fine(String.format("Reading from %s as an SQLite database",
				file.toString()));
			retval = DB_READER.readMap(file, warner);
		} else {
			LOGGER.fine(String.format("Reading from %s", file.toString()));
			retval = READER.readMap(file, warner);
		}
		retval.setFilename(file);
		LOGGER.fine(String.format("Finished reading from %s", file.toString()));
		return retval;
	}

	/**
	 * Read a map from a stream.
	 */
	public static IMutableMapNG readMap(final Reader stream)
			throws SPFormatException, XMLStreamException, IOException {
		return readMap(stream, Warning.WARN);
	}

	/**
	 * Read a map from a stream.
	 */
	public static IMutableMapNG readMap(final Reader stream, final Warning warner)
			throws SPFormatException, XMLStreamException, IOException {
		LOGGER.fine("In mapIOHelper.readMap");
		LOGGER.fine("Reading from a Reader");
		return READER.readMapFromStream(Paths.get(""), stream, warner);
	}

	/**
	 * Write a map to file.
	 */
	public static void writeMap(final Path file, final IMapNG map) throws IOException, XMLStreamException {
		if (file.toString().endsWith(".db") || file.toString().isEmpty()) {
			LOGGER.fine(String.format("Writing to %s as an SQLite database", file.toString()));
			DB_WRITER.write(file, map);
		} else {
			LOGGER.fine(String.format("Writing to %s", file.toString()));
			WRITER.write(file, map);
		}
	}
}
