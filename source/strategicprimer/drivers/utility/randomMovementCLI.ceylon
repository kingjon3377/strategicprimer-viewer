import strategicprimer.drivers.common {
	SimpleCLIDriver,
	SPOptions,
	IDriverModel,
	IDriverUsage,
	DriverUsage,
	ParamCount,
	ISPDriver
}
import strategicprimer.drivers.common.cli {
	ICLIHelper
}
import strategicprimer.drivers.exploration.common {
	IExplorationModel,
	ExplorationModel,
	Direction,
	Speed,
	TraversalImpossibleException
}
import strategicprimer.model.map {
	Player
}
import ceylon.random {
	Random,
	DefaultRandom
}
"An app to move independent units around at random."
service(`interface ISPDriver`)
shared class RandomMovementCLI() satisfies SimpleCLIDriver {
	shared actual IDriverUsage usage = DriverUsage(false, ["-v", "--move"], ParamCount.one,
		"Move independent units at random", "Move independent units randomly around the map.", true, false); // TODO: We'd like a GUI for this, perhaps adding customization or limiting the area or something
	shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options, IDriverModel model) {
		IExplorationModel emodel;
		if (is IExplorationModel model) {
			emodel = model;
		} else {
			emodel = ExplorationModel.copyConstructor(model);
		}
		for (unit in emodel.playerChoices.filter(Player.independent).flatMap(emodel.getUnits).sequence()) {
			Random rng = DefaultRandom(unit.id.leftLogicalShift(8) + model.map.currentTurn);
			Integer steps = rng.nextInteger(3) + rng.nextInteger(3);
			emodel.selectedUnit = unit;
			for (i in 0:steps) {
				try {
					emodel.move(rng.nextElement(`Direction`.caseValues) else Direction.nowhere, Speed.normal);
				} catch (TraversalImpossibleException except) {
					continue;
				}
			}
		}
	}
}
