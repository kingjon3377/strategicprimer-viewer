package utility;

import common.map.fixtures.FixtureIterable;
import java.util.Objects;
import org.javatuples.Pair;
import java.util.Comparator;
import drivers.common.IDriverModel;
import drivers.common.ReadOnlyDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import drivers.common.cli.ICLIHelper;

import common.map.IMapNG;
import common.map.TileType;
import common.map.River;
import common.map.TileFixture;

import lovelace.util.IntAccumulator;
import lovelace.util.DecimalAccumulator;
import lovelace.util.EnumCounter;

import java.math.BigDecimal;

import common.map.fixtures.terrain.Forest;
import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Oasis;

import java.util.function.Function;
import java.util.function.Predicate;

import java.util.List;
import java.util.Collection;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

import common.map.fixtures.Ground;

import common.map.fixtures.resources.StoneDeposit;
import common.map.fixtures.resources.MineralVein;
import common.map.fixtures.resources.Mine;
import common.map.fixtures.resources.CacheFixture;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.Grove;
import common.map.fixtures.resources.Shrub;

import common.map.fixtures.explorable.AdventureFixture;
import common.map.fixtures.explorable.Portal;
import common.map.fixtures.explorable.Battlefield;
import common.map.fixtures.explorable.Cave;

import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.AbstractTown;
import common.map.fixtures.towns.TownStatus;
import common.map.fixtures.towns.Village;

import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.Immortal;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.AnimalTracks;

/**
 * An app to report statistics on the contents of the map.
 */
/* package */ class CountingCLI implements ReadOnlyDriver {
	public CountingCLI(final ICLIHelper cli, final IDriverModel model) {
		this.cli = cli;
		this.model = model;
	}

	private final ICLIHelper cli;

	private final IDriverModel model;

	@Override
	public IDriverModel getModel() {
		return model;
	}

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	private static <Key, Count extends Number> String
			parameterizedCountSpaceKey(final Pair<Key, Count> entry) {
		return String.format("- %s %s", entry.getValue1(), entry.getValue0());
	}

	private <Base, Key, Count extends Number&Comparable<Count>> void printSummary(
			final MappedCounter<Base, Key, Count> counter, final String total) {
		printSummary(counter, total, CountingCLI::parameterizedCountSpaceKey);
	}

	private <Base, Key, Count extends Number&Comparable<Count>> void printSummary(
			final MappedCounter<Base, Key, Count> counter, final Function<Count, String> total) {
		printSummary(counter, total, CountingCLI::parameterizedCountSpaceKey);
	}

	private <Base, Key, Count extends Number&Comparable<Count>> void printSummary(
			final MappedCounter<Base, Key, Count> counter, final String total,
			final Function<Pair<Key, Count>, String> each) {
		if (counter.getTotal().doubleValue() > 0.0) {
			cli.println(total);
			cli.println();
			counter.stream().map(each).forEach(cli::println);
			cli.println();
		}
	}

	private <Base, Key, Count extends Number&Comparable<Count>> void printSummary(
			final MappedCounter<Base, Key, Count> counter, final Function<Count, String> total,
			final Function<Pair<Key, Count>, String> each) {
		if (counter.getTotal().doubleValue() > 0.0) {
			cli.println(total.apply(counter.getTotal()));
			cli.println();
			counter.stream().map(each).forEach(cli::println);
			cli.println();
		}
	}

	private <Type> MappedCounter<Type, String, Integer>
			simpleCounter(final Function<Type, String> keyExtractor) {
		return new MappedCounter<>(keyExtractor, t -> 1, IntAccumulator::new, 0);
	}

	private <Type> void countSimply(final Class<Type> cls, final Collection<?> stream, final String title,
	                                final Function<Type, String> extractor) {
		MappedCounter<Type, String, Integer> counter = simpleCounter(extractor);
		stream.stream().filter(cls::isInstance).map(cls::cast).forEach(counter::add);
		printSummary(counter, title);
	}

	private static <Type> Predicate<Object> exclude(final Class<Type> cls) {
		return obj -> !cls.isInstance(obj);
	}

	private static String reportForestTotal(final BigDecimal total) {
		return String.format("There are %s acres of forest, including:", total);
	}

	private static boolean hasLake(final Collection<River> iter) {
		return iter.contains(River.Lake);
	}

	private static boolean withNonLake(final Collection<River> iter) {
		return iter.stream().anyMatch(r -> River.Lake != r);
	}

	private static String countOfKind(final Pair<String, ? extends Number> pair) {
		String key = pair.getValue0();
		Number item = pair.getValue1();
		return String.format("- %s of %s", item, key);
	}

	private static String countTilesWithKind(final Pair<String, Integer> entry) {
		return String.format("- %s tiles with %s", entry.getValue1(), entry.getValue0());
	}

	private static String kindColonCount(final Pair<String, Integer> entry) {
		return String.format("- %s: %s", entry.getValue0(), entry.getValue1());
	}

	private static String countSpaceKind(final Pair<String, Integer> entry) {
		return String.format("  - %s %s", entry.getValue1(), entry.getValue0());
	}

	private static String townSummary(final AbstractTown t) {
		return String.format("%s %s %s", t.getStatus(), t.getTownSize(), t.getKind());
	}

	private static <T> Predicate<T> negate(final Predicate<T> pred) {
		return t -> !pred.test(t);
	}

	private static Predicate<TileFixture> notA(final Class<? extends TileFixture> cls) {
		return negate(cls::isInstance);
	}

	private static BigDecimal decimalize(final Number number) {
		if (number instanceof Integer || number instanceof Long
				|| number instanceof Short || number instanceof Byte) {
			return BigDecimal.valueOf(number.longValue());
		} else if (number instanceof BigDecimal) {
			return (BigDecimal) number;
		} else {
			return BigDecimal.valueOf(number.doubleValue());
		}
	}

	@Override
	public void startDriver() { // TODO: Reduce duplication
		IMapNG map = model.getMap();
		cli.println(String.format("There are %d tiles in all.",
			map.getDimensions().getRows() * map.getDimensions().getColumns()));
		EnumCounter<TileType> tileTypeCounts = new EnumCounter<>();
		tileTypeCounts.countMany(map.streamLocations()
			.map(map::getBaseTerrain).filter(Objects::nonNull).toArray(TileType[]::new));
		cli.println();
		// TODO: Add streamAllCounts() to EnumCounter
		for (Pair<TileType, Integer> entry : StreamSupport.stream(
					tileTypeCounts.getAllCounts().spliterator(), false)
				.sorted(Comparator.comparing(Pair::getValue1,
					Comparator.reverseOrder())).collect(Collectors.toList())) {
			// TODO: Use Stream::forEach to avoid collector step
			cli.println(String.format("- %d are %s", entry.getValue1(), entry.getValue0()));
		}
		cli.println();
		List<TileFixture> allFixtures = map.streamAllFixtures().collect(Collectors.toList());
		MappedCounter<Forest, String, BigDecimal> forests = new MappedCounter<>(Forest::getKind,
			f -> decimalize(f.getAcres()), DecimalAccumulator::new, BigDecimal.ZERO);
		allFixtures.stream().filter(Forest.class::isInstance).map(Forest.class::cast)
			.forEach(forests::add);
		this.printSummary(forests,
			CountingCLI::reportForestTotal, CountingCLI::countOfKind);

		cli.println("Terrain fixtures:");
		cli.println();
		List<Collection<TileFixture>> separateTiles = map.streamLocations()
				.map(map::getFixtures).collect(Collectors.toList());
		cli.println(String.format("- %d hilly tiles",
			separateTiles.stream().filter(c -> c.stream().anyMatch(Hill.class::isInstance))
				.count()));
		cli.println(String.format("- %d mountainous tiles", map.streamLocations()
				.filter(map::isMountainous).count()));
		cli.println(String.format("- %d at least partly forested tiles",
			separateTiles.stream().filter(c -> c.stream().anyMatch(Forest.class::isInstance))
				.count()));
		cli.println(String.format("- %d oases", separateTiles.stream()
			.filter(c -> c.stream().anyMatch(Oasis.class::isInstance)).count()));
		List<Collection<River>> tilesRivers = map.streamLocations()
				.map(map::getRivers).collect(Collectors.toList());
		cli.println(String.format("- %d lakes",
			tilesRivers.stream().filter(CountingCLI::hasLake).count()));
		cli.println(String.format("- %d tiles with rivers", tilesRivers.stream()
			.filter(CountingCLI::withNonLake).count()));
		// TODO: Count tiles with roads of each type
		cli.println();

		MappedCounter<Ground, String, Integer> ground = simpleCounter(Ground::getKind);
		allFixtures.stream().filter(Ground.class::isInstance).map(Ground.class::cast)
			.forEach(ground::add);
		this.printSummary(ground,
			"Ground (bedrock) (counting exposed/not separately):",
			CountingCLI::countTilesWithKind);

		countSimply(StoneDeposit.class, allFixtures, "Stone deposits:", StoneDeposit::getKind);
		countSimply(MineralVein.class, allFixtures, "Mineral veins:", MineralVein::getKind);
		countSimply(Mine.class, allFixtures, "Mines:", Mine::getKind);

		MappedCounter<CacheFixture, String, Integer> caches = simpleCounter(CacheFixture::getKind);
		allFixtures.stream().filter(CacheFixture.class::isInstance).map(CacheFixture.class::cast)
			.forEach(caches::add);
		printSummary(caches, "Caches:", CountingCLI::countOfKind);

		MappedCounter<AdventureFixture, String, Integer> adventures =
			simpleCounter(AdventureFixture::getBriefDescription);
		allFixtures.stream().filter(AdventureFixture.class::isInstance)
			.map(AdventureFixture.class::cast).forEach(adventures::add);
		adventures.addDirectly("Portal to another world", (int) separateTiles.stream()
			.filter(c -> c.stream().anyMatch(Portal.class::isInstance)).count());
		adventures.addDirectly("Ancient battlefield", (int) allFixtures.stream()
			.filter(Battlefield.class::isInstance).count());
		adventures.addDirectly("Cave system", (int) allFixtures.stream()
			.filter(Cave.class::isInstance).count());
		printSummary(adventures, "Adventure Hooks and Portals:", CountingCLI::kindColonCount);

		cli.println("Active Communities:");
		cli.println();
		cli.println(String.format("- %d fortresses",
			allFixtures.stream().filter(IFortress.class::isInstance).count()));
		cli.println(String.format("- %d active towns, cities, or fortifications of any size",
			allFixtures.stream().filter(AbstractTown.class::isInstance)
				.map(AbstractTown.class::cast)
				.filter(t -> TownStatus.Active == t.getStatus()).count()));

		MappedCounter<Village, String, Integer> villages = simpleCounter(Village::getRace);
		allFixtures.stream().filter(Village.class::isInstance).map(Village.class::cast)
			.forEach(villages::add);
		printSummary(villages, "- Villages, grouped by race:", CountingCLI::countSpaceKind);

		MappedCounter<AbstractTown, String, Integer> inactiveTowns = simpleCounter(
			CountingCLI::townSummary);
		allFixtures.stream().filter(AbstractTown.class::isInstance).map(AbstractTown.class::cast)
			.filter(t -> TownStatus.Active != t.getStatus()).forEach(inactiveTowns::add);
		printSummary(inactiveTowns, "Inactive Communities:");

		MappedCounter<IUnit, String, Integer> independentUnits =
			simpleCounter(IUnit::getName);
		allFixtures.stream().filter(IUnit.class::isInstance).map(IUnit.class::cast)
			.filter(u -> u.getOwner().isIndependent()).forEach(independentUnits::add);
		printSummary(independentUnits, "Independent Units:");

		MappedCounter<IWorker, String, Integer> workers = simpleCounter(IWorker::getRace);
		allFixtures.stream().filter(IUnit.class::isInstance).map(IUnit.class::cast)
			.flatMap(FixtureIterable::stream)
			.filter(IWorker.class::isInstance).map(IWorker.class::cast).forEach(workers::add);
		allFixtures.stream().filter(IFortress.class::isInstance).map(IFortress.class::cast)
			.flatMap(FixtureIterable::stream)
			.filter(IUnit.class::isInstance).map(IUnit.class::cast)
			.flatMap(FixtureIterable::stream)
			.filter(IWorker.class::isInstance).map(IWorker.class::cast).forEach(workers::add);
		printSummary(workers, "Worker Races:");

		countSimply(Immortal.class, allFixtures, "Immortals:", Immortal::getShortDescription);
		countSimply(Meadow.class, allFixtures, "Fields and Meadows:", Meadow::getKind);
		countSimply(Grove.class, allFixtures, "Groves and Orchards:", Grove::getKind);
		countSimply(Shrub.class, allFixtures, "Shrubs:", Shrub::getKind);

		MappedCounter<Animal, String, Integer> animals =
			new MappedCounter<>(Animal::getKind, Animal::getPopulation, IntAccumulator::new, 0);
		allFixtures.stream().filter(Animal.class::isInstance).map(Animal.class::cast)
			.filter(a -> !a.isTalking()).forEach(animals::add);
		allFixtures.stream().filter(IUnit.class::isInstance).map(IUnit.class::cast)
			.flatMap(FixtureIterable::stream)
			.filter(Animal.class::isInstance).map(Animal.class::cast).forEach(animals::add);
		allFixtures.stream().filter(IFortress.class::isInstance).map(IFortress.class::cast)
			.flatMap(FixtureIterable::stream)
			.filter(IUnit.class::isInstance).map(IUnit.class::cast)
			.flatMap(FixtureIterable::stream)
			.filter(Animal.class::isInstance).map(Animal.class::cast).forEach(animals::add);
		animals.addDirectly("various talking animals", (int) allFixtures.stream()
			.filter(Animal.class::isInstance).map(Animal.class::cast).filter(Animal::isTalking)
			.count());
		printSummary(animals, "Animals");

		List<TileFixture> remaining = allFixtures.stream().filter(notA(Animal.class))
			.filter(notA(Shrub.class)).filter(notA(Grove.class)).filter(notA(Meadow.class))
			.filter(notA(Immortal.class)).filter(notA(IFortress.class)).filter(notA(IUnit.class))
			.filter(notA(AbstractTown.class)).filter(notA(Village.class))
			.filter(notA(Portal.class)).filter(notA(AdventureFixture.class))
			.filter(notA(CacheFixture.class)).filter(notA(Mine.class))
			.filter(notA(MineralVein.class)).filter(notA(StoneDeposit.class))
			.filter(notA(Ground.class)).filter(notA(Forest.class)).filter(notA(Hill.class))
			.filter(notA(Oasis.class)).filter(notA(AnimalTracks.class)).filter(notA(Cave.class))
			.filter(notA(Battlefield.class)).collect(Collectors.toList());

		if (!remaining.isEmpty()) {
			cli.println();
			cli.println("Remaining fixtures:");
			cli.println();
			for (TileFixture fixture : remaining) {
				cli.println(String.format("- %s", fixture.getShortDescription()));
			}
		}
	}
}
