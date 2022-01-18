package utility.subset;

import java.nio.file.Path;
import drivers.common.IMultiMapModel;
import drivers.common.ReadOnlyDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import drivers.common.cli.ICLIHelper;

import common.map.IMapNG;
import java.util.function.Consumer;
import java.util.Optional;

/**
 * A driver to check whether player maps are subsets of the main map.
 */
public class SubsetCLI implements ReadOnlyDriver {
	public SubsetCLI(ICLIHelper cli, IMultiMapModel model) {
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

	private Consumer<String> report(String filename) {
		return string -> cli.println(String.format("In %s: %s", filename, string));
	}

	@Override
	public void startDriver() {
		for (IMapNG map : model.getSubordinateMaps()) {
			String filename = Optional.ofNullable(map.getFilename()).map(Path::toString)
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