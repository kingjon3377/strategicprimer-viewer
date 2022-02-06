package drivers.resourceadding;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import drivers.common.cli.ICLIHelper;

import common.idreg.IDFactoryFiller;

import common.map.IMapNG;
import common.map.Player;

import drivers.common.SPOptions;
import drivers.common.CLIDriver;

import java.util.ArrayList;
import java.util.List;

import common.map.fixtures.IResourcePile;
import common.map.fixtures.Implement;

/**
 * A driver to let the user enter a player's resources and equipment.
 */
/* package */ class ResourceAddingCLI implements CLIDriver {
	public ResourceAddingCLI(final ICLIHelper cli, final SPOptions options, final ResourceManagementDriverModel model) {
		this.cli = cli;
		this.options = options;
		this.model = model;
		helper = new ResourceAddingCLIHelper(cli, new IDFactoryFiller()
			.createIDFactory(StreamSupport.stream(model.getAllMaps().spliterator(),
				false).toArray(IMapNG[]::new)));
	}

	private final ICLIHelper cli;
	private final SPOptions options;
	private final ResourceManagementDriverModel model;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	@Override
	public ResourceManagementDriverModel getModel() {
		return model;
	}

	private final ResourceAddingCLIHelper helper;

	/**
	 * TODO: Add a loopOnPlayers() helper method to CLIDriver interface,
	 * since there are several disparate CLI drivers that do that. (?)
	 */
	@Override
	public void startDriver() {
		List<Player> players = StreamSupport.stream(model.getPlayers().spliterator(),
				false).collect(Collectors.toList());
		while (!players.isEmpty()) {
			Player chosen = cli.chooseFromList(players, "Players in the maps:",
				"No players found.", "Player to add resources for: ", false).getValue1();
			if (chosen == null) {
				break;
			}
			players.remove(chosen);
			while (true) {
				Boolean resp = cli.inputBoolean("Keep going? ");
				if (resp == null) {
					return;
				} else if (!resp) {
					break;
				}
				Boolean res = cli.inputBooleanInSeries("Enter a (quantified) resource? ");
				if (res == null) {
					return;
				} else if (res) {
					IResourcePile resource = helper.enterResource();
					if (resource == null) {
						return;
					}
					model.addResource(resource, chosen);
					continue;
				}
				Boolean eq = cli.inputBooleanInSeries("Enter equipment etc.? ");
				if (eq == null) {
					return;
				} else if (eq) {
					Implement implement = helper.enterImplement();
					if (implement == null) {
						return;
					}
					model.addResource(implement, chosen);
				}
			}
			Boolean continuation = cli.inputBoolean("Choose another player?");
			if (continuation == null) {
				return;
			} else if (!continuation) {
				break;
			}
		}
	}
}
