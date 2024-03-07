package drivers;

import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.BufferedWriter;

import java.io.IOException;
import java.util.Objects;

import drivers.common.IMultiMapModel;
import drivers.common.IDriverModel;
import drivers.common.SPOptions;
import drivers.common.DriverFailedException;
import drivers.common.ReadOnlyDriver;

import drivers.common.cli.ICLIHelper;

import legacy.map.Player;
import legacy.map.ILegacyMap;

import report.ReportGenerator;

/**
 * A driver to produce a report of the contents of a map.
 */
public class ReportCLI implements ReadOnlyDriver {
	public ReportCLI(final SPOptions options, final IDriverModel model, final ICLIHelper cli) {
		this.options = options;
		this.model = model;
		this.cli = cli;
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

	private void writeReport(final @Nullable Path filename, final ILegacyMap map) throws IOException {
		if (Objects.isNull(filename)) {
			LovelaceLogger.error("Asked to make report from map with no filename");
		} else {
			Player player;
			if (options.hasOption("--player")) {
				try {
					player = map.getPlayers().getPlayer(Integer.parseInt(
							options.getArgument("--player")));
				} catch (final NumberFormatException except) {
					LovelaceLogger.warning(except, "Non-numeric player");
					player = map.getCurrentPlayer();
				}
			} else {
				player = map.getCurrentPlayer();
			}
			final String outString;
			final Path outPath;
			if (options.hasOption("--out")) {
				outString = options.getArgument("--out");
				outPath = Paths.get(outString);
			} else {
				outString = filename.getFileName().toString() + ".report.html";
				outPath = filename.resolveSibling(outString);
			}
			try (final BufferedWriter writer = Files.newBufferedWriter(outPath,
					StandardCharsets.UTF_8, StandardOpenOption.WRITE,
					StandardOpenOption.CREATE_NEW)) {
				writer.write(ReportGenerator.createReport(map, cli, player));
			}
		}
	}

	@Override
	public void startDriver() throws DriverFailedException {
		try {
			if (model instanceof final IMultiMapModel mmm) {
				for (final ILegacyMap map : mmm.getAllMaps()) {
					writeReport(map.getFilename(), map);
				}
			} else {
				writeReport(model.getMap().getFilename(), model.getMap());
			}
		} catch (final IOException except) {
			throw new DriverFailedException(except, "I/O error while writing report(s)");
		}
	}
}

