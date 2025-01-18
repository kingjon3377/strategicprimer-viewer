package lovelace.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class FileContentsReader {
	private FileContentsReader() {
	}

	private static InputStream getResourceAsStream(final Class<?> cls, final String filename)
			throws NoSuchFileException {
		return Optional.ofNullable(Optional.ofNullable(cls.getResourceAsStream(filename))
						.orElseGet(() -> cls.getClassLoader().getResourceAsStream(filename)))
				.orElseThrow(() -> new NoSuchFileException(filename));
	}

	public static Stream<String> streamFileContents(final Class<?> cls, final Path path) throws IOException {
		// Going to List and then to Stream is less efficient, but returning the Stream from
		// Files or BufferedReader::lines caused test failures because the underlying reader
		// was closed before calling code had finished traversing the Stream.
		return readFileContents(cls, path).stream();
	}

	@SuppressWarnings("WeakerAccess") // We want to support callers who don't need to do Stream ops
	public static List<String> readFileContents(final Class<?> cls, final Path path) throws IOException {
		if (Files.isReadable(path)) {
			return Files.readAllLines(path, StandardCharsets.UTF_8);
		} else {
			try (final BufferedReader reader = new BufferedReader(new InputStreamReader(getResourceAsStream(cls,
					path.toString()), StandardCharsets.UTF_8))) {
				return reader.lines().toList();
			}
		}
	}
}
