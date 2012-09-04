package controller.map;

import controller.map.cxml.CompactXMLReader;
import controller.map.readerng.MapReaderNG;

/**
 * A factory to produce instances of the current and old map readers, to test
 * against. (So we don't have to ignore *all* deprecation warnings in the test
 * class.)
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
