package drivers.generators;

import java.io.IOException;
import drivers.common.DriverFailedException;
import drivers.exploration.old.MissingTableException;
import common.map.IMapNG;
import drivers.common.CLIDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import drivers.common.cli.ICLIHelper;

import common.idreg.IDRegistrar;
import common.idreg.IDFactoryFiller;

import java.util.stream.StreamSupport;

/**
 * A driver to let the user enter or generate 'stats' for towns.
 *
 * TODO: Write GUI to allow user to generate or enter town contents
 */
public class TownGeneratingCLI implements CLIDriver {
	public TownGeneratingCLI(ICLIHelper cli, PopulationGeneratingModel model) {
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
		TownGenerator generator;
		try {
			generator = new TownGenerator(cli); // TODO: Consider combining that with this class again.
		} catch (MissingTableException except) {
			throw new DriverFailedException(except, "Missing table file(s)");
		} catch (IOException except) {
			throw new DriverFailedException(except, "I/O error initializing generator");
		}
		IDRegistrar idf = new IDFactoryFiller().createIDFactory(StreamSupport.stream(
			model.getAllMaps().spliterator(), false).toArray(IMapNG[]::new));
		Boolean specific = cli.inputBoolean("Enter or generate stats for just specific towns? ");
		if (specific == null) {
			return;
		} else if (specific) {
			generator.generateSpecificTowns(idf, model);
		} else {
			generator.generateAllTowns(idf, model);
		}
	}
}
