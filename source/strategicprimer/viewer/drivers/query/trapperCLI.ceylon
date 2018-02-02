import ceylon.collection {
	Queue
}
import strategicprimer.drivers.common {
	SimpleDriver,
	SPOptions,
	DriverUsage,
	ParamCount,
	IDriverModel,
	IDriverUsage
}
import java.lang {
	JInteger=Integer
}
import strategicprimer.model.map {
	HasName,
	Point
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
import ceylon.math.float {
	round=halfEven
}
"Possible actions in the trapping CLI; top-level so we can switch on the cases,
 since the other alternative, `static`, isn't possible in an `object` anymore."
class TrapperCommand of setTrap | check | move | easyReset | quit
		satisfies HasName {
	shared actual String name;
	shared new setTrap { name = "Set or reset a trap"; }
	shared new check { name = "Check a trap"; }
	shared new move { name = "Move to another trap"; }
	shared new easyReset { name = "Reset a foothold trap, e.g."; }
	shared new quit { name = "Quit"; }
}
class QueueWrapper(variable {String*} wrapped) satisfies Queue<String> {
	shared actual String? accept() {
		String? retval = wrapped.first;
		wrapped = wrapped.rest;
		return retval;
	}
	shared actual String? back => wrapped.last;
	shared actual String? front => wrapped.first;
	shared actual void offer(String element) => wrapped = wrapped.chain({element});
}
"A driver to run a player's trapping activity."
todo("Tests")
shared object trappingCLI satisfies SimpleDriver {
	Integer minutesPerHour = 60;
	// TODO: Use `TrapperCommand`.caseValues?
	TrapperCommand[] commands = [TrapperCommand.setTrap, TrapperCommand.check, TrapperCommand.move,
		TrapperCommand.easyReset, TrapperCommand.quit];
	shared actual IDriverUsage usage = DriverUsage(false, ["-r", "--trap"], ParamCount.one,
		"Run a player's trapping", "Determine the results a player's trapper finds.");
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
		"The animals generated from the tile and the surrounding tiles."
		Queue<String> fixtures, ICLIHelper cli,
		"The command to handle"
		TrapperCommand command,
		"If true, we're dealing with *fish* traps, which have different costs"
		Boolean fishing) {
		switch (command)
		case (TrapperCommand.check){
			String? top = fixtures.accept();
			if (!top exists) {
				cli.println("Ran out of results");
				return JInteger.maxValue;
			}
			assert (exists top);
			if (HuntingModel.noResults == top) {
				cli.println("Nothing in the trap");
				return (fishing) then 5 else 10;
			} else {
				cli.println("Found either ``top`` or evidence of it escaping.");
				Integer num = cli.inputNumber("How long to check and deal with the animal? ");
				if (cli.inputBooleanInSeries("Handle processing now?")) {
					Integer mass = cli.inputNumber("Weight of meat in pounds: ");
					Integer hands = cli.inputNumber("# of workers processing this carcass: ");
					return num + round(HuntingModel.processingTime(mass) / hands).integer;
				} else {
					return num;
				}
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
		Queue<String> fixtures;
		if (fishing) {
			fixtures = QueueWrapper(huntModel.fish(point));
		} else {
			fixtures = QueueWrapper(huntModel.hunt(point));
		}
		while (minutes > 0, exists command = cli.chooseFromList(commands,
			"What should the ``name`` do next?", "Oops! No commands",
			"Next action: ", false).item) {
			minutes -= handleCommand(fixtures, cli, command, fishing);
			cli.println("``inHours(minutes)`` remaining");
			if (command == TrapperCommand.quit) {
				break;
			}
		}
	}
	"As this is a CLI, we can't show a file-chooser dialog."
	shared actual {JPath*} askUserForFiles() => {};
}
