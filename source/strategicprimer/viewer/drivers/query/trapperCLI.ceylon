import ceylon.collection {
	LinkedList,
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
"Possible actions in the trapping CLI; top-level so we can switch on the cases,
 since the other alternative, `static`, isn't possible in an `object` anymore."
// TODO: Convert to object-constructors
abstract class TrapperCommand(name) of setTrap | check | move | easyReset | quit
		satisfies HasName {
	shared actual String name;
}
object setTrap extends TrapperCommand("Set or reset a trap") {}
object check extends TrapperCommand("Check a trap") {}
object move extends TrapperCommand("Move to another trap") {}
object easyReset extends TrapperCommand("Reset a foothold trap, e.g.") {}
object quit extends TrapperCommand("Quit") {}
"A driver to run a player's trapping activity."
todo("Tests")
shared object trappingCLI satisfies SimpleDriver {
	Integer minutesPerHour = 60;
	TrapperCommand[] commands = [setTrap, check, move, easyReset, quit];
	shared actual IDriverUsage usage = DriverUsage(false, "-r", "--trap", ParamCount.one,
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
		case (check){
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
				return cli.inputNumber("How long to check and deal with the animal? ");
			}
		}
		case (easyReset) { return (fishing) then 20 else 5; }
		case (move) { return 2; }
		case (quit) { return 0; }
		case (setTrap) { return (fishing) then 30 else 45; }
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
			fixtures = LinkedList { *huntModel.fish(point, minutes)};
		} else {
			fixtures = LinkedList { *huntModel.hunt(point, minutes)};
		}
		while (minutes > 0, exists command = cli.chooseFromList(commands,
			"What should the ``name`` do next?", "Oops! No commands",
			"Next action: ", false).item) {
			minutes -= handleCommand(fixtures, cli, command, fishing);
			cli.println("``inHours(minutes)`` remaining");
			if (command == quit) {
				break;
			}
		}
	}
	"As this is a CLI, we can't show a file-chooser dialog."
	shared actual {JPath*} askUserForFiles() => {};
}
