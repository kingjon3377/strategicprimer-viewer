package drivers.advancement;

import drivers.common.SPOptions;
import drivers.common.CLIDriver;
import drivers.common.IWorkerModel;

import drivers.common.cli.ICLIHelper;

import common.map.Player;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.IUnit;

import java.util.List;

/**
 * The worker-advancement CLI driver.
 */
public class AdvancementCLI implements CLIDriver {
	public AdvancementCLI(final ICLIHelper cli, final SPOptions options, final IWorkerModel model) {
		this.cli = cli;
		this.options = options;
		this.model = model;
		helper = new AdvancementCLIHelper(model, cli);
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
	private void advanceWorkers(final IWorkerModel model, final Player player, final boolean allowExpertMentoring) {
		final List<IUnit> units = model.getUnits(player).stream()
				.filter(u -> u.stream().anyMatch(IWorker.class::isInstance)).collect(Collectors.toList());
		while (!units.isEmpty()) {
			final IUnit chosen = cli.chooseFromList((List<? extends IUnit>) units, String.format("%s's units:", player.getName()), "No unadvanced units remain.", "Chosen unit:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
			if (chosen == null) {
				break;
			}
			units.remove(chosen);
			helper.advanceWorkersInUnit(chosen, allowExpertMentoring);
			final Boolean continuation = cli.inputBoolean("Choose another unit?");
			if (continuation == null || !continuation) {
				break;
			}
		}
	}

	/**
	 * Let the user choose a player to run worker advancement for.
	 */
	@Override
	public void startDriver() {
		final List<Player> playerList = StreamSupport.stream(model.getPlayers().spliterator(), false).collect(Collectors.toList());
		while (!playerList.isEmpty()) {
			final Player chosen = cli.chooseFromList((List<? extends Player>) playerList, "Available players:", "No players found.", "Chosen player:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
			if (chosen == null) {
				break;
			}
			playerList.remove(chosen);
			advanceWorkers(model, chosen, options.hasOption("--allow-expert-mentoring"));
			final Boolean continuation = cli.inputBoolean("Select another player?");
			if (continuation == null || !continuation) {
				break;
			}
		}
	}
}
