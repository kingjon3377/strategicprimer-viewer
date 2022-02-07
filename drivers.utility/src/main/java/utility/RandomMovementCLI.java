package utility;

import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import common.map.fixtures.mobile.IUnit;
import drivers.common.SPOptions;
import drivers.common.CLIDriver;

import drivers.common.cli.ICLIHelper;

import exploration.common.IExplorationModel;
import exploration.common.Speed;
import exploration.common.TraversalImpossibleException;

import common.map.Direction;
import common.map.Player;

import java.util.Random;

/**
 * An app to move independent units around at random.
 */
/* package */ class RandomMovementCLI implements CLIDriver {
	public RandomMovementCLI(final ICLIHelper cli, final SPOptions options, final IExplorationModel model) {
		this.cli = cli;
		this.options = options;
		this.model = model;
	}

	private final ICLIHelper cli;

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
		Direction[] directions = Direction.values();
		for (IUnit unit : StreamSupport.stream(model.getPlayerChoices().spliterator(), false)
				.filter(Player::isIndependent).flatMap(p -> model.getUnits(p).stream())
				.collect(Collectors.toList())) {
			Random rng = new Random(unit.getId() << 8 + model.getMap().getCurrentTurn());
			int steps = rng.nextInt(3) + rng.nextInt(3);
			model.setSelectedUnit(unit);
			for (int i = 0; i < steps; i++) {
				try {
					model.move(directions[rng.nextInt(directions.length)], Speed.Normal);
				} catch (final TraversalImpossibleException except) {
					continue;
				}
			}
		}
	}
}
