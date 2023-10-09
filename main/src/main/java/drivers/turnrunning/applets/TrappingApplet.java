package drivers.turnrunning.applets;

import common.idreg.IDRegistrar;

import common.map.IFixture;
import common.map.TileFixture;
import drivers.common.cli.ICLIHelper;

import exploration.common.HuntingModel;

import common.map.Point;

import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.Animal;

import drivers.turnrunning.ITurnRunningModel;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

/* package */ class TrappingApplet extends HuntGeneralApplet {
	public TrappingApplet(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		super("trap", model, cli, idf);
	}

	private static final List<String> COMMANDS = Collections.singletonList("trap");

	@Override
	public List<String> getCommands() {
		return COMMANDS;
	}

	@Override
	public String getDescription() {
		return "check traps for animals or fish they may have caught";
	}

	private final List<TrapperCommand> trapperCommands =
			List.of(TrapperCommand.values());

	private @Nullable Integer handleFound(final Point center, final Point loc, final Animal item) {
		int cost;
		cli.println(String.format("Found either %s or evidence of it escaping.", item.getKind()));
		final Integer num = cli.inputNumber("How long to check and deal with the animal? ");
		if (num == null) {
			return null;
		}
		cost = num;
		final Boolean live = cli.inputBooleanInSeries("Is an animal captured live?");
		if (live == null) {
			return null;
		} else if (live && handleCapture(item) == null) {
			return null;
		}
		final Boolean process = cli.inputBooleanInSeries("Handle processing now?");
		if (process == null) {
			return null;
		} else if (process) {
			final Integer processingTime = processMeat();
			if (processingTime == null) {
				return null;
			}
			cost += processingTime;
		}
		final Boolean reduce = cli.inputBooleanInSeries(String.format("Reduce animal group population of %d?",
			item.getPopulation()));
		if (reduce == null) {
			return null;
		} else if (reduce) {
			reducePopulation(loc, item, "animals", IFixture.CopyBehavior.ZERO);
		} else {
			model.copyToSubMaps(center, new AnimalTracks(item.getKind()), IFixture.CopyBehavior.KEEP);
		}
		if (model.getSelectedUnit() != null) {
			resourceEntry(model.getSelectedUnit().owner());
		}
		return cost;
	}

	@Override
	public @Nullable String run() {
		final StringBuilder buffer = new StringBuilder();
		final Boolean fishing = cli.inputBooleanInSeries(
			"Is this a fisherman trapping fish rather than a trapper?");
		if (fishing == null) {
			return ""; // TODO: null, surely?
		}
		final Point center = confirmPoint("Location to search around: ");
		if (center == null) {
			return ""; // TODO: null, surely?
		}
		final Integer startingTime = cli.inputNumber("Minutes to spend working: ");
		if (startingTime == null) {
			return ""; // TODO: null, surely?
		}
		final Iterator<Pair<Point, /*Animal|AnimalTracks|HuntingModel.NothingFound*/TileFixture>> encounters;
		final String prompt;
		final int nothingCost;
		if (fishing) {
			encounters = huntingModel.fish(center).iterator();
			prompt = "What should the fisherman do next?";
			nothingCost = 5;
		} else {
			encounters = huntingModel.hunt(center).iterator();
			prompt = "What should the trapper do next?";
			nothingCost = 10;
		}
		int time = startingTime;
		while (time > 0) {
			final TrapperCommand command = cli.chooseFromList(trapperCommands, prompt,
				"Oops! No commands", "Next action: ", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
			if (command == null || TrapperCommand.Quit == command) {
				break;
			}
			boolean out = false;
			switch (command) {
			case Check:
				if (!encounters.hasNext()) {
					out = true;
					cli.println("Ran out of results!");
					break;
				}
				final Pair<Point, TileFixture> find = encounters.next();
				final Point loc = find.getValue0();
				final TileFixture item = find.getValue1();
				if (item instanceof HuntingModel.NothingFound) {
					cli.println("Nothing in the trap");
					time -= nothingCost;
					break;
				} else if (item instanceof final AnimalTracks at) {
					cli.println(String.format("Found evidence of %s escaping", at.getKind()));
					model.copyToSubMaps(center, item, IFixture.CopyBehavior.ZERO);
					time -= nothingCost;
					break;
				} else if (!(item instanceof Animal)) {
					throw new IllegalStateException("Unhandled case from HuntingModel");
				}
				final Integer cost = handleFound(center, loc, (Animal) item);
				if (cost == null) {
					return null;
				}
				time -= cost;
				break;
			case EasyReset:
				if (fishing) {
					time -= 20;
				} else {
					time -= 5;
				}
				break;
			case Move:
				time -= 2;
				break;
//			case Quit:
//				time = 0;
//				break;
			case SetTrap:
				if (fishing) {
					time -= 30;
				} else {
					time -= 45;
				}
				break;
			default:
				throw new IllegalStateException("Exhaustive switch wasn't");
			}
			if (out) {
				break;
			}
			cli.print(inHours(time));
			cli.println(" remaining.");
			final String addendum = cli.inputMultilineString("Add to results about that:");
			if (addendum == null) {
				return null;
			}
			buffer.append(addendum);
		}
		return buffer.toString(); // TODO: trim?
	}
}
