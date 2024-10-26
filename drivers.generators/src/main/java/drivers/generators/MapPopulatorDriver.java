package drivers.generators;

import legacy.idreg.IDFactoryFiller;
import legacy.idreg.IDRegistrar;

import drivers.common.CLIDriver;
import drivers.common.SPOptions;

import drivers.common.cli.ICLIHelper;

import lovelace.util.SingletonRandom;

import java.util.List;

import legacy.map.Point;

import java.util.stream.Collectors;
import java.util.Collections;

/**
 * A driver to add some kind of fixture to suitable tiles throughout the map.
 * Customize the {@link #populator} field before each use.
 */
// TODO: Write GUI equivalent of Map Populator Driver
public class MapPopulatorDriver implements CLIDriver {
	public MapPopulatorDriver(final ICLIHelper cli, final SPOptions options, final IPopulatorDriverModel model) {
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
	private void populate() {
		final IDRegistrar idf = IDFactoryFiller.createIDFactory(model.getMap());
		final List<Point> locations = model.getMap().streamLocations().collect(Collectors.toList());
		Collections.shuffle(locations);
		for (final Point location : locations) {
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
		populate();
		cli.printf("%d/%d suitable locations were changed%n", changedCount, suitableCount);
		if (changedCount > 0) {
			model.setMapModified(true);
		}
	}
}
