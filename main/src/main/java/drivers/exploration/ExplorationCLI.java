package drivers.exploration;

import java.util.logging.Logger;

import org.jetbrains.annotations.Nullable;

import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

import drivers.common.SPOptions;
import drivers.common.CLIDriver;
import drivers.common.EmptyOptions;

import drivers.common.cli.ICLIHelper;

import exploration.common.IExplorationModel;

import common.map.Player;

import common.map.fixtures.mobile.IUnit;

/**
 * A CLI to help running exploration.
 */
/* package */ class ExplorationCLI implements CLIDriver {
	private static final Logger LOGGER = Logger.getLogger(ExplorationCLI.class.getName());
	public ExplorationCLI(final ICLIHelper cli, final IExplorationModel model) {
		this.cli = cli;
		this.model = model;
	}

	private final ICLIHelper cli;
	private final IExplorationModel model;

	@Override public IExplorationModel getModel() {
		return model;
	}

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	/**
	 * Have the user choose a player.
	 */
	@Nullable
	public Player choosePlayer() {
		return cli.chooseFromList(StreamSupport.stream(model.getPlayerChoices().spliterator(), false)
				.collect(Collectors.toList()), "Players shared by all the maps:",
			"No players shared by all the maps:", "Chosen player: ", true).getValue1();
	}

	/**
	 * Have the user choose a unit belonging to that player.
	 */
	@Nullable
	public IUnit chooseUnit(final Player player) {
		return cli.chooseFromList(model.getUnits(player), "Player's units:",
			"That player has no units in the master map", "Chosen unit: ", true).getValue1();
	}

	@Override
	public void startDriver() {
		// TODO: Set up eCLI in constructor, surely? Or else remove it as listener at end
		ExplorationCLIHelper eCLI = new ExplorationCLIHelper(model, cli);
		model.addSelectionChangeListener(eCLI);
		model.addMovementCostListener(eCLI);
		Player player = choosePlayer();
		if (player != null) {
			IUnit unit = chooseUnit(player);
			if (unit != null) {
				model.setSelectedUnit(unit);
				while (eCLI.getMovement() > 0) {
					eCLI.moveOneStep();
				}
			}
		}
	}
}
