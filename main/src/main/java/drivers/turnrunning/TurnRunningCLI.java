package drivers.turnrunning;

import common.idreg.IDFactoryFiller;
import common.idreg.IDRegistrar;
import common.map.IFixture;
import common.map.IMapNG;
import common.map.Player;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.ProxyUnit;
import common.map.fixtures.towns.IFortress;
import drivers.advancement.AdvancementCLIHelper;
import drivers.advancement.LevelGainListener;
import drivers.common.CLIDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;
import drivers.common.cli.AppletChooser;
import drivers.common.cli.ICLIHelper;
import drivers.turnrunning.applets.ConsumptionApplet;
import drivers.turnrunning.applets.SpoilageApplet;
import drivers.turnrunning.applets.TurnApplet;
import drivers.turnrunning.applets.TurnAppletFactory;
import either.Either;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.javatuples.Pair;

// TODO: Tests
/* package */ class TurnRunningCLI implements CLIDriver {
	public TurnRunningCLI(ICLIHelper cli, ITurnRunningModel model) {
		this.cli = cli;
		this.model = model;
		idf = new IDFactoryFiller().createIDFactory(StreamSupport.stream(model.getAllMaps().spliterator(), false)
				.toArray(IMapNG[]::new));
		advancementCLI = new AdvancementCLIHelper(model, cli);
		appletChooser = new AppletChooser<TurnApplet>(cli,
			StreamSupport.stream(ServiceLoader.load(TurnAppletFactory.class).spliterator(), false)
				.map(factory -> factory.create(model, cli, idf)).toArray(TurnApplet[]::new));
		consumptionApplet = new ConsumptionApplet(model, cli, idf);
		spoilageApplet = new SpoilageApplet(model, cli, idf);
	}

	private final ICLIHelper cli;

	private final ITurnRunningModel model;

	@Override
	public ITurnRunningModel getModel() {
		return model;
	}

	// TODO: Support some standard options
	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	private final IDRegistrar idf;

	private Predicate<IUnit> unfinishedResults(int turn) {
		return unit -> {
			String results = unit.getResults(turn).toLowerCase();
			return results.isEmpty() || results.contains("fixme") || results.contains("todo") ||
				results.contains("xxx");
		};
	}

	/**
	 * If {@link fixture the argument} is a {@link IFortress fortress},
	 * return a stream of its contents; otherwise, return a stream
	 * containing only the argument. This allows callers to get a flattened
	 * stream of units, including those in fortresses.
	 */
	private static Stream<IFixture> flatten(IFixture fixture) {
		if (fixture instanceof IFortress) {
			return StreamSupport.stream(((IFortress) fixture).spliterator(), false).map(IFixture.class::cast);
		} else {
			return Stream.of(fixture);
		}
	}

	/**
	 * Flatten and filter the stream to include only units, and only those owned by the given player.
	 */
	private static Stream<IUnit> getUnitsImpl(Stream<? extends IFixture> s, Player player) {
		return s.flatMap(TurnRunningCLI::flatten).filter(IUnit.class::isInstance).map(IUnit.class::cast)
			.filter(u -> player.equals(u.getOwner()));
	}

	private Stream<IUnit> getUnits(Player player) {
		final List<IUnit> temp = StreamSupport.stream(model.getAllMaps().spliterator(), false)
			.flatMap(indivMap -> getUnitsImpl(StreamSupport.stream(indivMap.getLocations().spliterator(), false)
				.flatMap(l -> indivMap.getFixtures(l).stream()), player)).collect(Collectors.toList());
		final Map<Integer, ProxyUnit> tempMap = new TreeMap<>();
		for (IUnit unit : temp) {
			int key = unit.getId();
			final ProxyUnit proxy;
			if (tempMap.containsKey(key)) {
				proxy = tempMap.get(key);
			} else {
				final ProxyUnit newProxy = new ProxyUnit(key);
				tempMap.put(key, newProxy);
				proxy = newProxy;
			}
			proxy.addProxied(unit);
		}
		return tempMap.values().stream().map(IUnit.class::cast)
				.sorted(Comparator.comparing(IUnit::getName, String.CASE_INSENSITIVE_ORDER));
	}

	private final AdvancementCLIHelper advancementCLI;
	private final AppletChooser<TurnApplet> appletChooser;

	private final ConsumptionApplet consumptionApplet;

	private final SpoilageApplet spoilageApplet;

	private String createResults(IUnit unit, int turn) {
		if (unit instanceof ProxyUnit) {
			model.setSelectedUnit(((ProxyUnit) unit).getProxied().iterator().next());
		} else {
			model.setSelectedUnit(unit);
		}
		cli.print("Orders for unit ", unit.getName(), " (", unit.getKind());
		cli.print(") for turn ", Integer.toString(turn), ": ");
		cli.println(unit.getLatestOrders(turn));
		StringBuilder buffer = new StringBuilder();
		while (true) {
			Either<TurnApplet, Boolean> command = appletChooser.chooseApplet();
			Boolean bool = command.fromRight().orElse(null);
			TurnApplet applet = command.fromLeft().orElse(null);
			if (bool != null && !bool) {
				return ""; // TODO: why not null? (making the method return type nullable) also below
			} else if (applet != null) {
				if (!applet.getCommands().contains("other")) {
					String results = applet.run();
					if (results == null) {
						return "";
					}
					buffer.append(results);
				}
				break;
			}
		}
		final String prompt;
		if (buffer.length() == 0) {
			prompt = "Results: ";
		} else {
			prompt = "Additional Results: ";
		}
		final String addendum = cli.inputMultilineString(prompt);
		if (addendum == null) {
			return "";
		}
		buffer.append(addendum);
		final Boolean runAdvancement = cli.inputBooleanInSeries("Run advancement for this unit now?");
		if (runAdvancement != null && runAdvancement) {
			Boolean expertMentoring = cli.inputBooleanInSeries("Account for expert mentoring?");
			if (expertMentoring != null) {
				buffer.append(System.lineSeparator());
				buffer.append(System.lineSeparator());
				LevelGainListener levelListener =
					(workerName, jobName, skillName, gains, currentLevel) -> {
						buffer.append(workerName);
						buffer.append(" showed improvement in the skill of ");
						buffer.append(skillName);
						if (gains > 1) {
							buffer.append(" (");
							buffer.append(Integer.toString(gains));
							buffer.append(" skill ranks)");
						}
						buffer.append(". ");
					};
				advancementCLI.addLevelGainListener(levelListener);
				advancementCLI.advanceWorkersInUnit(unit, expertMentoring);
				advancementCLI.removeLevelGainListener(levelListener);
			}
		}
		final Boolean runFoodConsumptionAnswer = cli.inputBooleanInSeries(
			"Run food consumption for this unit now?");
		if (runFoodConsumptionAnswer != null && runFoodConsumptionAnswer) {
			consumptionApplet.setTurn(turn);
			consumptionApplet.setUnit(unit);
			String consumptionResults = consumptionApplet.run();
			if (consumptionResults == null) {
				return "";
			}
			if (!consumptionResults.isEmpty()) {
				buffer.append(System.lineSeparator());
				buffer.append(System.lineSeparator());
				buffer.append(consumptionResults);
				buffer.append(System.lineSeparator());
			}
		}
		final Boolean runFoodSpoilageAnswer = cli.inputBooleanInSeries(
			"Run food spoilage and report it under this unit's results?");
		if (runFoodSpoilageAnswer != null && runFoodSpoilageAnswer) {
			spoilageApplet.setOwner(unit.getOwner());
			spoilageApplet.setTurn(turn);
			String foodSpoilageResult = spoilageApplet.run();
			if (foodSpoilageResult != null) {
				buffer.append(System.lineSeparator());
				buffer.append(System.lineSeparator());
				buffer.append(foodSpoilageResult);
				buffer.append(System.lineSeparator());
			}
		}
		return buffer.toString().trim();
	}

	@Override
	public void startDriver() {
		int currentTurn = model.getMap().getCurrentTurn();
		Player player = cli.chooseFromList(StreamSupport.stream(model.getPlayerChoices().spliterator(), false)
				.collect(Collectors.toList()), "Players in the maps:", "No players found",
			"Player to run:", false).getValue1();
		if (player == null) {
			return;
		}
		List<IUnit> units = new ArrayList<>(
			getUnits(player).filter(unfinishedResults(currentTurn)).collect(Collectors.toList()));
		while (true) {
			Pair<Integer, IUnit> pair = cli.chooseFromList(units,
				String.format("Units belonging to %s:", player),
				"Player has no units without apparently-final results", "Unit to run:", false);
			int index = pair.getValue0();
			IUnit unit = pair.getValue1();
			if (unit == null) {
				break;
			}
			String results = createResults(unit, currentTurn);
			model.setUnitResults(unit, currentTurn, results);
			if (!unfinishedResults(currentTurn).test(unit)) {
				units.remove(index);
			}
			if (units.isEmpty()) {
				break;
			}
		}
	}
}
