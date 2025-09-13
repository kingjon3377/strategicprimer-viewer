package utility;

import drivers.common.DriverFailedException;
import common.xmlio.SPFormatException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;

import java.io.StringReader;

import legacy.map.ILegacyMap;

import common.xmlio.Warning;

import legacy.xmlio.IMapReader;
import legacy.xmlio.TestReaderFactory;
import impl.xmlio.SPWriter;

import drivers.common.UtilityDriver;
import drivers.common.SPOptions;
import drivers.common.EmptyOptions;

import drivers.common.cli.ICLIHelper;

import javax.xml.stream.XMLStreamException;

/**
 * A driver for comparing map readers.
 */
public final class ReaderComparator implements UtilityDriver {
	private static String readAll(final Path path) throws IOException {
		return String.join(System.lineSeparator(), Files.readAllLines(path, StandardCharsets.UTF_8));
	}

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	private final ICLIHelper cli;

	public ReaderComparator(final ICLIHelper cli) {
		this.cli = cli;
	}

	private void logElapsedTime(final String label, final long start, final long end) {
		cli.printf("%s took %ld", label, (end - start));
	}

	/**
	 * Compare the two readers' performance on the given files.
	 */
	@SuppressWarnings("HardcodedFileSeparator")
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		final IMapReader readerOne = TestReaderFactory.getOldMapReader();
		final IMapReader readerTwo = TestReaderFactory.getNewMapReader();
		final SPWriter writerOne = TestReaderFactory.getOldWriter();
		final SPWriter writerTwo = TestReaderFactory.getNewWriter();
		final Warning warner = Warning.IGNORE;
		for (final String arg : args) {
			cli.print(arg);
			cli.println(":");
			final Path path = Paths.get(arg);
			final String contents;
			try {
				contents = readAll(path);
			} catch (final IOException except) {
				throw new DriverFailedException(except, "I/O error reading file");
			}
			final long readStartOne = System.nanoTime();
			final ILegacyMap mapOne;
			try {
				mapOne = readerOne.readMapFromStream(path,
						new StringReader(contents), warner);
			} catch (final SPFormatException except) {
				throw new DriverFailedException(except,
						"Fatal SP format error reported by first reader");
			} catch (final XMLStreamException except) {
				throw new DriverFailedException(except,
						"Malformed XML reported by first reader");
			} catch (final IOException except) {
				throw new DriverFailedException(except,
						"I/O error thrown by first reader");
			}
			final long readEndOne = System.nanoTime();
			logElapsedTime("Old reader", readEndOne, readStartOne);
			final long readStartTwo = System.nanoTime();
			final ILegacyMap mapTwo;
			try {
				mapTwo = readerTwo.readMapFromStream(path,
						new StringReader(contents), warner);
			} catch (final SPFormatException except) {
				throw new DriverFailedException(except,
						"Fatal SP format error reported by second reader");
			} catch (final XMLStreamException except) {
				throw new DriverFailedException(except,
						"Malformed XML reported by second reader");
			} catch (final IOException except) {
				throw new DriverFailedException(except,
						"I/O error thrown by second reader");
			}
			final long readEndTwo = System.nanoTime();
			logElapsedTime("New reader", readEndTwo, readStartTwo);
			if (mapOne.equals(mapTwo)) {
				cli.println("Readers produce identical results");
			} else {
				cli.print("Readers differ on ");
				cli.println(arg);
			}
			final StringBuilder outOne = new StringBuilder();
			final long writeStartOne = System.nanoTime();
			try {
				writerOne.write(outOne::append, mapOne);
			} catch (final XMLStreamException except) {
				throw new DriverFailedException(except,
						"First writer produced malformed XML");
			} catch (final IOException except) {
				throw new DriverFailedException(except,
						"I/O error reported by first writer");
			}
			final long writeEndOne = System.nanoTime();
			logElapsedTime("Old writer", writeEndOne, writeStartOne);
			final StringBuilder outTwo = new StringBuilder();
			final long writeStartTwo = System.nanoTime();
			try {
				writerTwo.write(outTwo::append, mapTwo);
			} catch (final XMLStreamException except) {
				throw new DriverFailedException(except,
						"Second writer produced malformed XML");
			} catch (final IOException except) {
				throw new DriverFailedException(except,
						"I/O error reported by second writer");
			}
			final long writeEndTwo = System.nanoTime();
			logElapsedTime("New writer", writeEndTwo, writeStartTwo);
			if (outOne.toString().contentEquals(outTwo)) {
				cli.println("Writers produce identical results");
			} else if (outOne.toString().strip().equals(outTwo.toString().strip())) {
				// TODO: try with a global replacement of all whitespace with single spaces
				cli.println("Writers produce identical results except for whitespace");
			} else {
				cli.print("Writers differ on ");
				cli.println(arg);
			}
		}
	}
}
