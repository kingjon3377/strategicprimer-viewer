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
/* package */ final class ResourceAddingCLI implements CLIDriver {
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
		boolean continuation = true;
		while (continuation && !players.isEmpty()) {
			final Player chosen = cli.chooseFromList((List<? extends Player>) players, "Players in the maps:",
					"No players found.", "Player to add resources for: ",
					ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
			if (Objects.isNull(chosen)) {
				break;
			}
			players.remove(chosen);
			boolean moreResources = true;
			while (moreResources) {
				switch (cli.inputBooleanInSeries("Enter a (quantified) resource? ")) {
					case YES -> {
						final IResourcePile resource = helper.enterResource();
						if (Objects.isNull(resource)) {
							return;
						}
						model.addResource(resource, chosen);
						continue;
					}
					case NO -> { // Do nothing
					}
					case QUIT -> {
						return;
					}
					case EOF -> {
						return; // TODO: Somehow signal EOF to callers
					}
				}
				switch (cli.inputBooleanInSeries("Enter equipment etc.? ")) {
					case YES -> {
						final Implement implement = helper.enterImplement();
						if (Objects.isNull(implement)) {
							return;
						}
						model.addResource(implement, chosen);
					}
					case NO -> { // Do nothing
					}
					case QUIT -> {
						return;
					}
					case EOF -> {
						return; // TODO: Somehow signal EOF to callers
					}
				}
				switch(cli.inputBoolean("Keep going? ")) {
					case YES -> { // Do nothing
					}
					case NO -> moreResources = false;
					case QUIT -> {
						return;
					}
					case EOF -> {
						return; // TODO: Somehow signal EOF to callers
					}
				}
			}
			switch (cli.inputBoolean("Choose another player?")) {
				case YES -> { // Do nothing
				}
				case NO -> continuation = false;
				case QUIT -> {
					return;
				}
				case EOF -> {
					return; // TODO: Somehow signal EOF to callers
				}
			}
		}
	}
}
