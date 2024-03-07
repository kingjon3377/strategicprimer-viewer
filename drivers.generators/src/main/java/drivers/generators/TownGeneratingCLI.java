package drivers.generators;

import java.io.IOException;
import java.util.Objects;

import drivers.common.DriverFailedException;
import drivers.exploration.old.MissingTableException;
import legacy.map.ILegacyMap;
import drivers.common.CLIDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import drivers.common.cli.ICLIHelper;

import legacy.idreg.IDRegistrar;
import legacy.idreg.IDFactoryFiller;

/**
 * A driver to let the user enter or generate 'stats' for towns.
 *
 * TODO: Write GUI to allow user to generate or enter town contents
 */
public class TownGeneratingCLI implements CLIDriver {
	public TownGeneratingCLI(final ICLIHelper cli, final PopulationGeneratingModel model) {
		this.cli = cli;
		this.model = model;
	}

	private final ICLIHelper cli;
	private final PopulationGeneratingModel model;

	@Override
	public PopulationGeneratingModel getModel() {
		return model;
	}

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	@Override
	public void startDriver() throws DriverFailedException {
		final TownGenerator generator;
		try {
			generator = new TownGenerator(cli); // TODO: Consider combining that with this class again.
		} catch (final MissingTableException except) {
			throw new DriverFailedException(except, "Missing table file(s)");
		} catch (final IOException except) {
			throw new DriverFailedException(except, "I/O error initializing generator");
		}
		final IDRegistrar idf = IDFactoryFiller.createIDFactory(
				model.streamAllMaps().toArray(ILegacyMap[]::new));
		final Boolean specific = cli.inputBoolean("Enter or generate stats for just specific towns? ");
		if (Objects.isNull(specific)) {
			return;
		} else if (specific) {
			generator.generateSpecificTowns(idf, model);
		} else {
			generator.generateAllTowns(idf, model);
		}
	}
}
