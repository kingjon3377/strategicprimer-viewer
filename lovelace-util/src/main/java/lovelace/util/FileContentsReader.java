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
import java.util.stream.Collectors;

public final class FileContentsReader {
	public static Iterable<String> readFileContents(Class<?> cls,
			String filename) throws IOException {
		final Path onDisk = Paths.get(filename);
		if (Files.isReadable(onDisk)) {
			return Files.readAllLines(onDisk,
				StandardCharsets.UTF_8);
		}
		final InputStream relative = cls.getResourceAsStream(filename);
		final InputStream fromClasspath;
		if (relative == null) {
			fromClasspath = cls.getClassLoader().getResourceAsStream(filename);
		} else {
			fromClasspath = relative;
		}
		if (fromClasspath == null) {
			throw new NoSuchFileException(filename);
		}
		return new BufferedReader(new InputStreamReader(fromClasspath,
			StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
	}
}
