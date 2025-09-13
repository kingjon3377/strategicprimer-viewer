package drivers;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

import common.xmlio.SPFormatException;
import common.xmlio.Warning;
import legacy.xmlio.MapIOHelper;
import drivers.common.IDriverModel;
import drivers.common.DriverFailedException;
import drivers.common.IMultiMapModel;
import drivers.common.SimpleDriverModel;
import drivers.common.SimpleMultiMapModel;

import legacy.map.ILegacyMap;
import legacy.map.IMutableLegacyMap;
import lovelace.util.LovelaceLogger;

/**
 * A collection of a few methods for reading and writing map models, adding an
 * additional layer of caller convenience on top of {@link MapIOHelper}.
 *
 * TODO: Evaluate whether this is really desirable now that driver factories
 * have their own type-specific model factory methods. Maybe take the factory
 * methods as parameters in the reading methods here?
 */
public final class MapReaderAdapter {
	private MapReaderAdapter() {
	}

	/**
	 * Read a map from a file, wrapping any errors the process generates in
	 * a {@link DriverFailedException} it throws instead. (It was returned
	 * in Ceylon, but there's no easy way to do that in Java.)
	 */
	public static IMutableLegacyMap readMap(final Path file, final Warning warner) throws DriverFailedException {
		try {
			return MapIOHelper.readMap(file, warner);
		} catch (final FileNotFoundException | NoSuchFileException except) {
			throw new DriverFailedException(except, "File not found");
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			throw new DriverFailedException(except, "I/O error while reading");
		} catch (final XMLStreamException except) {
			throw new DriverFailedException(except, "Malformed XML");
		} catch (final SPFormatException except) {
			throw new DriverFailedException(except, "SP map format error");
		}
	}

	/**
	 * Read a map model from a file, wrapping any errors the
	 * process generates in a (thrown) {@link DriverFailedException} to
	 * simplify callers.
	 */
	public static IDriverModel readMapModel(final Path file, final Warning warner) throws DriverFailedException {
		try {
			return new SimpleDriverModel(MapIOHelper.readMap(file, warner));
		} catch (final FileNotFoundException | NoSuchFileException except) {
			throw new DriverFailedException(except, "File not found");
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			throw new DriverFailedException(except, "I/O error while reading");
		} catch (final XMLStreamException except) {
			throw new DriverFailedException(except, "Malformed XML");
		} catch (final SPFormatException except) {
			throw new DriverFailedException(except, "SP map format error");
		}
	}

	/**
	 * Read a map model from a file or a stream, wrapping any errors the
	 * process generates in a (thrown) {@link DriverFailedException} to
	 * simplify callers.
	 */
	public static IDriverModel readMapModel(final Reader stream, final Warning warner) throws DriverFailedException {
		try {
			return new SimpleDriverModel(MapIOHelper.readMap(stream, warner));
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			throw new DriverFailedException(except, "I/O error while reading");
		} catch (final XMLStreamException except) {
			throw new DriverFailedException(except, "Malformed XML");
		} catch (final SPFormatException except) {
			throw new DriverFailedException(except, "SP map format error");
		}
	}

	/**
	 * Read several maps into a driver model, wrapping any errors in a
	 * (thrown) DriverFailedException to simplify callers.
	 */
	public static IMultiMapModel readMultiMapModel(final Warning warner, final Path master, final Path... files)
			throws DriverFailedException {
		LovelaceLogger.trace("In MapReaderAdapter.readMultiMapModel");
		String current = master.toString();
		try {
			final IMultiMapModel retval = new SimpleMultiMapModel(
					MapIOHelper.readMap(master, warner));
			for (final Path file : files) {
				current = file.toString();
				retval.addSubordinateMap(MapIOHelper.readMap(file, warner));
			}
			LovelaceLogger.trace("Finished with mapReaderAdapter.readMultiMapModel");
			return retval;
		} catch (final FileNotFoundException | NoSuchFileException except) {
			// TODO: Catch FNFE as close to source as possible and convert to NoSuchFileException, to preserve
			//  filename as a field of the exception
			throw new DriverFailedException(except, "File not found: " + current);
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			throw new DriverFailedException(except, "I/O error reading from file " + current);
		} catch (final XMLStreamException except) {
			throw new DriverFailedException(except, "Malformed XML in " + current);
		} catch (final SPFormatException except) {
			throw new DriverFailedException(except, "SP map format error in " + current);
		}
	}

	/**
	 * Write maps from a map model back to file, wrapping any errors in a
	 * (thrown) {@link DriverFailedException} to simplify callers.
	 */
	public static void writeModel(final IDriverModel model) throws DriverFailedException {
		final Path mainFile = model.getMap().getFilename();
		if (Objects.isNull(mainFile)) {
			LovelaceLogger.error("Model didn't contain filename for main map, so didn't write it");
		} else {
			try {
				MapIOHelper.writeMap(mainFile, model.getMap());
				model.setMapStatus(ILegacyMap.ModificationStatus.Unmodified);
			} catch (final XMLStreamException except) {
				throw new DriverFailedException(except,
						"Malformed XML while writing " + mainFile);
			} catch (final IOException except) {
				//noinspection HardcodedFileSeparator
				throw new DriverFailedException(except, "I/O error writing to " + mainFile);
			}
		}
		if (model instanceof final IMultiMapModel mmm) {
			for (final ILegacyMap map : mmm.getSubordinateMaps()) {
				final Path filename = map.getFilename();
				if (Objects.isNull(filename)) {
					LovelaceLogger.error("A map didn't have a filename, and so wasn't written.");
				} else {
					try {
						MapIOHelper.writeMap(filename, map);
						mmm.clearModifiedFlag(map);
					} catch (final XMLStreamException except) {
						throw new DriverFailedException(except,
								"Malformed XML while writing " + filename);
					} catch (final IOException except) {
						//noinspection HardcodedFileSeparator
						throw new DriverFailedException(except,
								"I/O error writing to " + filename);
					}
				}
			}
		}
	}
}
