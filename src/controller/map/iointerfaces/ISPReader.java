package controller.map.iointerfaces;

import controller.map.formatexceptions.SPFormatException;
import util.NullCleaner;
import util.Warning;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;

/**
 * An interface for readers of any SP model type.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
public interface ISPReader {
	/**
	 * Tags we expect to use in the future; they are skipped for now and we'll warn if
	 * they're used.
	 */
	Iterable<String> FUTURE =
			NullCleaner.assertNotNull(Collections.unmodifiableList(Arrays.asList
					                                                              ("future",
					"explorer", "building", "resource", "changeset", "change", "move",
					"work", "discover", "submap")));

	/**
	 * @param <T>     A supertype of the object the XML represents
	 * @param file    the name of the file being read from
	 * @param istream a reader from which to read the XML
	 * @param type    The type of the object the XML represents
	 * @param warner  a Warning instance to use for warnings
	 * @return the object contained in that stream
	 * @throws XMLStreamException if XML isn't well-formed.
	 * @throws SPFormatException  if the data is invalid.
	 */
	<T> T readXML(File file, Reader istream, Class<T> type,
	              Warning warner) throws XMLStreamException, SPFormatException;
}
