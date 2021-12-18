package mining;

import java.io.IOException;
import java.io.BufferedWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import drivers.common.DriverFailedException;
import drivers.common.UtilityDriver;
import drivers.common.SPOptions;
import drivers.common.IncorrectUsageException;

import drivers.common.cli.ICLIHelper;

import java.nio.charset.Charset;

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

	public MiningCLI(ICLIHelper cli, SPOptions options) {
		this.cli = cli;
		this.options = options;
	}

	@Override
	public SPOptions getOptions() {
		return options;
	}

	@Override
	public void startDriver(String... args) throws DriverFailedException {
		if (args.length != 2) {
			throw new IncorrectUsageException(MiningCLIFactory.USAGE);
		}
		String filename = args[0];
		String second = args[1];
		long seed; // TODO: long instead?
		if (options.hasOption("--seed")) {
			try {
				seed = Long.parseLong(options.getArgument("--seed"));
			} catch (NumberFormatException except) {
				throw new DriverFailedException(except, "Seed must be numeric");
			}
		} else {
			seed = System.currentTimeMillis();
		}

		LodeStatus initial = LodeStatus.parse(second);
		if (initial == null) {
			try {
				int index = Integer.parseInt(second);
				initial = LodeStatus.values()[index];
			} catch (NumberFormatException|ArrayIndexOutOfBoundsException except) {
				throw new DriverFailedException(except,
					"Status must be a valid status or the index of a valid status");
			}
		}

		MineKind mineKind;
		// TODO: Support distance-from-center deposits
		if (options.hasOption("--banded")) {
			mineKind = MineKind.Banded;
		} else {
			mineKind = MineKind.Normal;
		}

		MiningModel model = new MiningModel(initial, seed, mineKind);
		int lowerRightRow = model.getMaximumRow();
		int lowerRightColumn = model.getMaximumColumn();

		Path path = Paths.get(filename);
		if (Files.exists(path)) {
			throw new DriverFailedException(new Exception(
				String.format("Output file %s already exists", filename)));
		}
		try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"))) {
			for (int row = 0; row <= lowerRightRow; row++) {
				for (int col = 0; col <= lowerRightColumn; col++) {
					LodeStatus status = model.statusAt(row, col);
					if (status == null) {
						writer.write("-1,");
					} else {
						writer.write(Integer.toString(status.getRatio()));
						writer.write(",");
					}
				}
				writer.write(System.lineSeparator());
			}
		} catch (IOException except) {
			throw new DriverFailedException(except);
		}
	}
}
