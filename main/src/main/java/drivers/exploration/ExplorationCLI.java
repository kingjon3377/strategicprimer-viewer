package drivers.exploration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import drivers.common.SPOptions;
import drivers.common.CLIDriver;
import drivers.common.EmptyOptions;

import drivers.common.cli.ICLIHelper;

import exploration.common.IExplorationModel;

import legacy.map.Player;

import legacy.map.fixtures.mobile.IUnit;

/**
 * A CLI to help running exploration.
 */
/* package */ final class ExplorationCLI implements CLIDriver {
	public ExplorationCLI(final ICLIHelper cli, final IExplorationModel model) {
		this.cli = cli;
		this.model = model;
	}

	private final ICLIHelper cli;
	private final IExplorationModel model;

	@Override
	public IExplorationModel getModel() {
		return model;
	}

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	/**
	 * Have the user choose a player.
	 */
	public @Nullable Player choosePlayer() {
		return cli.chooseFromList((List<? extends Player>) new ArrayList<>(model.getPlayerChoices()),
				"Players shared by all the maps:", "No players shared by all the maps:", "Chosen player: ",
				ICLIHelper.ListChoiceBehavior.AUTO_CHOOSE_ONLY).getValue1();
	}

	/**
	 * Have the user choose a unit belonging to that player.
	 */
	public @Nullable IUnit chooseUnit(final Player player) {
		return cli.chooseFromList((List<? extends IUnit>) model.getUnits(player), "Player's units:",
				"That player has no units in the master map", "Chosen unit: ",
				ICLIHelper.ListChoiceBehavior.AUTO_CHOOSE_ONLY).getValue1();
	}

	@Override
	public void startDriver() {
		final ExplorationCLIHelper eCLI = new ExplorationCLIHelper(model, cli);
		model.addSelectionChangeListener(eCLI);
		model.addMovementCostListener(eCLI);
		final Player player = choosePlayer();
		if (Objects.nonNull(player)) {
			final IUnit unit = chooseUnit(player);
			if (Objects.nonNull(unit)) {
				model.setSelectedUnit(unit);
				while (eCLI.getMovement() > 0) {
					switch (eCLI.moveOneStep()) {
						case YES, NO -> {
						}
						case QUIT, EOF -> { return;
						}
					}
				}
			}
		}
		model.removeSelectionChangeListener(eCLI);
		model.removeMovementCostListener(eCLI);
	}
}
