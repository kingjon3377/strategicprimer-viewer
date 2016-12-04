package controller.map.iointerfaces;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import model.map.IMapNG;

/**
 * An interface for map (and other SP XML) writers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface SPWriter {
	/**
	 * Write a map.
	 *
	 * @param file the file to write to
	 * @param map  the map to write.
	 * @throws IOException on error opening the file
	 */
	void write(Path file, IMapNG map) throws IOException;

	/**
	 * Write a map.
	 *
	 * @param ostream the writer to write to
	 * @param map     the map to write
	 * @throws IOException on error in writing
	 */
	void write(Appendable ostream, IMapNG map) throws IOException;

	/**
	 * Write an object to file.
	 *
	 * @param filename the file to write to
	 * @param obj      the object to write
	 * @throws IOException on I/O error
	 */
	default void writeSPObject(final String filename, final Object obj)
			throws IOException {
		writeSPObject(Paths.get(filename), obj);
	}

	/**
	 * Write an object to file.
	 *
	 * @param file the file to write to
	 * @param obj  the object to write
	 * @throws IOException on I/O error
	 */
	default void writeSPObject(final Path file, final Object obj) throws IOException {
		try (final Writer writer = Files.newBufferedWriter(file)) {
			writeSPObject(writer, obj);
		}
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj     the object to write
	 * @throws IOException on I/O error
	 */
	void writeSPObject(final Appendable ostream, final Object obj) throws IOException;
}
