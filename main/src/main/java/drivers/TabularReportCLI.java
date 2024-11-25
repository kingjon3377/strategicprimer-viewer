package drivers;

import java.nio.file.Paths;

import lovelace.util.LovelaceLogger;
import lovelace.util.ThrowingConsumer;
import lovelace.util.ThrowingFunction;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.BufferedWriter;

import java.io.IOException;
import java.io.IOError;

import drivers.common.IMultiMapModel;
import drivers.common.IDriverModel;
import drivers.common.SPOptions;
import drivers.common.DriverFailedException;
import drivers.common.ReadOnlyDriver;

import drivers.common.cli.ICLIHelper;

import legacy.map.ILegacyMap;

import report.TabularReportGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A driver to produce tabular (CSV) reports of the contents of a player's map.
 */
public final class TabularReportCLI implements ReadOnlyDriver {
	public TabularReportCLI(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		this.cli = cli;
		this.options = options;
		this.model = model;
	}

	private final ICLIHelper cli;
	private final SPOptions options;
	private final IDriverModel model;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	@Override
	public IDriverModel getModel() {
		return model;
	}

	private final Map<String, BufferedWriter> writers = new HashMap<>();

	private ThrowingFunction<String, ThrowingConsumer<String, IOException>, IOException> filenameFunction(
			final Path base) {
		final String baseName = base.getFileName().toString();
		return tableName -> {
			final String key = "%s.%s.csv".formatted(baseName, tableName);
			// n.b. can't use computeIfAbsent because the function we would pass throws IOException
			if (writers.containsKey(key)) {
				return writers.get(key)::write;
			} else {
				// FIXME: Looks like this leaks the writer
				final BufferedWriter writer = Files.newBufferedWriter(base.resolveSibling(key));
				writers.put(key, writer);
				return writer::write;
			}
		};
	}

	@SuppressWarnings("ErrorNotRethrown") // It's IOError, and is rethrown wrapped
	private void createReports(final ILegacyMap map, final @Nullable Path mapFile) throws DriverFailedException {
		try {
			if (Objects.isNull(mapFile)) {
				LovelaceLogger.error("Asked to create reports from map with no filename");
				TabularReportGenerator.createTabularReports(map,
						filenameFunction(Paths.get("unknown.xml")), cli);
			} else {
				TabularReportGenerator.createTabularReports(map,
						filenameFunction(mapFile), cli);
			}
		} catch (final IOException | IOError except) {
			throw new DriverFailedException(except);
		}
	}

	@Override
	public void startDriver() throws DriverFailedException {
		try {
			if (model instanceof final IMultiMapModel mmm) {
				for (final ILegacyMap map : mmm.getAllMaps()) {
					createReports(map, map.getFilename());
				}
			} else {
				createReports(model.getMap(), model.getMap().getFilename());
			}
		} finally {
			try {
				for (final BufferedWriter writer : writers.values()) {
					writer.close();
				}
			} catch (final IOException except) {
				LovelaceLogger.error(except, "I/O error closing writer(s)");
			}
		}
	}
}
