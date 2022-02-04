package common.map.fixtures.mobile;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lovelace.util.FileContentsReader;

import org.javatuples.Pair;

/**
 * A helper to load tab-separated data from file.
 *
 * TODO: move to lovelace-util? or drivers.common?
 *
 * TODO: Instead, combine with all the classes that use this into just one class?
 */
public final class FileSplitter {
	private FileSplitter() {
		// do not instantiate
	}
	/**
	 * Split a line on its first tab.
	 */
	static String[] splitOnFirstTab(String line) {
		return line.split("\t", 2);
	}
	/**
	 * Convert the results of splitting a line into an Entry, with the
	 * first field as the key and passing the second (presumed only) field
	 * through the provided method to get the item.
	 */
	static <Type> Pair<String, Type> lineToEntry(String[] line, Function<String, Type> factory) {
		if (line.length == 0) {
			throw new IllegalArgumentException("Empty line");
		}
		String first = line[0];
		String second;
		if (line.length > 1) {
			second = line[1];
		} else {
			second = "";
		}
		return Pair.with(first, factory.apply(second));
	}

	/**
	 * Read a tab-separated file from either the filesystem or this
	 * module's classpath, and return its contents as a Map from keys (the
	 * first field) to values (the remainder passed through the provided
	 * factory).
	 */
	public static <Type> Map<String, Type> getFileContents(String filename,
			Function<String, Type> factory) throws IOException {
		Iterable<String> textContent =
			FileContentsReader.readFileContents(FileSplitter.class, filename);
		return StreamSupport.stream(textContent.spliterator(), false)
			.map(FileSplitter::splitOnFirstTab)
			.map(str -> lineToEntry(str, factory))
			.collect(Collectors.toMap(Pair::getValue0, Pair::getValue1));
	}
}
