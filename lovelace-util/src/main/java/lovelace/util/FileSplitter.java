package lovelace.util;

import java.io.IOException;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.javatuples.Pair;

/**
 * A helper to load tab-separated data from file.
 *
 * TODO: Support 3-column files as well
 */
public final class FileSplitter {
	private FileSplitter() {
		// do not instantiate
	}

	/**
	 * Split a line on its first tab.
	 */
	private static String[] splitOnFirstTab(final String line) {
		return line.split("\t", 2);
	}

	/**
	 * Convert the results of splitting a line into an Entry, with the
	 * first field as the key and passing the second (presumed only) field
	 * through the provided method to get the item.
	 */
	private static <Type> Pair<String, Type> lineToEntry(final String[] line, final Function<String, Type> factory) {
		if (line.length == 0) {
			throw new IllegalArgumentException("Empty line");
		}
		final String first = line[0];
		final String second;
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
	public static <Type> Map<String, Type> getFileContents(final Path path, final Function<String, Type> factory)
			throws IOException {
		final Iterable<String> textContent = FileContentsReader.readFileContents(FileSplitter.class, path);
		return StreamSupport.stream(textContent.spliterator(), false).map(FileSplitter::splitOnFirstTab)
				.map(str -> lineToEntry(str, factory)).collect(Collectors.toMap(Pair::getValue0, Pair::getValue1));
	}
}
