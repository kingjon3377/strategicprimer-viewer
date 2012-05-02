package controller.map.drivers;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import model.map.SPMap;
import util.Pair;
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
		final Pair<SPMap, Boolean> mainPair = safeLoadMap(reader, args[0]);
		if (Boolean.TRUE.equals(mainPair.second())) {
			System.err.println("Error loading main map");
			System.exit(1);
			return;
		}
		final SPMap mainMap = mainPair.first();
		SystemOut.SYS_OUT.println("OK if strict subset, WARN if needs manual checking, FAIL if error in reading");
		for (String arg : args) {
			if (arg.equals(args[0])) {
				continue;
			}
			SystemOut.SYS_OUT.print(arg);
			SystemOut.SYS_OUT.print("\t...\t\t");
			final Pair<SPMap, Boolean> pair = safeLoadMap(reader, arg);
			if (Boolean.TRUE.equals(pair.second())) {
				SystemOut.SYS_OUT.println("FAIL");
				continue;
			}
			final SPMap map = pair.first(); // NOPMD
			if (mainMap.isSubset(map)) {
				SystemOut.SYS_OUT.println("OK");
			} else {
				SystemOut.SYS_OUT.println("WARN");
			}
		}
	}
	
	/**
	 * Return the specified map (or null) *and* whether an exception was thrown.
	 * Any thrown exceptions will additionally be warned about.
	 * 
	 * @param reader
	 *            the map reader to use
	 * @param filename
	 *            the name of a map
	 * @return a Pair of the map (or null) and whether an exception was thrown.
	 */
	private static Pair<SPMap, Boolean> safeLoadMap(final MapReaderAdapter reader, final String filename) {
		try {
			return Pair.of(reader.readMap(filename, new Warning(// NOPMD
					Warning.Action.Ignore)), Boolean.FALSE);
		} catch (MapVersionException e) {
			Warning.INSTANCE.warn(e);
			return Pair.of((SPMap) null, Boolean.TRUE); // NOPMD
		} catch (IOException e) {
			Warning.INSTANCE.warn(e);
			return Pair.of((SPMap) null, Boolean.TRUE); // NOPMD
		} catch (XMLStreamException e) {
			Warning.INSTANCE.warn(e);
			return Pair.of((SPMap) null, Boolean.TRUE); // NOPMD
		} catch (SPFormatException e) {
			Warning.INSTANCE.warn(e);
			return Pair.of((SPMap) null, Boolean.TRUE); // NOPMD
		}
	}
	
}
