package controller.map.iointerfaces;

import controller.map.fluidxml.SPFluidReader;
import controller.map.fluidxml.SPFluidWriter;
import controller.map.yaxml.YAXMLReader;
import controller.map.yaxml.YAXMLWriter;

/**
 * A factory to produce instances of the current and old map readers, to test against. (So
 * we don't have to ignore *all* deprecation warnings in the test class.)
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
@SuppressWarnings({"deprecation", "UtilityClassCanBeEnum"})
public final class TestReaderFactory {
	/**
	 * Do not instantiate.
	 */
	private TestReaderFactory() {
		// Do nothing.
	}

	/**
	 * Get an instance of the old reader.
	 * @return an instance of the old reader
	 */
	public static ISPReader createOldReader() {
		return new SPFluidReader();
	}

	/**
	 * Get an instance of the new reader.
	 * @return an instance of the new reader
	 */
	public static ISPReader createNewReader() {
		return new YAXMLReader();
	}

	/**
	 * Get an instance of the old writer.
	 * @return an instance of the old writer
	 */
	public static SPWriter createOldWriter() {
		return new SPFluidWriter();
	}

	/**
	 * Get an instance of the new writer.
	 * @return an instance of the new writer
	 */
	public static SPWriter createNewWriter() {
		return new YAXMLWriter();
	}
}
