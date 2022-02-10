package drivers;

import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.logging.Level;

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

import common.map.IMapNG;

import report.TabularReportGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * A driver to produce tabular (CSV) reports of the contents of a player's map.
 */
public class TabularReportCLI implements ReadOnlyDriver {
	private static final Logger LOGGER = Logger.getLogger(TabularReportCLI.class.getName());
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

	private ThrowingFunction<String, ThrowingConsumer<String, IOException>, IOException> filenameFunction(final Path base) {
		final String baseName = base.getFileName().toString();
		return tableName -> {
			final String key = String.format("%s.%s.csv", baseName, tableName);
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

	private void createReports(final IMapNG map, @Nullable final Path mapFile) throws DriverFailedException {
		try {
			if (mapFile == null) {
				LOGGER.severe("Asked to create reports from map with no filename");
				TabularReportGenerator.createTabularReports(map,
						filenameFunction(Paths.get("unknown.xml")), cli);
			} else {
				TabularReportGenerator.createTabularReports(map,
					filenameFunction(mapFile), cli);
			}
		} catch (final IOException|IOError except) {
			throw new DriverFailedException(except);
		}
	}

	@Override
	public void startDriver() throws DriverFailedException {
		try {
			if (model instanceof IMultiMapModel) {
				for (final IMapNG map : ((IMultiMapModel) model).getAllMaps()) {
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
				LOGGER.log(Level.SEVERE, "I/O error closing writer(s)", except);
			}
		}
	}
}
