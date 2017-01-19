package controller.map.misc;

import controller.map.drivers.DriverFailedException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.IMapReader;
import controller.map.iointerfaces.SPWriter;
import controller.map.yaxml.YAXMLReader;
import controller.map.yaxml.YAXMLWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import model.misc.SimpleMultiMapModel;
import model.viewer.ViewerModel;
import util.Pair;
import util.TypesafeLogger;
import util.Warning;

/**
 * An adapter, so that classes using map readers and writers don't have to change whenever
 * the map reader or writer is replaced.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class MapReaderAdapter {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(MapReaderAdapter
																		  .class);
	/**
	 * The implementation we use under the hood.
	 */
	private final IMapReader reader;
	/**
	 * The map writer implementation we use under the hood.
	 */
	private final SPWriter spWriter;

	/**
	 * Constructor.
	 */
	public MapReaderAdapter() {
		reader = new YAXMLReader();
		spWriter = new YAXMLWriter();
	}

	/**
	 * Turn a series of Strings to an array of equivalent Files, optionally omitting the
	 * first.
	 * @param names     some filenames
	 * @param dropFirst whether to skip the first filename.
	 * @return an array of equivalent Files
	 */
	public static Path[] namesToFiles(final boolean dropFirst, final String... names) {
		final int skip;
		if (dropFirst) {
			skip = 1;
		} else {
			skip = 0;
		}
		return Stream.of(names).map(Paths::get).skip(skip).toArray(Path[]::new);
	}

	/**
	 * Read a map from a file.
	 * @param file   the file to open
	 * @param warner the Warning instance to use for warnings.
	 * @return the map it contains
	 * @throws IOException        on I/O error opening the file
	 * @throws XMLStreamException if the XML is badly formed
	 * @throws SPFormatException  if the reader can't handle this map version or there
	 * are map format errors
	 */
	public IMutableMapNG readMap(final Path file, final Warning warner)
			throws IOException, XMLStreamException, SPFormatException {
		return reader.readMap(file, warner);
	}

	/**
	 * Read a map from a stream.
	 * @param stream the stream to read the map from
	 * @param warner the Warning instance to use for warnings.
	 * @return the map it contains
	 * @throws XMLStreamException if the XML is badly formed
	 * @throws SPFormatException  if the reader can't handle this map version or there
	 * are map format errors
	 */
	public IMutableMapNG readMapFromStream(final Reader stream, final Warning warner)
			throws XMLStreamException, SPFormatException {
		return reader.readMap(Paths.get(""), stream, warner);
	}

	/**
	 * Read a map model from a stream. Because this is parallel to readMapModel(), we
	 * wrap any errors we generate in a DriverFailedException.
	 *
	 * @param stream the stream to read a map from
	 * @param warner the Warning instance to use for warnings.
	 * @return a driver model containing the map contained in the stream
	 * @throws DriverFailedException on any error
	 */
	public IDriverModel readMapModelFromStream(final Reader stream, final Warning warner)
			throws DriverFailedException {
		try {
			return new ViewerModel(readMapFromStream(stream, warner), Optional.empty());
		} catch (final XMLStreamException except) {
			throw new DriverFailedException("Malformed XML in stream", except);
		} catch (final SPFormatException except) {
			throw new DriverFailedException("SP map format error in stream", except);
		}
	}

	/**
	 * Because this is intended to be used by implementations of ISPDriver, which can
	 * only throw DriverFailedException, we use that class for all errors we generate.
	 *
	 * @param file   the file to open
	 * @param warner the Warning instance to use for warnings
	 * @return a driver model containing the map described by that file
	 * @throws DriverFailedException on any error
	 */
	public IDriverModel readMapModel(final Path file, final Warning warner)
			throws DriverFailedException {
		try {
			return new ViewerModel(readMap(file, warner), Optional.of(file));
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			throw new DriverFailedException("I/O error reading " + file,
												   except);
		} catch (final XMLStreamException except) {
			throw new DriverFailedException("Malformed XML in " + file,
												   except);
		} catch (final SPFormatException except) {
			throw new DriverFailedException("SP map format error in " + file,
												   except);
		}
	}

	/**
	 * Read several maps into a driver model. Because this is intended to be used by
	 * implementations of ISPDriver, which can only throw DriverFailedException, we use
	 * that class for all errors we generate.
	 *
	 * @param master the "master" map file
	 * @param files  the files to open
	 * @param warner the Warning instance to use for warnings
	 * @return a driver model containing the maps described by those files
	 * @throws DriverFailedException on any error
	 */
	public IMultiMapModel readMultiMapModel(final Warning warner, final Path master,
											final Path... files)
			throws DriverFailedException {
		String current = master.toString();
		try {
			final IMultiMapModel retval =
					new SimpleMultiMapModel(readMap(master, warner),
												   Optional.of(master));
			for (final Path file : files) {
				current = file.toString();
				retval.addSubordinateMap(readMap(file, warner), Optional.of(file));
			}
			return retval;
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			throw new DriverFailedException("I/O error reading from file " + current,
												   except);
		} catch (final XMLStreamException except) {
			throw new DriverFailedException("Malformed XML in " + current, except);
		} catch (final SPFormatException except) {
			throw new DriverFailedException("SP map format error in " + current, except);
		}
	}

	/**
	 * Write a map.
	 *
	 * @param file the file to write to
	 * @param map  the map to write
	 * @throws IOException on error opening the file
	 */
	public void write(final Path file, final IMapNG map)
			throws IOException {
		spWriter.write(file, map);
	}

	/**
	 * Because this is intended to be used by implementations of ISPDriver, which can
	 * only
	 * throw DriverFailedException, we use that class for all errors we generate. Write
	 * maps from a map model back to file.
	 *
	 * @param model the model to write from
	 * @throws DriverFailedException on any error
	 */
	public void writeModel(final IDriverModel model) throws DriverFailedException {
		final Optional<Path> mainFile = model.getMapFile();
		if (mainFile.isPresent()) {
			try {
				spWriter.write(mainFile.get(), model.getMap());
			} catch (final IOException except) {
				//noinspection HardcodedFileSeparator
				throw new DriverFailedException("I/O error writing to " + mainFile.get(),
													   except);
			}
		} else {
			LOGGER.severe(
					"Model didn't contain filename for main map, so didn't write it");
		}
		if (model instanceof IMultiMapModel) {
			for (final Pair<IMutableMapNG, Optional<Path>> pair :
					((IMultiMapModel) model).getSubordinateMaps()) {
				final Optional<Path> filename = pair.second();
				if (filename.isPresent()) {
					try {
						spWriter.write(filename.get(), pair.first());
					} catch (final IOException except) {
						//noinspection HardcodedFileSeparator
						throw new DriverFailedException("I/O error writing to " +
																filename, except);
					}
				} else {
					LOGGER.severe("A map didn't have a filename, and so wasn't written");
				}
			}
		}
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "MapReaderAdapter";
	}
}
