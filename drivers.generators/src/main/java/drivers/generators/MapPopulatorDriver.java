package drivers.generators;

import common.idreg.IDFactoryFiller;
import common.idreg.IDRegistrar;

import drivers.common.CLIDriver;
import drivers.common.SPOptions;

import drivers.common.cli.ICLIHelper;

import lovelace.util.SingletonRandom;

import java.util.List;
import common.map.Point;
import java.util.stream.Collectors;
import java.util.Collections;

/**
 * A driver to add some kind of fixture to suitable tiles throughout the map.
 * Customize the {@link populator} field before each use.
 */
// TODO: Write GUI equivalent of Map Populator Driver
public class MapPopulatorDriver implements CLIDriver {
	public MapPopulatorDriver(ICLIHelper cli, SPOptions options, IPopulatorDriverModel model) {
		this.cli = cli;
		this.options = options;
		this.model = model;
	}

	private final ICLIHelper cli;
	private final SPOptions options;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	private final IPopulatorDriverModel model;
	@Override
	public IPopulatorDriverModel getModel() {
		return model;
	}

	/**
	 * The object that does the heavy lifting of populating the map. This
	 * is the one field that should be changed before each populating pass.
	 */
	private final MapPopulator populator = new SampleMapPopulator();

	private int suitableCount = 0;

	private int changedCount = 0;

	/**
	 * Populate the map. You shouldn't need to customize this.
	 */
	private void populate(IPopulatorDriverModel model) {
		IDRegistrar idf = new IDFactoryFiller().createIDFactory(model.getMap());
		List<Point> locations = model.getMap().streamLocations().collect(Collectors.toList());
		Collections.shuffle(locations);
		for (Point location : locations) {
			if (populator.isSuitable(model.getMap(), location)) {
				suitableCount++;
				if (SingletonRandom.SINGLETON_RANDOM.nextDouble() < populator.getChance()) {
					changedCount++;
					populator.create(location, model, idf);
				}
			}
		}
	}

	@Override
	public void startDriver() {
		populate(model);
		cli.println(String.format("%d/%d suitable locations were changed", // TODO: add printf() to ICLIHelper
			changedCount, suitableCount));
		if (changedCount > 0) {
			model.setMapModified(true);
		}
	}
}
