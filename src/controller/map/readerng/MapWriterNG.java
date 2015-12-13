package controller.map.readerng;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import controller.map.iointerfaces.SPWriter;
import model.map.IMapNG;

/**
 * Entry point for the new map writing framework.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public final class MapWriterNG implements SPWriter {
	/**
	 * Write a map.
	 *
	 * @param file the file to write to
	 * @param map the map to write.
	 * @throws IOException on error opening the file
	 */
	@Override
	public void write(final File file, final IMapNG map) throws IOException {
		writeObject(file, map);
	}
	/**
	 * Write a map.
	 *
	 * @param ostream the Writer to write to
	 * @param map the map to write.
	 * @throws IOException on I/O error in writing
	 */
	@Override
	public void write(final Appendable ostream, final IMapNG map)
			throws IOException {
		writeObject(ostream, map);
	}
	/**
	 * Write a SP object.
	 *
	 * @param file the file to write to
	 * @param obj the object to write.
	 * @throws IOException on error opening the file
	 */
	public static void writeObject(final File file, final Object obj)
			throws IOException {
		try (final Writer writer = new FileWriter(file)) {
			writeObject(writer, obj);
		}
	}
	/**
	 * Write a SP object.
	 *
	 * @param ostream the Writer to write to
	 * @param obj the object to write.
	 * @throws IOException on I/O error in writing
	 */
	public static void writeObject(final Appendable ostream, final Object obj)
			throws IOException {
		ReaderAdapter.ADAPTER.write(obj).write(ostream, 0);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MapWriterNG";
	}
}
