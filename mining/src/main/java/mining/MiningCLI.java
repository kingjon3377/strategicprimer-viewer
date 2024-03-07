package mining;

import java.io.IOException;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Objects;

import drivers.common.DriverFailedException;
import drivers.common.UtilityDriver;
import drivers.common.SPOptions;
import drivers.common.IncorrectUsageException;

import drivers.common.cli.ICLIHelper;

/**
 * A driver to create a spreadsheet model of a mine. Its parameters are the
 * name of the file to write the CSV to and the value at the top center (as an
 * index into the LodeStatus values array).
 *
 * TODO: Write GUI to allow user to visually explore a mine
 */
public final class MiningCLI implements UtilityDriver {
	private final SPOptions options;
	private final ICLIHelper cli;

	public MiningCLI(final ICLIHelper cli, final SPOptions options) {
		this.cli = cli;
		this.options = options;
	}

	@Override
	public SPOptions getOptions() {
		return options;
	}

	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length != 2) {
			throw new IncorrectUsageException(MiningCLIFactory.USAGE);
		}
		final String filename = args[0];
		final String second = args[1];
		final long seed;
		if (options.hasOption("--seed")) {
			try {
				seed = Long.parseLong(options.getArgument("--seed"));
			} catch (final NumberFormatException except) {
				throw new DriverFailedException(except, "Seed must be numeric");
			}
		} else {
			seed = System.currentTimeMillis();
		}

		LodeStatus initial = LodeStatus.parse(second);
		if (Objects.isNull(initial)) {
			try {
				final int index = Integer.parseInt(second);
				if (index >= 0 && index < LodeStatus.values().length) {
					initial = LodeStatus.values()[index];
				} else {
					throw new DriverFailedException(new ArrayIndexOutOfBoundsException(index),
							"Status must be valid status or the index of a valid status");
				}
			} catch (final NumberFormatException except) {
				throw new DriverFailedException(except,
						"Status must be a valid status or the index of a valid status");
			}
		}

		final MineKind mineKind;
		// TODO: Support distance-from-center deposits
		if (options.hasOption("--banded")) {
			mineKind = MineKind.Banded;
		} else {
			mineKind = MineKind.Normal;
		}

		final MiningModel model = new MiningModel(initial, seed, mineKind, cli);
		final int lowerRightRow = model.getMaximumRow();
		final int lowerRightColumn = model.getMaximumColumn();

		final Path path = Paths.get(filename);
		if (Files.exists(path)) {
			throw new DriverFailedException(new Exception(
					String.format("Output file %s already exists", filename)));
		}
		try (final BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			for (int row = 0; row <= lowerRightRow; row++) {
				for (int col = 0; col <= lowerRightColumn; col++) {
					final LodeStatus status = model.statusAt(row, col);
					if (Objects.isNull(status)) {
						writer.write("-1,");
					} else {
						writer.write(Integer.toString(status.getRatio()));
						writer.write(",");
					}
				}
				writer.write(System.lineSeparator());
			}
		} catch (final IOException except) {
			throw new DriverFailedException(except);
		}
	}
}
