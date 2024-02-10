package lovelace.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

public final class FileContentsReader {
	private FileContentsReader() {
	}

	private static InputStream getResourceAsStream(final Class<?> cls, final String filename)
			throws NoSuchFileException {
		return Optional.ofNullable(Optional.ofNullable(cls.getResourceAsStream(filename))
				.orElseGet(() -> cls.getClassLoader().getResourceAsStream(filename)))
			.orElseThrow(() -> new NoSuchFileException(filename));
	}

	public static Iterable<String> readFileContents(final Class<?> cls, final Path path) throws IOException {
		if (Files.isReadable(path)) {
			return Files.readAllLines(path, StandardCharsets.UTF_8);
		} else {
			try (final BufferedReader reader = new BufferedReader(new InputStreamReader(getResourceAsStream(cls,
				path.toString()), StandardCharsets.UTF_8))) {
				return reader.lines().collect(Collectors.toList());
			}
		}
	}
}
