import ceylon.collection {
	Queue
}
import strategicprimer.drivers.common {
	SimpleDriver,
	SPOptions,
	DriverUsage,
	ParamCount,
	IDriverModel,
	IDriverUsage,
	ISPDriver,
	IMultiMapModel
}
import strategicprimer.model.map {
	HasName,
	Point,
	IMutableMapNG
}
import java.nio.file {
	JPath=Path
}
import strategicprimer.viewer.drivers.exploration {
	HuntingModel
}
import strategicprimer.drivers.common.cli {
	ICLIHelper
}
import lovelace.util.common {
	todo
}
import ceylon.numeric.float {
	round=halfEven
}
import strategicprimer.model.map.fixtures.mobile {
	Animal,
	AnimalTracks
}
"Possible actions in the trapping CLI; top-level so we can switch on the cases,
 since the other alternative, `static`, isn't possible in an `object` anymore."
class TrapperCommand of setTrap | check | move | easyReset | quit
		satisfies HasName&Comparable<TrapperCommand> {
	shared actual String name;
	Integer ordinal;
	shared new setTrap { name = "Set or reset a trap"; ordinal = 0; }
	shared new check { name = "Check a trap"; ordinal = 1; }
	shared new move { name = "Move to another trap"; ordinal = 2; }
	shared new easyReset { name = "Reset a foothold trap, e.g."; ordinal = 3; }
	shared new quit { name = "Quit"; ordinal = 4; }
	shared actual Comparison compare(TrapperCommand other) => ordinal <=> other.ordinal;
}
class QueueWrapper<Type>(variable {Type*} wrapped) satisfies Queue<Type> {
	shared actual Type? accept() {
		Type? retval = wrapped.first;
		wrapped = wrapped.rest;
		return retval;
	}
	shared actual Type? back => wrapped.last;
	shared actual Type? front => wrapped.first;
	shared actual void offer(Type element) => wrapped = wrapped.chain(Singleton(element));
}
"A driver to run a player's trapping activity."
todo("Tests") // This'll have to wait until eclipse/ceylon#6986 is fixed
service(`interface ISPDriver`)
// FIXME: Write trapping (and hunting, etc.) GUI
shared class TrappingCLI() satisfies SimpleDriver {
	Integer minutesPerHour = 60;
	TrapperCommand[] commands = sort(`TrapperCommand`.caseValues);
	shared actual IDriverUsage usage = DriverUsage(false, ["-r", "--trap"], ParamCount.one,
		"Run a player's trapping", "Determine the results a player's trapper finds.", true,
		false);
	String inHours(Integer minutes) {
		if (minutes < minutesPerHour) {
			return "``minutes`` minutes";
		} else {
			return "``minutes / minutesPerHour`` hours, ``
			minutes % minutesPerHour`` minutes";
		}
	}
	"Handle a command. Returns how long it took to execute the command."
	Integer handleCommand(
			"The main map."
			IMutableMapNG map,
			"The animals generated from the tile and the surrounding tiles, with their home
			 locations."
			Queue<Point->Animal|AnimalTracks|HuntingModel.NothingFound> fixtures, ICLIHelper cli,
			"The command to handle"
			TrapperCommand command,
			"If true, we're dealing with *fish* traps, which have different costs"
			Boolean fishing,
			"Method to add animal traces to player maps"
			Anything(AnimalTracks) tracksHandler) {
		switch (command)
		case (TrapperCommand.check){
			<Point->Animal|AnimalTracks|HuntingModel.NothingFound>? top = fixtures.accept();
			if (!top exists) {
				cli.println("Ran out of results");
				return runtime.maxArraySize;
			}
			assert (exists top);
			Point loc = top.key;
			value item = top.item;
			if (is HuntingModel.NothingFound item) {
				cli.println("Nothing in the trap");
				return (fishing) then 5 else 10;
			} else if (is AnimalTracks item) { // TODO: Is this really possible now?
				cli.println("Found evidence of ``item.kind`` escaping");
				tracksHandler(item.copy(true));
				return (fishing) then 5 else 10;
			} else {
				cli.println("Found either ``item.kind`` or evidence of it escaping.");
				Integer num = cli.inputNumber("How long to check and deal with the animal? ");
				Integer retval;
				if (cli.inputBooleanInSeries("Handle processing now?")) {
					Integer mass = cli.inputNumber("Weight of meat in pounds: ");
					Integer hands = cli.inputNumber("# of workers processing this carcass: ");
					retval = num + round(HuntingModel.processingTime(mass) / hands).integer;
				} else {
					retval = num;
				}
				if (cli.inputBooleanInSeries(
						"Reduce animal group population of ``item.population``?")) {
					Integer count = Integer.smallest(cli.inputNumber("How many animals to remove?"),
						item.population);
					if (count > 0) {
						map.removeFixture(loc, item);
						Integer remaining = item.population - count;
						if (remaining > 0) {
							map.addFixture(loc, item.reduced(remaining));
							tracksHandler(AnimalTracks(item.kind));
						}
					} else {
						tracksHandler(AnimalTracks(item.kind));
					}
				} else {
					tracksHandler(AnimalTracks(item.kind));
				}
				return retval;
			}
		}
		case (TrapperCommand.easyReset) { return (fishing) then 20 else 5; }
		case (TrapperCommand.move) { return 2; }
		case (TrapperCommand.quit) { return 0; }
		case (TrapperCommand.setTrap) { return (fishing) then 30 else 45; }
	}
	shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
			IDriverModel model) {
		Boolean fishing = cli.inputBooleanInSeries(
			"Is this a fisherman trapping fish rather than a trapper? ");
		String name = (fishing) then "fisherman" else "trapper";
		variable Integer minutes = cli
				.inputNumber("How many hours will the ``name`` work? ") * minutesPerHour;
		Point point = cli.inputPoint("Where is the ``name`` working? ");
		HuntingModel huntModel = HuntingModel(model.map);
		Queue<Point->Animal|AnimalTracks|HuntingModel.NothingFound> fixtures;
		if (fishing) {
			fixtures = QueueWrapper(huntModel.fish(point));
		} else {
			fixtures = QueueWrapper(huntModel.hunt(point));
		}
		void addTracksToMaps(AnimalTracks tracks) {
			if (is IMultiMapModel model) {
				for ([subMap, _] in model.subordinateMaps) {
					subMap.addFixture(point, tracks);
				}
			}
		}
		while (minutes > 0, exists command = cli.chooseFromList(commands,
				"What should the ``name`` do next?", "Oops! No commands",
				"Next action: ", false).item) {
			minutes -= handleCommand(model.map, fixtures, cli, command, fishing, addTracksToMaps);
			cli.println("``inHours(minutes)`` remaining");
			if (command == TrapperCommand.quit) {
				break;
			}
		}
	}
	"As this is a CLI, we can't show a file-chooser dialog."
	shared actual {JPath*} askUserForFiles() => [];
}
