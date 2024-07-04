package drivers.turnrunning.applets;

import legacy.idreg.IDRegistrar;
import legacy.map.fixtures.LegacyQuantity;
import legacy.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.AnimalPlurals;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.IWorker;
import legacy.map.fixtures.towns.IFortress;
import drivers.common.cli.ICLIHelper;
import drivers.turnrunning.ITurnRunningModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
                        .collect(Collectors.toList()), "What kind of animal(s) is/are %s?".formatted(animal),
                "No animal kinds found", "Kind of animal:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
	}

	@Override
	public @Nullable String run() {
		final IUnit unit = model.getSelectedUnit();
		if (Objects.isNull(unit)) {
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
			if (Objects.isNull(herdModel)) {
				final Boolean cont = cli.inputBoolean("Skip?"); // TODO: Inline
				if (Boolean.TRUE.equals(cont)) {
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
			final Boolean cont = cli.inputBoolean("No model for %s. Really skip?".formatted(group.getKind()));
			if (Boolean.TRUE.equals(cont)) {
				continue;
			} else {
				cli.println("Aborting ...");
				return null;
			}
		}
		long workerCount = unit.stream().filter(IWorker.class::isInstance).map(IWorker.class::cast).count();
		final Integer addendum = cli.inputNumber("%d workers in this unit. Any additional workers to account for:".formatted(workerCount));
		if (!Objects.isNull(addendum) && addendum >= 0) {
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
			final LegacyQuantity production = herdModel.scaledProduction(combinedAnimal.getPopulation());
			final double pounds = herdModel.scaledPoundsProduction(combinedAnimal.getPopulation());
			final String resourceProduced;
			switch (herdModel) {
				case final PoultryModel pm -> {
					resourceProduced = combinedAnimal.getKind() + " eggs";
					final Boolean cleaningDay = cli.inputBoolean("Is this the one turn in every %d to clean up after birds?".formatted(pm.getExtraChoresInterval() + 1));
					addLineToOrders.accept("Gathering %s eggs took the %d workers %d min".formatted(
							combinedAnimal, workerCount, pm.dailyTime((int) flockPerHerder)));
					minutesSpent += pm.getDailyTimePerHead() * flockPerHerder;
					if (Objects.isNull(cleaningDay)) {
						return null;
					} else if (cleaningDay) {
						addLineToOrders.accept("Cleaning up after them takes %.1f hours.".formatted(PoultryModel.dailyExtraTime((int) flockPerHerder) / 60.0));
						minutesSpent += PoultryModel.getExtraTimePerHead() * flockPerHerder;
					}
				}
				case MammalModel mammalModel -> {
					resourceProduced = "milk";
					addToOrders.accept("Between two milkings, tending the ");
					addToOrders.accept(AnimalPlurals.get(combinedAnimal.getKind()));
					final long baseCost;
					if (experts) {
						baseCost = flockPerHerder * (herdModel.getDailyTimePerHead() - 10); // TODO: That's a sub-optimal formula
					} else {
						baseCost = flockPerHerder * herdModel.getDailyTimePerHead();
					}
					addLineToOrders.accept(" took %d min, plus %s min to gather them".formatted(
							baseCost, MammalModel.getDailyTimeFloor()));
					minutesSpent += baseCost;
					minutesSpent += MammalModel.getDailyTimeFloor();
				}
				case final SmallAnimalModel smm -> {
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
					addLineToOrders.accept(" took the %d workers %d min.".formatted(workerCount, baseCost));
					final Boolean extra = cli.inputBoolean("Is this the one turn in every %d to clean up after the animals?".formatted(smm.getExtraChoresInterval() + 1));
					if (Objects.isNull(extra)) {
						return null;
					} else {
						addLineToOrders.accept("Cleaning up after them took %d minutes.".formatted(SmallAnimalModel.getExtraTimePerHead() * flockPerHerder));
						minutesSpent += SmallAnimalModel.getExtraTimePerHead() * flockPerHerder;
					}
					continue;
				}
				default -> {
					LovelaceLogger.error("Unhandled animal type");
					return null;
				}
			}
			addLineToOrders.accept("This produced %.1f %s, %.1f lbs, of %s.".formatted(
					production.number().doubleValue(), production.units(), pounds, resourceProduced));
			if (!Objects.isNull(home)) {
				// FIXME: 'production' is in gallons; we want only pound-denominated food resources in the map
				// TODO: If 'home' is null, should probably add to the unit itself ...
				model.addResource(home, idf.createID(), "food", resourceProduced, production,
						model.getMap().getCurrentTurn());
			}
		}
		addToOrders.accept("In all, tending the animals took %s.".formatted(inHours(minutesSpent)));
		return buffer.toString().strip();
	}
}
