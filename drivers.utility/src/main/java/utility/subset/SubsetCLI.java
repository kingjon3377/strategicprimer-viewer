package utility.subset;

import java.nio.file.Path;

import drivers.common.IMultiMapModel;
import drivers.common.ReadOnlyDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import drivers.common.cli.ICLIHelper;

import legacy.map.ILegacyMap;

import java.util.function.Consumer;
import java.util.Optional;

/**
 * A driver to check whether player maps are subsets of the main map.
 */
public final class SubsetCLI implements ReadOnlyDriver {
	public SubsetCLI(final ICLIHelper cli, final IMultiMapModel model) {
		this.model = model;
		this.cli = cli;
	}

	private final ICLIHelper cli;

	private final IMultiMapModel model;

	@Override
	public IMultiMapModel getModel() {
		return model;
	}

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	private Consumer<String> report(final String filename) {
		return string -> cli.printf("In %s: %s%n", filename, string);
	}

	@Override
	public void startDriver() {
		for (final ILegacyMap map : model.getSubordinateMaps()) {
			final String filename = Optional.ofNullable(map.getFilename()).map(Path::toString)
					.orElse("map without a filename");
			cli.print(filename, "\t...\t\t");
			if (model.getMap().isSubset(map, report(filename))) {
				cli.println("OK");
			} else {
				cli.println("WARN");
			}
		}
	}
}
