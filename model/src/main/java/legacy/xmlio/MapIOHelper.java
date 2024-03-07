package legacy.xmlio;

import java.nio.file.NoSuchFileException;
import java.util.List;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import java.io.Reader;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;

import legacy.dbio.SPDatabaseWriter;
import legacy.dbio.SPDatabaseReader;
import common.xmlio.SPFormatException;
import legacy.map.IMutableLegacyMap;
import legacy.map.ILegacyMap;
import impl.xmlio.SPWriter;
import legacy.xmlio.fluidxml.SPFluidReader;
import legacy.xmlio.yaxml.YAXMLWriter;
import common.xmlio.Warning;
import lovelace.util.LovelaceLogger;

/**
 * A helper to abstract the details of specific I/O implementations to shield
 * callers from them, and in particular to encapsulate the decision of which
 * implementation to use in one place.
 */
public final class MapIOHelper {
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
	public static IMutableLegacyMap readMap(final Path file)
			throws SPFormatException, IOException, NoSuchFileException, XMLStreamException {
		return readMap(file, Warning.WARN);
	}

	/**
	 * Read a map from a file.
	 */
	public static IMutableLegacyMap readMap(final Path file, final Warning warner)
			throws SPFormatException, IOException, NoSuchFileException, XMLStreamException {
		LovelaceLogger.debug("In mapIOHelper.readMap");
		final IMutableLegacyMap retval;
		if (file.toString().endsWith(".db")) {
			LovelaceLogger.debug("Reading from %s as an SQLite database",
					file.toString());
			retval = DB_READER.readMap(file, warner);
		} else {
			LovelaceLogger.debug("Reading from %s", file);
			retval = READER.readMap(file, warner);
		}
		retval.setFilename(file);
		LovelaceLogger.debug("Finished reading from %s", file);
		return retval;
	}

	/**
	 * Read a map from a stream.
	 */
	public static IMutableLegacyMap readMap(final Reader stream)
			throws SPFormatException, XMLStreamException, IOException {
		return readMap(stream, Warning.WARN);
	}

	/**
	 * Read a map from a stream.
	 */
	public static IMutableLegacyMap readMap(final Reader stream, final Warning warner)
			throws SPFormatException, XMLStreamException, IOException {
		LovelaceLogger.debug("In mapIOHelper.readMap");
		LovelaceLogger.debug("Reading from a Reader");
		return READER.readMapFromStream(Paths.get(""), stream, warner);
	}

	/**
	 * Write a map to file.
	 */
	public static void writeMap(final Path file, final ILegacyMap map) throws IOException, XMLStreamException {
		if (file.toString().endsWith(".db") || file.toString().isEmpty()) {
			LovelaceLogger.debug("Writing to %s as an SQLite database", file);
			DB_WRITER.write(file, map);
		} else {
			LovelaceLogger.debug("Writing to %s", file);
			WRITER.write(file, map);
		}
	}
}
