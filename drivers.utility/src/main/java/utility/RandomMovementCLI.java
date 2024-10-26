package utility;

import legacy.map.fixtures.mobile.IUnit;
import drivers.common.SPOptions;
import drivers.common.CLIDriver;

import exploration.common.IExplorationModel;
import exploration.common.Speed;
import exploration.common.TraversalImpossibleException;

import legacy.map.Direction;
import legacy.map.Player;

import java.util.Random;

/**
 * An app to move independent units around at random.
 */
/* package */ final class RandomMovementCLI implements CLIDriver {
	public RandomMovementCLI(final SPOptions options, final IExplorationModel model) {
		// TODO Add listener to report movement players would notice
		this.options = options;
		this.model = model;
	}

	private final IExplorationModel model;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	@Override
	public IExplorationModel getModel() {
		return model;
	}

	private final SPOptions options;

	@Override
	public void startDriver() {
		final Direction[] directions = Direction.values();
		final Random rng = new Random();
		for (final IUnit unit : model.getPlayerChoices().stream()
				.filter(Player::isIndependent).flatMap(p -> model.getUnits(p).stream()).toList()) {
			// TODO: Pull out of loop and call setSeed() on it here instead.
			rng.setSeed(unit.getId() << 8 + model.getMap().getCurrentTurn());
			final int steps = rng.nextInt(3) + rng.nextInt(3);
			model.setSelectedUnit(unit);
			for (int i = 0; i < steps; i++) {
				try {
					model.move(directions[rng.nextInt(directions.length)], Speed.Normal);
				} catch (final TraversalImpossibleException except) {
					// ignore ... normal control flow
				}
			}
		}
	}
}
