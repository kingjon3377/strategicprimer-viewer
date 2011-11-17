package controller.map.drivers;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import model.map.SPMap;
import util.Warning;
import view.util.SystemOut;
import controller.map.MapVersionException;
import controller.map.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver to check whether player maps are subsets of the main map.
 * @author Jonathan Lovelace
 *
 */
public final class SubsetDriver {
	/**
	 * Do not instantiate.
	 */
	private SubsetDriver() {
		// Do nothing
	}
	/**
	 * @param args the files to check
	 */
	// ESCA-JAVA0177:
	public static void main(final String[] args) {
		if (args.length < 2) {
			SystemOut.SYS_OUT.println("Usage: SubsetDriver mainMap playerMap [playerMap ...]");
		}
		final MapReaderAdapter reader = new MapReaderAdapter();
		final SPMap mainMap; // NOPMD
		try {
			mainMap = reader.readMap(args[0]);
		} catch (MapVersionException e) {
			Warning.warn(e);
			return; // NOPMD
		} catch (IOException e) {
			Warning.warn(e);
			return; // NOPMD
		} catch (XMLStreamException e) {
			Warning.warn(e);
			return; // NOPMD
		} catch (SPFormatException e) {
			Warning.warn(e);
			return; // NOPMD
		}
		SystemOut.SYS_OUT.println("OK if strict subset, WARN if needs manual checking, FAIL if error in reading");
		for (String arg : args) {
			if (arg.equals(args[0])) {
				continue;
			}
			SystemOut.SYS_OUT.print(arg);
			SystemOut.SYS_OUT.print("\t...\t\t");
			final SPMap map; // NOPMD
			try {
				map = reader.readMap(arg);
			} catch (MapVersionException e) {
				Warning.warn(e);
				SystemOut.SYS_OUT.println("FAIL");
				continue;
			} catch (IOException e) {
				Warning.warn(e);
				SystemOut.SYS_OUT.println("FAIL");
				continue;
			} catch (XMLStreamException e) {
				Warning.warn(e);
				SystemOut.SYS_OUT.println("FAIL");
				continue;
			} catch (SPFormatException e) {
				Warning.warn(e);
				SystemOut.SYS_OUT.println("FAIL");
				continue;
			}
			if (mainMap.isSubset(map)) {
				SystemOut.SYS_OUT.println("OK");
			} else {
				SystemOut.SYS_OUT.println("WARN");
			}
		}
	}
}
