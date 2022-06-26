package drivers.turnrunning.applets;

import common.idreg.IDRegistrar;
import common.map.fixtures.Quantity;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.AnimalPlurals;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.towns.IFortress;
import drivers.common.cli.ICLIHelper;
import drivers.turnrunning.ITurnRunningModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;
import query.HerdModel;
import query.MammalModel;
import query.PoultryModel;
import query.SmallAnimalModel;

/* package */ class HerdApplet extends AbstractTurnApplet {
	public HerdApplet(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		super(model, cli);
		this.model = model;
		this.cli = cli;
		this.idf = idf;
	}


	private final ITurnRunningModel model;
	private final ICLIHelper cli;
	private final IDRegistrar idf;

	private static final List<String> COMMANDS = Collections.singletonList("herd");

	@Override
	public List<String> getCommands() {
		return COMMANDS;
	}

	@Override
	public String getDescription() {
		return "milk or gather eggs from animals";
	}

	// TODO: Pull up to AbstractTurnApplet for use by other applets?
	private @Nullable IFortress containingFortress(final IUnit unit) {
		return model.getMap().getFixtures(model.find(unit)).stream().filter(IFortress.class::isInstance)
			.map(IFortress.class::cast).filter(f -> f.owner().equals(unit.owner()))
			.findAny().orElse(null);
	}

	private final Map<String, HerdModel> herdModels = new HashMap<>();

	private @Nullable HerdModel chooseHerdModel(final String animal) {
		return cli.chooseFromList(Stream.concat(Stream.of(MammalModel.values()), Stream.concat(
						Stream.of(PoultryModel.values()), Stream.of(SmallAnimalModel.values())))
				.collect(Collectors.toList()), String.format("What kind of animal(s) is/are %s?", animal), "No animal kinds found", "Kind of animal:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
	}

	@Override
	public @Nullable String run() {
		final IUnit unit = model.getSelectedUnit();
		if (unit == null) {
			return null;
		}
		final StringBuilder buffer = new StringBuilder();
		final @Nullable IFortress home = containingFortress(unit);
		for (final String kind : unit.stream()
				.filter(Animal.class::isInstance).map(Animal.class::cast)
				.filter(animal -> "domesticated".equals(animal.getStatus()) ||
					"tame".equals(animal.getStatus()))
				.map(Animal::getKind).distinct()
				.filter(k -> !herdModels.containsKey(k)).toList()) {
			final HerdModel herdModel = chooseHerdModel(kind);
			if (herdModel == null) {
				final Boolean cont = cli.inputBoolean("Skip?");
				if (cont != null && cont) {
					continue;
				} else {
					cli.println("Aborting ...");
					return null;
				}
			}
			herdModels.put(kind, herdModel);
		}
		// TODO: Use a multimap
		final Map<HerdModel, List<Animal>> modelMap = new HashMap<>();
		for (final Animal group : unit.stream()
				.filter(Animal.class::isInstance).map(Animal.class::cast)
				.filter(a -> "tame".equals(a.getStatus()) ||
						"domesticated".equals(a.getStatus())).toList()) {
			if (herdModels.containsKey(group.getKind())) {
				final HerdModel hModel = herdModels.get(group.getKind());
				final List<Animal> list;
				if (modelMap.containsKey(hModel)) {
					list = modelMap.get(hModel);
				} else {
					list = new ArrayList<>();
					modelMap.put(hModel, list);
				}
				list.add(group);
				continue;
			}
			final Boolean cont = cli.inputBoolean(String.format(
				"No model for %s. Really skip?", group.getKind()));
			if (cont != null && cont) {
				continue;
			} else {
				cli.println("Aborting ...");
				return null;
			}
		}
		long workerCount = unit.stream().filter(IWorker.class::isInstance).map(IWorker.class::cast).count();
		final Integer addendum = cli.inputNumber(String.format(
			"%d workers in this unit. Any additional workers to account for:", workerCount));
		if (addendum != null && addendum >= 0) {
			workerCount += addendum;
		} else {
			return null;
		}
		final boolean experts = unit.stream()
			.filter(IWorker.class::isInstance).map(IWorker.class::cast)
			.mapToInt(w -> w.getJob("herder").getLevel()).anyMatch(l -> l > 5);
		int minutesSpent = 0;
		final Consumer<String> addToOrders = string -> {
			cli.print(string);
			buffer.append(string);
		};
		final Consumer<String> addLineToOrders = string -> {
			cli.println(string);
			buffer.append(string);
			buffer.append(System.lineSeparator());
		};
		for (final Map.Entry<HerdModel, List<Animal>> entry : modelMap.entrySet()) {
			if (!buffer.isEmpty()) {
				buffer.append(System.lineSeparator());
				buffer.append(System.lineSeparator());
			}
			final HerdModel herdModel = entry.getKey();
			final List<Animal> animals = entry.getValue();
			final Animal combinedAnimal = animals.stream().reduce(Animal::combined).get();
			final long flockPerHerder =
				(combinedAnimal.getPopulation() + workerCount - 1) / workerCount;
			final Quantity production = herdModel.scaledProduction(combinedAnimal.getPopulation());
			final double pounds = herdModel.scaledPoundsProduction(combinedAnimal.getPopulation());
			final String resourceProduced;
			if (herdModel instanceof final PoultryModel pm) {
				resourceProduced = combinedAnimal.getKind() + " eggs";
				final Boolean cleaningDay = cli.inputBoolean(String.format(
					"Is this the one turn in every %d to clean up after birds?",
					pm.getExtraChoresInterval() + 1));
				addLineToOrders.accept(String.format("Gathering %s eggs took the %d workers %d min",
					combinedAnimal, workerCount, pm.dailyTime((int) flockPerHerder)));
				minutesSpent += pm.getDailyTimePerHead() * flockPerHerder;
				if (cleaningDay == null) {
					return null;
				} else if (cleaningDay) {
					addLineToOrders.accept(String.format(
						"Cleaning up after them takes %.1f hours.",
						PoultryModel.dailyExtraTime((int) flockPerHerder) / 60.0));
					minutesSpent += PoultryModel.getExtraTimePerHead() * flockPerHerder;
				}
			} else if (herdModel instanceof MammalModel) {
				resourceProduced = "milk";
				addToOrders.accept("Between two milkings, tending the ");
				addToOrders.accept(AnimalPlurals.get(combinedAnimal.getKind()));
				final long baseCost;
				if (experts) {
					baseCost = flockPerHerder * (herdModel.getDailyTimePerHead() - 10); // TODO: That's a sub-optimal formula
				} else {
					baseCost = flockPerHerder * herdModel.getDailyTimePerHead();
				}
				addLineToOrders.accept(String.format(" took %d min, plus %s min to gather them",
					baseCost, MammalModel.getDailyTimeFloor()));
				minutesSpent += baseCost;
				minutesSpent += MammalModel.getDailyTimeFloor();
			} else if (herdModel instanceof SmallAnimalModel smm) {
				addToOrders.accept("Tending the ");
				addToOrders.accept(AnimalPlurals.get(combinedAnimal.getKind()));
				final long baseCost;
				if (experts) {
					baseCost = (int) ((flockPerHerder * herdModel.getDailyTimePerHead() +
						SmallAnimalModel.getDailyTimeFloor()) * 0.9);
				} else {
					baseCost = flockPerHerder * herdModel.getDailyTimePerHead() +
						SmallAnimalModel.getDailyTimeFloor();
				}
				minutesSpent += baseCost;
				addLineToOrders.accept(String.format(" took the %d workers %d min.", workerCount, baseCost));
				final Boolean extra = cli.inputBoolean(String.format(
					"Is this the one turn in every %d to clean up after the animals?",
					smm.getExtraChoresInterval() + 1));
				if (extra == null) {
					return null;
				} else {
					addLineToOrders.accept(String.format("Cleaning up after them took %d minutes.",
						SmallAnimalModel.getExtraTimePerHead() * flockPerHerder));
					minutesSpent += SmallAnimalModel.getExtraTimePerHead() * flockPerHerder;
				}
				continue;
			} else {
				LovelaceLogger.error("Unhandled animal type");
				return null;
			}
			addLineToOrders.accept(String.format("This produced %.1f %s, %.1f lbs, of %s.",
				production.number().doubleValue(), production.units(), pounds, resourceProduced));
			if (home != null) {
				// FIXME: 'production' is in gallons; we want only pound-denominated food resources in the map
				// TODO: If 'home' is null, should probably add to the unit itself ...
				model.addResource(home, idf.createID(), "food", resourceProduced, production,
					model.getMap().getCurrentTurn());
			}
		}
		addToOrders.accept(String.format("In all, tending the animals took %s.", inHours(minutesSpent)));
		return buffer.toString().strip();
	}
}
