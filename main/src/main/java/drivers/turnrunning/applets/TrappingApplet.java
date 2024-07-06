package drivers.turnrunning.applets;

import legacy.idreg.IDRegistrar;

import legacy.map.IFixture;
import legacy.map.TileFixture;
import drivers.common.cli.ICLIHelper;

import exploration.common.HuntingModel;

import legacy.map.Point;

import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.Animal;

import drivers.turnrunning.ITurnRunningModel;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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

	private static final List<TrapperCommand> TRAPPER_COMMANDS =
			List.of(TrapperCommand.values());

	private @Nullable Integer handleFound(final Point center, final Point loc, final Animal item) {
		int cost;
		cli.printf("Found either %s or evidence of it escaping.%n", item.getKind());
		final Integer num = cli.inputNumber("How long to check and deal with the animal? ");
		if (Objects.isNull(num)) {
			return null;
		}
		cost = num;
		final Boolean live = cli.inputBooleanInSeries("Is an animal captured live?");
		if (Objects.isNull(live)) {
			return null;
		} else if (live && Objects.isNull(handleCapture(item))) {
			return null;
		}
		final Boolean process = cli.inputBooleanInSeries("Handle processing now?");
		if (Objects.isNull(process)) {
			return null;
		} else if (process) {
			final Integer processingTime = processMeat();
			if (Objects.isNull(processingTime)) {
				return null;
			}
			cost += processingTime;
		}
		final Boolean reduce = cli.inputBooleanInSeries("Reduce animal group population of %d?"
				.formatted(item.getPopulation()));
		if (Objects.isNull(reduce)) {
			return null;
		} else if (reduce) {
			reducePopulation(loc, item, "animals", IFixture.CopyBehavior.ZERO);
		} else {
			model.copyToSubMaps(center, new AnimalTracks(item.getKind()), IFixture.CopyBehavior.KEEP);
		}
		if (!Objects.isNull(model.getSelectedUnit())) {
			resourceEntry(model.getSelectedUnit().owner());
		}
		return cost;
	}

	@Override
	public @Nullable String run() {
		final StringBuilder buffer = new StringBuilder();
		final Boolean fishing = cli.inputBooleanInSeries(
				"Is this a fisherman trapping fish rather than a trapper?");
		if (Objects.isNull(fishing)) {
			return ""; // TODO: null, surely?
		}
		final Point center = confirmPoint("Location to search around: ");
		if (Objects.isNull(center)) {
			return ""; // TODO: null, surely?
		}
		final Integer startingTime = cli.inputNumber("Minutes to spend working: ");
		if (Objects.isNull(startingTime)) {
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
			final TrapperCommand command = cli.chooseFromList(TRAPPER_COMMANDS, prompt,
					"Oops! No commands", "Next action: ", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
			if (Objects.isNull(command) || TrapperCommand.Quit == command) {
				break;
			}
			boolean out = false; // TODO: Just set 'time' to 0, right?
			switch (command) {
				case Check -> {
					if (!encounters.hasNext()) {
						out = true;
						cli.println("Ran out of results!");
						break;
					}
					final Pair<Point, TileFixture> find = encounters.next();
					final Point loc = find.getValue0();
					final TileFixture item = find.getValue1();
					switch (item) {
						case final HuntingModel.NothingFound nothingFound -> {
							cli.println("Nothing in the trap");
							time -= nothingCost;
						}
						case final AnimalTracks at -> {
							cli.printf("Found evidence of %s escaping%n", at.getKind());
							model.copyToSubMaps(center, item, IFixture.CopyBehavior.ZERO);
							time -= nothingCost;
						}
						case final Animal animal -> {
							final Integer cost = handleFound(center, loc, animal);
							if (Objects.isNull(cost)) {
								return null;
							}
							time -= cost;
						}
						default -> throw new IllegalStateException("Unhandled case from HuntingModel");
					}
				}
				case EasyReset -> {
					if (fishing) {
						time -= 20;
					} else {
						time -= 5;
					}
				}
				case Move -> time -= 2;

//			case Quit -> time = 0;
				case SetTrap -> {
					if (fishing) {
						time -= 30;
					} else {
						time -= 45;
					}
				}
				default -> throw new IllegalStateException("Exhaustive switch wasn't");
			}
			if (out) {
				break;
			}
			cli.print(inHours(time));
			cli.println(" remaining.");
			final String addendum = cli.inputMultilineString("Add to results about that:");
			if (Objects.isNull(addendum)) {
				return null;
			}
			buffer.append(addendum);
		}
		return buffer.toString(); // TODO: trim?
	}
}
