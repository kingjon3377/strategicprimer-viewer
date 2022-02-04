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
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import query.HerdModel;
import query.MammalModel;
import query.PoultryModel;
import query.SmallAnimalModel;

/* package */ class HerdApplet extends AbstractTurnApplet {
	private static final Logger LOGGER = Logger.getLogger(HerdApplet.class.getName());
	public HerdApplet(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) {
		super(model, cli, idf);
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
	@Nullable
	private IFortress containingFortress(IUnit unit) {
		return model.getMap().getFixtures(model.find(unit)).stream().filter(IFortress.class::isInstance)
			.map(IFortress.class::cast).filter(f -> f.getOwner().equals(unit.getOwner()))
			.findAny().orElse(null);
	}

	private final Map<String, HerdModel> herdModels = new HashMap<>();

	@Nullable
	private HerdModel chooseHerdModel(String animal) {
		return cli.chooseFromList(Stream.concat(Stream.of(MammalModel.values()), Stream.concat(
				Stream.of(PoultryModel.values()), Stream.of(SmallAnimalModel.values())))
				.collect(Collectors.toList()),
			String.format("What kind of animal(s) is/are %s?", animal),
			"No animal kinds found", "Kind of animal:", false).getValue1();
	}

	@Override
	@Nullable
	public String run() {
		IUnit unit = model.getSelectedUnit();
		StringBuilder buffer = new StringBuilder();
		@Nullable IFortress home = containingFortress(unit);
		for (String kind : unit.stream()
				.filter(Animal.class::isInstance).map(Animal.class::cast)
				.filter(animal -> "domesticated".equals(animal.getStatus()) ||
					"tame".equals(animal.getStatus()))
				.map(Animal::getKind).distinct()
				.filter(k -> !herdModels.keySet().contains(k))
				.collect(Collectors.toList())) {
			HerdModel herdModel = chooseHerdModel(kind);
			if (herdModel == null) {
				Boolean cont = cli.inputBoolean("Skip?");
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
		for (Animal group : unit.stream()
				.filter(Animal.class::isInstance).map(Animal.class::cast)
				.filter(a -> "tame".equals(a.getStatus()) ||
						"domesticated".equals(a.getStatus()))
				.collect(Collectors.toList())) {
			if (herdModels.containsKey(group.getKind())) {
				HerdModel hModel = herdModels.get(group.getKind());
				List<Animal> list;
				if (modelMap.containsKey(hModel)) {
					list = modelMap.get(hModel);
				} else {
					list = new ArrayList<>();
					modelMap.put(hModel, list);
				}
				list.add(group);
				continue;
			}
			Boolean cont = cli.inputBoolean(String.format(
				"No model for %s. Really skip?", group.getKind()));
			if (cont != null && cont) {
				continue;
			} else {
				cli.println("Aborting ...");
				return null;
			}
		}
		long workerCount = unit.stream().filter(IWorker.class::isInstance).map(IWorker.class::cast).count();
		Integer addendum = cli.inputNumber(String.format(
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
		Consumer<String> addToOrders = string -> {
			cli.print(string);
			buffer.append(string);
		};
		Consumer<String> addLineToOrders = string -> {
			cli.println(string);
			buffer.append(string);
			buffer.append(System.lineSeparator());
		};
		for (Map.Entry<HerdModel, List<Animal>> entry : modelMap.entrySet()) {
			if (buffer.length() > 0) {
				buffer.append(System.lineSeparator());
				buffer.append(System.lineSeparator());
			}
			HerdModel herdModel = entry.getKey();
			List<Animal> animals = entry.getValue();
			Animal combinedAnimal = animals.stream().reduce(Animal::combined).get();
			long flockPerHerder =
				(combinedAnimal.getPopulation() + workerCount - 1) / workerCount;
			Quantity production = herdModel.scaledProduction(combinedAnimal.getPopulation());
			double pounds = herdModel.scaledPoundsProduction(combinedAnimal.getPopulation());
			String resourceProduced;
			if (herdModel instanceof PoultryModel) {
				PoultryModel pm = (PoultryModel) herdModel;
				resourceProduced = combinedAnimal.getKind() + " eggs";
				Boolean cleaningDay = cli.inputBoolean(String.format(
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
						pm.dailyExtraTime((int) flockPerHerder) / 60.0));
					minutesSpent += ((PoultryModel) herdModel).getExtraTimePerHead() * flockPerHerder;
				}
			} else if (herdModel instanceof MammalModel) {
				resourceProduced = "milk";
				addToOrders.accept("Between two milkings, tending the ");
				addToOrders.accept(AnimalPlurals.get(combinedAnimal.getKind()));
				long baseCost;
				if (experts) {
					baseCost = flockPerHerder * (herdModel.getDailyTimePerHead() - 10); // TODO: That's a sub-optimal formula
				} else {
					baseCost = flockPerHerder * herdModel.getDailyTimePerHead();
				}
				addLineToOrders.accept(String.format(" took %d min, plus %s min to gather them",
					baseCost, ((MammalModel) herdModel).getDailyTimeFloor()));
				minutesSpent += baseCost;
				minutesSpent += ((MammalModel) herdModel).getDailyTimeFloor();
			} else if (herdModel instanceof SmallAnimalModel) {
				addToOrders.accept("Tending the ");
				addToOrders.accept(AnimalPlurals.get(combinedAnimal.getKind()));
				long baseCost;
				if (experts) {
					baseCost = (int) ((flockPerHerder * herdModel.getDailyTimePerHead() +
						((SmallAnimalModel) herdModel).getDailyTimeFloor()) * 0.9);
				} else {
					baseCost = flockPerHerder * herdModel.getDailyTimePerHead() +
						((SmallAnimalModel) herdModel).getDailyTimeFloor();
				}
				minutesSpent += baseCost;
				addLineToOrders.accept(String.format(" took the %d workers %d min.", workerCount, baseCost));
				Boolean extra = cli.inputBoolean(String.format(
					"Is this the one turn in every %d to clean up after the animals?",
					((SmallAnimalModel) herdModel).getExtraChoresInterval() + 1));
				if (extra == null) {
					return null;
				} else {
					addLineToOrders.accept(String.format("Cleaning up after them took %d minutes.",
						((SmallAnimalModel) herdModel).getExtraTimePerHead() * flockPerHerder));
					minutesSpent += ((SmallAnimalModel) herdModel).getExtraTimePerHead() * flockPerHerder;
				}
				continue;
			} else {
				LOGGER.severe("Unhandled animal type");
				return null;
			}
			addLineToOrders.accept(String.format("This produced %.1f %s, %.1f lbs, of %s.",
				production.getNumber().doubleValue(), pounds, resourceProduced));
			if (home != null) {
				// FIXME: 'production' is in gallons; we want only pound-denominated food resources in the map
				// TODO: If 'home' is null, should probably add to the unit itself ...
				model.addResource(home, idf.createID(), "food", resourceProduced, production,
					model.getMap().getCurrentTurn());
			}
		}
		addToOrders.accept(String.format("In all, tending the animals took %s.", inHours(minutesSpent)));
		return buffer.toString().trim();
	}
}
