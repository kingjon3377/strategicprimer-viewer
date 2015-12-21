package controller.map.iointerfaces;

import model.map.IMapNG;

import java.io.File;
import java.io.IOException;

/**
 * An interface for map (and other SP XML) writers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
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
	void write(File file, IMapNG map) throws IOException;

	/**
	 * Write a map.
	 *
	 * @param ostream the writer to write to
	 * @param map     the map to write
	 * @throws IOException on error in writing
	 */
	void write(Appendable ostream, IMapNG map) throws IOException;

}
