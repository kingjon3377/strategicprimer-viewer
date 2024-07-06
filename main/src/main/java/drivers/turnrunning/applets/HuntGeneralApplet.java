package drivers.turnrunning.applets;

import legacy.map.HasKind;
import legacy.map.IFixture;
import legacy.map.Player;
import legacy.map.Point;

import legacy.map.TileFixture;
import legacy.map.fixtures.IMutableResourcePile;
import exploration.common.HuntingModel;

import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.AnimalPlurals;

import drivers.common.cli.ICLIHelper;

import legacy.idreg.IDRegistrar;

import drivers.resourceadding.ResourceAddingCLIHelper;

import drivers.turnrunning.ITurnRunningModel;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import lovelace.util.LovelaceLogger;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

// TODO: Once we're on Java 17, make "sealed", limited to HuntingApplet, FishingApplet, and TrappingApplet
/* package */ abstract class HuntGeneralApplet extends AbstractTurnApplet {
	protected final HuntingModel huntingModel;
	protected final ResourceAddingCLIHelper resourceAddingHelper;
	protected final ITurnRunningModel model;
	protected final ICLIHelper cli;
	protected final IDRegistrar idf;
	protected final String verb;

	protected HuntGeneralApplet(final String verb, final ITurnRunningModel model, final ICLIHelper cli,
								final IDRegistrar idf) {
		super(model, cli);
		huntingModel = new HuntingModel(model.getMap());
		resourceAddingHelper = new ResourceAddingCLIHelper(cli, idf);
		this.model = model;
		this.cli = cli;
		this.idf = idf;
		this.verb = verb;
	}

	/**
	 * A description of what could be a single animal or a population of animals.
	 */
	private static String populationDescription(final Animal animal) {
		if (animal.getPopulation() > 1) {
			return "a group of perhaps %d %s".formatted(animal.getPopulation(),
					AnimalPlurals.get(animal.getKind()));
		} else {
			return animal.getKind();
		}
	}

	private static String describeUnit(final IUnit unit) {
		return "%s (%s)".formatted(unit.getName(), unit.getKind());
	}

	protected @Nullable Boolean handleCapture(final HasKind find) {
		final IUnit unit = chooseFromList(model.getUnits(Optional.ofNullable(model.getSelectedUnit())
						.map(IUnit::owner).orElse(model.getMap().getCurrentPlayer())),
				"Available units:", "No units", "Unit to add animals to:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT,
				HuntGeneralApplet::describeUnit);
		if (Objects.isNull(unit)) {
			return false;
		} else {
			final Integer num = cli.inputNumber("Number captured:");
			if (Objects.isNull(num)) {
				return null;
			} else {
				return model.addAnimal(unit, find.getKind(), "wild", idf.createID(), num);
			}
		}
	}

	protected @Nullable Integer processMeat() {
		int cost = 0;
		// TODO: somehow handle processing-in-parallel case
		final Integer iterations = cli.inputNumber("How many carcasses?");
		if (Objects.isNull(iterations)) {
			return null;
		}
		for (int i = 0; i < iterations; i++) {
			final Integer mass = cli.inputNumber("Weight of this animal's meat in pounds: ");
			if (Objects.isNull(mass)) {
				return null;
			}
			final Integer hands = cli.inputNumber("# of workers processing this carcass: ");
			if (Objects.isNull(hands)) {
				return null;
			}
			cost += (int) Math.round(HuntingModel.processingTime(mass) / hands);
		}
		return cost;
	}

	protected void resourceEntry(final Player owner) {
		cli.println("Enter resources produced (any empty string aborts):");
		while (true) {
			final IMutableResourcePile resource = resourceAddingHelper.enterResource();
			if (Objects.isNull(resource)) {
				break;
			}
			if ("food".equals(resource.getKind())) {
				resource.setCreated(model.getMap().getCurrentTurn());
			}
			if (!model.addExistingResource(resource, owner)) {
				cli.println("Failed to find a fortress to add to in any map");
			}
		}
	}

	private @Nullable Integer handleFight(final Point loc, final Animal find, final int time) {
		int cost;
		final Integer temp = cli.inputNumber("Time to %s: ".formatted(verb));
		if (Objects.isNull(temp)) {
			return null;
		} else {
			cost = temp;
		}
		final Boolean capture = cli.inputBooleanInSeries("Capture any animals?");
		if (Objects.isNull(capture)) {
			return null;
		} else if (capture && Objects.isNull(handleCapture(find))) {
			return null;
		}
		final Boolean processNow = cli.inputBooleanInSeries("Process carcasses now?");
		if (Objects.isNull(processNow)) {
			return null;
		} else if (processNow) {
			final Integer processingTime = processMeat();
			if (Objects.isNull(processingTime)) {
				return null;
			}
			cost += processingTime;
		}
		final Boolean reduce = cli.inputBooleanInSeries("Reduce animal group population of %d?"
				.formatted(find.getPopulation()));
		if (Objects.isNull(reduce)) {
			return null;
		} else if (reduce) {
			reducePopulation(loc, find, "animals", IFixture.CopyBehavior.ZERO);
		} else {
			model.copyToSubMaps(loc, find, IFixture.CopyBehavior.ZERO);
		}
		if (!Objects.isNull(model.getSelectedUnit())) {
			resourceEntry(model.getSelectedUnit().owner());
		}
		return cost;
	}

	private @Nullable Integer handleEncounter(final StringBuilder buffer, final int time, final Point loc,
			/*Animal|AnimalTracks|HuntingModel.NothingFound*/ final TileFixture find) {
		switch (find) {
			case final HuntingModel.NothingFound nothingFound -> {
				cli.printf("Found nothing for the next %d minutes.%n", NO_RESULT_COST);
				return NO_RESULT_COST;
			}
			case final AnimalTracks at -> {
				model.copyToSubMaps(loc, find, IFixture.CopyBehavior.ZERO);
				cli.printf("Found only tracks or traces from %s for the next %d minutes.%n",
						at.getKind(), NO_RESULT_COST);
				return NO_RESULT_COST;
			}
			case final Animal a -> {
				final Boolean fight = cli.inputBooleanInSeries("Found %s. Should they %s?".formatted(
						populationDescription(a), verb), a.getKind());
				if (Objects.isNull(fight)) {
					return null;
				} else if (fight) {
					return handleFight(loc, (Animal) find, time);
				} else {
					model.copyToSubMaps(loc, find, IFixture.CopyBehavior.ZERO);
					return NO_RESULT_COST;
				}
			}
			default -> {
				LovelaceLogger.error("Unhandled case from hunting model");
				return null;
			}
		}
	}

	// TODO: Distinguish hunting from fishing in no-result time cost (encounters / hour)?
	// Note that the intended return type of encounterSrc::apply is Pair<Point, Animal|AnimalTracks|NothingFound>,
	// but Java doesn't offer union types.
	protected @Nullable String impl(final String command,
									final Function<Point, Iterable<Pair<Point, TileFixture>>> encounterSrc) {
		final StringBuilder buffer = new StringBuilder();
		final Point center = confirmPoint("Location to search around: ");
		if (Objects.isNull(center)) {
			return ""; // TODO: return null, surely?
		}
		final Integer startingTime = cli.inputNumber("Minutes to spend %sing: ".formatted(command));
		if (Objects.isNull(startingTime)) {
			return ""; // TODO: return null, surely?
		}
		int time = startingTime;
		int noResultsTime = 0;
		final Iterator<Pair<Point, TileFixture>> encounters = encounterSrc.apply(center).iterator();
		while (time > 0 && encounters.hasNext()) {
			final Pair<Point, TileFixture> pair = encounters.next();
			final Point loc = pair.getValue0();
			final TileFixture find = pair.getValue1();
			if (find instanceof HuntingModel.NothingFound) {
				noResultsTime += NO_RESULT_COST;
				time -= NO_RESULT_COST;
				if (time <= 0) {
					cli.print("Found nothing for the next ");
					cli.println(inHours(noResultsTime));
				}
				continue;
			} else if (noResultsTime > 0) {
				cli.print("Found nothing for the next ");
				cli.println(inHours(noResultsTime));
				final String addendum = cli.inputMultilineString("Add to results about that:");
				if (Objects.isNull(addendum)) {
					return null;
				}
				buffer.append(addendum);
				noResultsTime = 0;
			}
			cli.print(inHours(time));
			cli.println(" remaining.");
			final Integer cost = handleEncounter(buffer, time, loc, find);
			if (Objects.isNull(cost)) {
				return null;
			}
			time -= cost;
			final String addendum = cli.inputMultilineString("Add to results about that:");
			if (Objects.isNull(addendum)) {
				return null;
			}
			buffer.append(addendum);
		}
		return buffer.toString().strip();
	}
}
