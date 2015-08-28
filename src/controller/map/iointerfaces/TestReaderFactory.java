package controller.map.iointerfaces;

import controller.map.cxml.CompactXMLReader;
import controller.map.readerng.MapReaderNG;

/**
 * A factory to produce instances of the current and old map readers, to test
 * against. (So we don't have to ignore *all* deprecation warnings in the test
 * class.)
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2013 Jonathan Lovelace
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
@SuppressWarnings("deprecation")
public final class TestReaderFactory {
	/**
	 * Do not instantiate.
	 */
	private TestReaderFactory() {
		// Do nothing.
	}

	/**
	 * @return an instance of the old reader
	 */
	public static ISPReader createOldReader() {
		return new MapReaderNG();
	}

	/**
	 * @return an instance of the new reader
	 */
	public static ISPReader createNewReader() {
		return new CompactXMLReader();
	}
}
