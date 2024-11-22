package drivers.advancement;

import drivers.common.SPOptions;
import drivers.common.CLIDriver;
import drivers.common.IWorkerModel;

import drivers.common.cli.ICLIHelper;

import legacy.map.Player;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import legacy.map.fixtures.mobile.IWorker;
import legacy.map.fixtures.mobile.IUnit;

import java.util.List;

/**
 * The worker-advancement CLI driver.
 */
public final class AdvancementCLI implements CLIDriver {
	public AdvancementCLI(final ICLIHelper cli, final SPOptions options, final IWorkerModel model) {
		this.cli = cli;
		this.options = options;
		this.model = model;
		helper = new AdvancementCLIHelperImpl(model, cli);
	}

	private final ICLIHelper cli;

	private final SPOptions options;
	private final IWorkerModel model;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	@Override
	public IWorkerModel getModel() {
		return model;
	}

	private final AdvancementCLIHelper helper;

	/**
	 * Let the user add experience to a player's workers.
	 */
	private void advanceWorkers(final Player player, final AdvancementCLIHelper.ExperienceConfig experienceConfig) {
		final List<IUnit> units = model.getUnits(player).stream()
				.filter(u -> u.stream().anyMatch(IWorker.class::isInstance)).collect(Collectors.toList());
		while (!units.isEmpty()) {
			final IUnit chosen = cli.chooseFromList((List<? extends IUnit>) units,
					"%s's units:".formatted(player.getName()), "No unadvanced units remain.", "Chosen unit:",
					ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
			if (Objects.isNull(chosen)) {
				break;
			}
			units.remove(chosen);
			helper.advanceWorkersInUnit(chosen, experienceConfig);
			// FIXME: Don't prompt if there aren't any more units
			switch (cli.inputBoolean("Choose another unit?")) {
				case YES -> { // Do nothing
				}
				case NO -> {
					return;
				}
				case QUIT -> { // TODO: Maybe abort from caller (but not its caller)
					return;
				}
				case EOF -> { // TODO: Signal EOF to callers
					return;
				}
			}
		}
	}

	/**
	 * Let the user choose a player to run worker advancement for.
	 */
	@Override
	public void startDriver() {
		final List<Player> playerList = StreamSupport.stream(model.getPlayers().spliterator(), false)
				.collect(Collectors.toList());
		while (!playerList.isEmpty()) {
			final Player chosen = cli.chooseFromList((List<? extends Player>) playerList, "Available players:",
					"No players found.", "Chosen player:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
			if (Objects.isNull(chosen)) {
				break;
			}
			playerList.remove(chosen);
			advanceWorkers(chosen, options.hasOption("--allow-expert-mentoring") ?
					AdvancementCLIHelper.ExperienceConfig.ExpertMentoring :
					AdvancementCLIHelper.ExperienceConfig.SelfTeaching);
			switch(cli.inputBoolean("Select another player?")) {
				case YES -> { // Do nothing
				}
				case NO, QUIT, EOF -> {
					return;
				}
			}
		}
	}
}
