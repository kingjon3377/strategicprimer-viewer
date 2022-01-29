package drivers;

import lovelace.util.MissingFileException;
import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.io.Reader;

import lovelace.util.MalformedXMLException;

import common.xmlio.SPFormatException;
import common.xmlio.Warning;
import impl.xmlio.MapIOHelper;
import drivers.common.IDriverModel;
import drivers.common.DriverFailedException;
import drivers.common.IMultiMapModel;
import drivers.common.SimpleDriverModel;
import drivers.common.SimpleMultiMapModel;

import common.map.IMapNG;
import common.map.IMutableMapNG;

/**
 * A collection of a few methods for reading and writing map models, adding an
 * additional layer of caller convenience on top of {@link MapIOHelper}.
 *
 * TODO: Evaluate whether this is really desirable now that driver factories
 * have their own type-specific model factory methods. Maybe take the factory
 * methods as parameters in the reading methods here?
 */
public class MapReaderAdapter {
	private MapReaderAdapter() {}

	private static final Logger LOGGER = Logger.getLogger(MapReaderAdapter.class.getName());

	/**
	 * Read a map from a file, wrapping any errors the process generates in
	 * a {@link DriverFailedException} it throws instead. (It was returned
	 * in Ceylon, but there's no easy way to do that in Java.)
	 */
	public static IMutableMapNG readMap(Path file, Warning warner) throws DriverFailedException {
		try {
			return MapIOHelper.readMap(file, warner);
		} catch (MissingFileException|FileNotFoundException|NoSuchFileException except) {
			throw new DriverFailedException(except, "File not found");
		} catch (IOException except) {
			throw new DriverFailedException(except, "I/O error while reading");
		} catch (MalformedXMLException except) {
			throw new DriverFailedException(except, "Malformed XML");
		} catch (SPFormatException except) {
			throw new DriverFailedException(except, "SP map format error");
		}
	}

	/**
	 * Read a map model from a file, wrapping any errors the
	 * process generates in a (thrown) {@link DriverFailedException} to
	 * simplify callers.
	 */
	public static IDriverModel readMapModel(Path file, Warning warner) throws DriverFailedException {
		try {
			return new SimpleDriverModel(MapIOHelper.readMap(file, warner));
		} catch (MissingFileException|FileNotFoundException|NoSuchFileException except) {
			throw new DriverFailedException(except, "File not found");
		} catch (IOException except) {
			throw new DriverFailedException(except, "I/O error while reading");
		} catch (MalformedXMLException except) {
			throw new DriverFailedException(except, "Malformed XML");
		} catch (SPFormatException except) {
			throw new DriverFailedException(except, "SP map format error");
		}
	}

	/**
	 * Read a map model from a file or a stream, wrapping any errors the
	 * process generates in a (thrown) {@link DriverFailedException} to
	 * simplify callers.
	 */
	public static IDriverModel readMapModel(Reader stream, Warning warner) throws DriverFailedException {
		try {
			return new SimpleDriverModel(MapIOHelper.readMap(stream, warner));
		} catch (IOException except) {
			throw new DriverFailedException(except, "I/O error while reading");
		} catch (MalformedXMLException except) {
			throw new DriverFailedException(except, "Malformed XML");
		} catch (SPFormatException except) {
			throw new DriverFailedException(except, "SP map format error");
		}
	}

	/**
	 * Read several maps into a driver model, wrapping any errors in a
	 * (thrown) DriverFailedException to simplify callers.
	 */
	public static IMultiMapModel readMultiMapModel(Warning warner, Path master, Path... files) 
			throws DriverFailedException {
		LOGGER.finer("In MapReaderAdapter.readMultiMapModel");
		String current = master.toString();
		try {
			IMultiMapModel retval = new SimpleMultiMapModel(
				MapIOHelper.readMap(master, warner));
			for (Path file : files) {
				current = file.toString();
				retval.addSubordinateMap(MapIOHelper.readMap(file, warner));
			}
			LOGGER.finer("Finished with mapReaderAdapter.readMultiMapModel");
			return retval;
		} catch (MissingFileException|FileNotFoundException|NoSuchFileException except) {
			throw new DriverFailedException(except, "File not found: " + current);
		} catch (IOException except) {
			throw new DriverFailedException(except, "I/O error reading from file " + current);
		} catch (MalformedXMLException except) {
			throw new DriverFailedException(except, "Malformed XML in " + current);
		} catch (SPFormatException except) {
			throw new DriverFailedException(except, "SP map format error in " + current);
		}
	}

	/**
	 * Write maps from a map model back to file, wrapping any errors in a
	 * (thrown) {@link DriverFailedException} to simplify callers.
	 */
	public static void writeModel(IDriverModel model) throws DriverFailedException{
		Path mainFile = model.getMap().getFilename();
		if (mainFile != null) { // TODO: invert
			try {
				MapIOHelper.writeMap(mainFile, model.getMap());
				model.setMapModified(false);
			} catch (MalformedXMLException except) {
				throw new DriverFailedException(except,
					"Malformed XML while writing " + mainFile);
			} catch (IOException except) {
				throw new DriverFailedException(except, "I/O error writing to " + mainFile);
			}
		} else {
			LOGGER.severe("Model didn't contain filename for main map, so didn't write it");
		}
		if (model instanceof IMultiMapModel) {
			for (IMapNG map : ((IMultiMapModel) model).getSubordinateMaps()) {
				Path filename = map.getFilename();
				if (filename != null) { // TODO: invert (and 'continue' to reduce indent)
					try {
						MapIOHelper.writeMap(filename, map);
						((IMultiMapModel) model).clearModifiedFlag(map);
					} catch (MalformedXMLException except) {
						throw new DriverFailedException(except,
							"Malformed XML while writing " + filename);
					} catch (IOException except) {
						throw new DriverFailedException(except,
							"I/O error writing to " + filename);
					}
				} else {
					LOGGER.severe("A map didn't have a filename, and so wasn't written.");
				}
			}
		}
	}
}
