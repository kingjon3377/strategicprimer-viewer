package drivers.turnrunning.applets;

import legacy.idreg.IDRegistrar;

import legacy.map.HasName;
import legacy.map.IFixture;
import legacy.map.TileFixture;
import drivers.common.cli.ICLIHelper;

import exploration.common.HuntingModel;

import legacy.map.Point;

import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.Animal;

import drivers.turnrunning.ITurnRunningModel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.javatuples.Pair;
import org.jspecify.annotations.Nullable;

/* package */ final class TrappingApplet extends HuntGeneralApplet {
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
		switch (cli.inputBooleanInSeries("Is an animal captured live?")) {
			case YES -> {
				if (Objects.isNull(handleCapture(item))) {
					return null;
				}
			}
			case NO -> { // Do nothing
			}
			case QUIT -> {
				return cost;
			}
			case EOF -> {
				return null;
			}
		}
		switch (cli.inputBooleanInSeries("Handle processing now?")) {
			case YES -> {
				final Integer processingTime = processMeat();
				if (Objects.isNull(processingTime)) {
					return null;
				}
				cost += processingTime;
			}
			case NO -> { // Do nothing
			}
			case QUIT -> {
				return cost;
			}
			case EOF -> {
				return null;
			}
		}
		switch (cli.inputBooleanInSeries("Reduce animal group population of %d?"
				.formatted(item.getPopulation()))) {
			case YES -> reducePopulation(loc, item, "animals", IFixture.CopyBehavior.ZERO);
			case NO -> model.copyToSubMaps(center, new AnimalTracks(item.getKind()), IFixture.CopyBehavior.KEEP);
			case QUIT -> {
				return cost;
			}
			case EOF -> {
				return null;
			}
		}
		if (Objects.nonNull(model.getSelectedUnit())) {
			resourceEntry(model.getSelectedUnit().owner());
		}
		return cost;
	}

	@SuppressWarnings("MagicNumber")
	@Override
	public @Nullable String run() {
		final StringBuilder buffer = new StringBuilder();
		final Function<Point, Supplier<Pair<Point, ? extends TileFixture>>> encountersGenerator;
		final String prompt;
		final int nothingCost;
		final int resetCost;
		final int trapSetCost;
		switch (cli.inputBooleanInSeries(
				"Is this a fisherman trapping fish rather than a trapper?")) {
			case YES -> {
				encountersGenerator = huntingModel::fish;
				prompt = "What should the fisherman do next?";
				nothingCost = 5;
				resetCost = 20;
				trapSetCost = 30;
			}
			case NO -> {
				encountersGenerator = huntingModel::hunt;
				prompt = "What should the trapper do next?";
				nothingCost = 10;
				resetCost = 5;
				trapSetCost = 45;
			}
			case QUIT -> {
				return "";
			}
			case EOF -> {
				return null;
			}
			default -> throw new IllegalStateException("Exhaustive switch wasn't");
		}
		final Point center = confirmPoint("Location to search around: ");
		if (Objects.isNull(center)) {
			return null;
		}
		final Integer startingTime = cli.inputNumber("Minutes to spend working: ");
		if (Objects.isNull(startingTime)) {
			return null;
		}
		final Supplier<Pair<Point, /*Animal|AnimalTracks|HuntingModel.NothingFound*/? extends TileFixture>> encounters =
				encountersGenerator.apply(center);
		int time = startingTime;
		while (time > 0) {
			final TrapperCommand command = cli.chooseFromList(TRAPPER_COMMANDS, prompt,
					"Oops! No commands", "Next action: ", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
			if (Objects.isNull(command) || TrapperCommand.Quit == command) {
				break;
			}
			switch (command) {
				case Check -> {
					final Pair<Point, ? extends TileFixture> find = encounters.get();
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
				case EasyReset -> time -= resetCost;
				case Move -> time -= 2;

//			case Quit -> time = 0;
				case SetTrap -> time -= trapSetCost;
				default -> throw new IllegalStateException("Exhaustive switch wasn't");
			}
			cli.print(inHours(time));
			cli.println(" remaining.");
			final String addendum = cli.inputMultilineString("Add to results about that:");
			if (Objects.isNull(addendum)) {
				return null;
			}
			buffer.append(addendum);
		}
		return buffer.toString().strip();
	}

	/**
	 * Possible actions a trapper can take.
	 */
	private enum TrapperCommand implements HasName {
		SetTrap("Set or reset a trap"),
		Check("Check a trap"),
		Move("Move to another trap"),
		EasyReset("Reset a foothold trap, e.g."),
		Quit("Quit");

		TrapperCommand(final String name) {
			this.name = name;
		}

		private final String name;

		@Override
		public String getName() {
			return name;
		}
	}
}
