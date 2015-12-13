package controller.map.misc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import controller.map.cxml.CompactXMLReader;
import controller.map.cxml.CompactXMLWriter;
import controller.map.drivers.ISPDriver.DriverFailedException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.IMapReader;
import controller.map.iointerfaces.SPWriter;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import model.misc.SimpleMultiMapModel;
import model.viewer.ViewerModel;
import util.NullCleaner;
import util.Pair;
import util.Warning;

/**
 * An adapter, so that classes using map readers and writers don't have to
 * change whenever the map reader or writer is replaced.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class MapReaderAdapter {
	/**
	 * The implementation we use under the hood.
	 */
	private final IMapReader reader;
	/**
	 * The map writer implementation we use under the hood.
	 */
	private final SPWriter writer;

	/**
	 * Constructor.
	 */
	public MapReaderAdapter() {
		reader = new CompactXMLReader();
		writer = new CompactXMLWriter();
	}

	/**
	 * @param file
	 *            the file to open
	 * @param warner
	 *            the Warning instance to use for warnings.
	 * @return the map it contains
	 * @throws IOException
	 *             on I/O error opening the file
	 * @throws XMLStreamException
	 *             if the XML is badly formed
	 * @throws SPFormatException
	 *             if the reader can't handle this map version or there are map
	 *             format errors
	 */
	public IMutableMapNG readMap(final File file, final Warning warner)
			throws IOException, XMLStreamException, SPFormatException {
		return reader.readMap(file, warner);
	}

	/**
	 * Because this is intended to be used by implementations of ISPDriver,
	 * which can only throw DriverFailedException, we use that class for all
	 * errors we generate.
	 *
	 * @param file
	 *            the file to open
	 * @param warner
	 *            the Warning instance to use for warnings
	 * @return a driver model containing the map described by that file
	 */
	public IDriverModel readMapModel(final File file, final Warning warner)
			throws DriverFailedException {
		try {
			return new ViewerModel(readMap(file, warner), file);
		} catch (IOException except) {
			throw new DriverFailedException("I/O error reading " + file.getPath(), except);
		} catch (XMLStreamException except) {
			throw new DriverFailedException("Malformed XML in " + file.getPath(), except);
		} catch (SPFormatException except) {
			throw new DriverFailedException("SP map format error in " + file.getPath(), except);
		}
	}
	/**
	 * Read several maps into a driver model.
	 * Because this is intended to be used by implementations of ISPDriver,
	 * which can only throw DriverFailedException, we use that class for all
	 * errors we generate.
	 *
	 * @param files
	 *            the files to open
	 * @param warner
	 *            the Warning instance to use for warnings
	 * @return a driver model containing the maps described by those files
	 */
	public IMultiMapModel readMultiMapModel(final Warning warner, final File master, final File... files) throws DriverFailedException {
		String current = master.getPath();
		try {
			IMultiMapModel retval = new SimpleMultiMapModel(readMap(master, warner), master);
			for (File file : files) {
				current = file.getPath();
				retval.addSubordinateMap(readMap(file, warner), file);
			}
			return retval;
		} catch (IOException except) {
			throw new DriverFailedException("I/O error reading from file" + current, except);
		} catch (XMLStreamException except) {
			throw new DriverFailedException("Malformed XML in " + current, except);
		} catch (SPFormatException except) {
			throw new DriverFailedException("SP map format error in " + current, except);
		}
	}
	/**
	 * Write a map.
	 * @param file the file to write to
	 * @param map the map to write
	 * @throws IOException on error opening the file
	 */
	public void write(final File file, final IMapNG map) throws IOException {
		writer.write(file, map);
	}
	/**
	 * Because this is intended to be used by implementations of ISPDriver,
	 * which can only throw DriverFailedException, we use that class for all
	 * errors we generate.
	 * Write maps from a map model back to file.
	 * @param model the model to write from
	 * @throws IOException on error opening or writing to a file
	 */
	public void writeModel(final IDriverModel model) throws DriverFailedException {
		try {
			writer.write(model.getMapFile(), model.getMap());
		} catch (IOException except) {
			throw new DriverFailedException("I/O error writing to " + model.getMapFile(), except);
		}
		if (model instanceof IMultiMapModel) {
			for (Pair<IMutableMapNG, File> pair : ((IMultiMapModel) model).getSubordinateMaps()) {
				try {
					writer.write(pair.second(), pair.first());
				} catch (IOException except) {
					throw new DriverFailedException("I/O error writing to " + pair.second(), except);
				}
			}
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MapReaderAdapter";
	}
	/**
	 * @param names some filenames
	 * @param dropFirst whether to skip the first filename.
	 * @return an array of equivalent Files
	 */
	public static File[] namesToFiles(final boolean dropFirst, final String... names) {
		final List<File> retval = new ArrayList<>();
		for (String name : names) {
			retval.add(new File(name));
		}
		if (dropFirst) {
			retval.remove(0);
		}
		return NullCleaner.assertNotNull(retval.toArray(new File[retval.size()]));
	}
}
