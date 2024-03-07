package drivers.resourceadding;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import drivers.common.cli.ICLIHelper;

import legacy.idreg.IDFactoryFiller;

import legacy.map.ILegacyMap;
import legacy.map.Player;

import drivers.common.SPOptions;
import drivers.common.CLIDriver;

import java.util.List;

import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.Implement;

/**
 * A driver to let the user enter a player's resources and equipment.
 */
/* package */ class ResourceAddingCLI implements CLIDriver {
	public ResourceAddingCLI(final ICLIHelper cli, final SPOptions options, final ResourceManagementDriverModel model) {
		this.cli = cli;
		this.options = options;
		this.model = model;
		helper = new ResourceAddingCLIHelper(cli, IDFactoryFiller
				.createIDFactory(model.streamAllMaps().toArray(ILegacyMap[]::new)));
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
		final List<Player> players = StreamSupport.stream(model.getPlayers().spliterator(),
				false).collect(Collectors.toList());
		while (!players.isEmpty()) {
			final Player chosen = cli.chooseFromList((List<? extends Player>) players, "Players in the maps:",
					"No players found.", "Player to add resources for: ",
					ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
			if (Objects.isNull(chosen)) {
				break;
			}
			players.remove(chosen);
			while (true) {
				final Boolean resp = cli.inputBoolean("Keep going? ");
				if (Objects.isNull(resp)) {
					return;
				} else if (!resp) {
					break;
				}
				final Boolean res = cli.inputBooleanInSeries("Enter a (quantified) resource? ");
				if (Objects.isNull(res)) {
					return;
				} else if (res) {
					final IResourcePile resource = helper.enterResource();
					if (Objects.isNull(resource)) {
						return;
					}
					model.addResource(resource, chosen);
					continue;
				}
				final Boolean eq = cli.inputBooleanInSeries("Enter equipment etc.? ");
				if (Objects.isNull(eq)) {
					return;
				} else if (eq) {
					final Implement implement = helper.enterImplement();
					if (Objects.isNull(implement)) {
						return;
					}
					model.addResource(implement, chosen);
				}
			}
			final Boolean continuation = cli.inputBoolean("Choose another player?");
			if (Objects.isNull(continuation)) {
				return;
			} else if (!continuation) {
				break;
			}
		}
	}
}
