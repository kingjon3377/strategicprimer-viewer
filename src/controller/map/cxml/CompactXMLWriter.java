package controller.map.cxml;

import controller.map.iointerfaces.SPWriter;
import java.io.IOException;
import java.nio.file.Path;
import model.map.IMapNG;

/**
 * CompactXML's Writer implementation.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 * @deprecated CompactXML is deprecated in favor of FluidXML
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Deprecated
public final class CompactXMLWriter implements SPWriter {
	/**
	 * Write a map to file.
	 *
	 * @param file The file to write to
	 * @param map  the map to write
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Path file, final IMapNG map) throws IOException {
		writeSPObject(file, map);
	}

	/**
	 * Write a map to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param map     the map to write
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final IMapNG map)
			throws IOException {
		writeSPObject(ostream, map);
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj     the object to write
	 * @throws IOException on I/O error
	 */
	@Override
	public void writeSPObject(final Appendable ostream, final Object obj)
			throws IOException {
		CompactReaderAdapter.write(ostream, obj, 0);
	}
}
