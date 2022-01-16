package utility;

import lovelace.util.MalformedXMLException;
import drivers.common.DriverFailedException;
import common.xmlio.SPFormatException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;

import java.io.StringReader;

import common.map.IMapNG;

import common.xmlio.Warning;

import impl.xmlio.IMapReader;
import impl.xmlio.TestReaderFactory;
import impl.xmlio.SPWriter;

import drivers.common.UtilityDriver;
import drivers.common.SPOptions;
import drivers.common.EmptyOptions;

import drivers.common.cli.ICLIHelper;
import java.util.stream.Collectors;

/**
 * A driver for comparing map readers.
 */
public class ReaderComparator implements UtilityDriver {
	private static String readAll(Path path) throws IOException {
		return Files.readAllLines(path, StandardCharsets.UTF_8).stream()
			.collect(Collectors.joining("\n"));
	}

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	private final ICLIHelper cli;
	public ReaderComparator(ICLIHelper cli) {
		this.cli = cli;
	}

	/**
	 * Compare the two readers' performance on the given files.
	 */
	@Override
	public void startDriver(String... args) throws DriverFailedException {
		IMapReader readerOne = TestReaderFactory.getOldMapReader();
		IMapReader readerTwo = TestReaderFactory.getNewMapReader();
		SPWriter writerOne = TestReaderFactory.getOldWriter();
		SPWriter writerTwo = TestReaderFactory.getNewWriter();
		Warning warner = Warning.IGNORE;
		for (String arg : args) {
			cli.println(arg + ":");
			Path path = Paths.get(arg);
			String contents;
			try {
				contents = readAll(path);
			} catch (IOException except) {
				throw new DriverFailedException(except, "I/O error reading file");
			}
			long readStartOne = System.nanoTime();
			IMapNG mapOne;
			try {
				mapOne = readerOne.readMapFromStream(path,
					new StringReader(contents), warner);
			} catch (SPFormatException except) {
				throw new DriverFailedException(except,
					"Fatal SP format error reported by first reader");
			} catch (MalformedXMLException except) {
				throw new DriverFailedException(except,
					"Malformed XML reported by first reader");
			} catch (IOException except) {
				throw new DriverFailedException(except,
					"I/O error thrown by first reader");
			}
			long readEndOne = System.nanoTime();
			cli.println("Old reader took " + (readEndOne - readStartOne));
			long readStartTwo = System.nanoTime();
			IMapNG mapTwo;
			try {
				mapTwo = readerTwo.readMapFromStream(path,
					new StringReader(contents), warner);
			} catch (SPFormatException except) {
				throw new DriverFailedException(except,
					"Fatal SP format error reported by second reader");
			} catch (MalformedXMLException except) {
				throw new DriverFailedException(except,
					"Malformed XML reported by second reader");
			} catch (IOException except) {
				throw new DriverFailedException(except,
					"I/O error thrown by second reader");
			}
			long readEndTwo = System.nanoTime();
			cli.println("New reader took " + (readEndTwo - readStartTwo));
			if (mapOne.equals(mapTwo)) {
				cli.println("Readers produce identical results");
			} else {
				cli.println("Readers differ on " + arg);
			}
			StringBuilder outOne = new StringBuilder();
			long writeStartOne = System.nanoTime();
			try {
				writerOne.write(outOne::append, mapOne);
			} catch (MalformedXMLException except) {
				throw new DriverFailedException(except,
					"First writer produced malformed XML");
			} catch (IOException except) {
				throw new DriverFailedException(except,
					"I/O error reported by first writer");
			}
			long writeEndOne = System.nanoTime();
			cli.println("Old writer took " + (writeEndOne - writeStartOne));
			StringBuilder outTwo = new StringBuilder();
			long writeStartTwo = System.nanoTime();
			try {
				writerTwo.write(outTwo::append, mapTwo);
			} catch (MalformedXMLException except) {
				throw new DriverFailedException(except,
					"Second writer produced malformed XML");
			} catch (IOException except) {
				throw new DriverFailedException(except,
					"I/O error reported by second writer");
			}
			long writeEndTwo = System.nanoTime();
			cli.println("New writer took " + (writeEndTwo - writeStartTwo));
			if (outOne.toString().equals(outTwo.toString())) {
				cli.println("Writers produce identical results");
			} else if (outOne.toString().trim().equals(outTwo.toString().trim())) {
				// TODO: try with a global replacement of all whitespace with single spaces
				cli.println("Writers produce identical results except for whitespace");
			} else {
				cli.println("Writers differ on " + arg);
			}
		}
	}
}
