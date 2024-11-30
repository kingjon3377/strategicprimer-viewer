package utility;

import legacy.map.IFixture;
import legacy.map.fixtures.FixtureIterable;
import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.Implement;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import legacy.map.fixtures.towns.ITownFixture;
import org.javatuples.Pair;

import java.util.Comparator;

import drivers.common.IDriverModel;
import drivers.common.ReadOnlyDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import drivers.common.cli.ICLIHelper;

import legacy.map.ILegacyMap;
import legacy.map.TileType;
import legacy.map.River;
import legacy.map.TileFixture;

import lovelace.util.IntAccumulator;
import lovelace.util.DecimalAccumulator;
import lovelace.util.EnumCounter;

import java.math.BigDecimal;

import legacy.map.fixtures.terrain.Forest;
import legacy.map.fixtures.terrain.Hill;
import legacy.map.fixtures.terrain.Oasis;

import java.util.function.Function;
import java.util.function.Predicate;

import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;

import legacy.map.fixtures.Ground;

import legacy.map.fixtures.resources.StoneDeposit;
import legacy.map.fixtures.resources.MineralVein;
import legacy.map.fixtures.resources.Mine;
import legacy.map.fixtures.resources.CacheFixture;
import legacy.map.fixtures.resources.Meadow;
import legacy.map.fixtures.resources.Grove;
import legacy.map.fixtures.resources.Shrub;

import legacy.map.fixtures.explorable.AdventureFixture;
import legacy.map.fixtures.explorable.Portal;
import legacy.map.fixtures.explorable.Battlefield;
import legacy.map.fixtures.explorable.Cave;

import legacy.map.fixtures.towns.IFortress;
import legacy.map.fixtures.towns.AbstractTown;
import common.map.fixtures.towns.TownStatus;
import legacy.map.fixtures.towns.Village;

import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.IWorker;
import legacy.map.fixtures.mobile.Immortal;
import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.AnimalTracks;

import static java.util.function.Predicate.not;

/**
 * An app to report statistics on the contents of the map.
 */
/* package */ final class CountingCLI implements ReadOnlyDriver {
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
		return "- %s %s".formatted(entry.getValue1(), entry.getValue0());
	}

	private <Base, Key, Count extends Number & Comparable<Count>> void printSummary(
			final MappedCounter<Base, Key, Count> counter, final String total) {
		printSummary(counter, total, CountingCLI::parameterizedCountSpaceKey, 0);
	}

	private <Base, Key, Count extends Number & Comparable<Count>> void printSummary(
			final MappedCounter<Base, Key, Count> counter, final Function<Count, String> total) {
		printSummary(counter, total, CountingCLI::parameterizedCountSpaceKey);
	}

	private <Base, Key, Count extends Number & Comparable<Count>> void printSummary(
			final MappedCounter<Base, Key, Count> counter, final String total,
			final int indent) {
		printSummary(counter, total, CountingCLI::parameterizedCountSpaceKey, indent);
	}

	private <Base, Key, Count extends Number & Comparable<Count>> void printSummary(
			final MappedCounter<Base, Key, Count> counter, final String total,
			final Function<Pair<Key, Count>, String> each, final int indent) {
		if (counter.getTotal().doubleValue() > 0.0) {
			if (indent > 0) {
				cli.print("  ".repeat(indent - 1));
				cli.print("- ");
			}
			cli.println(total);
			// TODO: use stream().map(each) ?
			for (final Pair<Key, Count> pair : counter) {
				cli.print("  ".repeat(indent));
				cli.println(each.apply(pair));
			}
			if (indent <= 0) {
				cli.println();
			}
		}
	}

	private <Base, Key, Count extends Number & Comparable<Count>> void printSummary(
			final MappedCounter<Base, Key, Count> counter, final Function<Count, String> total,
			final Function<Pair<Key, Count>, String> each) {
		if (counter.getTotal().doubleValue() > 0.0) {
			cli.println(total.apply(counter.getTotal()));
			cli.println();
			counter.stream().map(each).forEach(cli::println);
			cli.println();
		}
	}

	private static <Type> MappedCounter<Type, String, Integer>
	simpleCounter(final Function<Type, String> keyExtractor) {
		return new MappedCounter<>(keyExtractor, t -> 1, IntAccumulator::new, 0);
	}

	private <Type> void countSimply(final Class<Type> cls, final Collection<?> stream, final String title,
	                                final Function<Type, String> extractor) {
		final MappedCounter<Type, String, Integer> counter = simpleCounter(extractor);
		stream.stream().filter(cls::isInstance).map(cls::cast).forEach(counter::add);
		printSummary(counter, title);
	}

	private static <Type> Predicate<Object> exclude(final Class<Type> cls) {
		return obj -> !cls.isInstance(obj);
	}

	private static String reportForestTotal(final BigDecimal total) {
		return "There are %s acres of forest, including:".formatted(total);
	}

	private static boolean hasLake(final Collection<River> iter) {
		return iter.contains(River.Lake);
	}

	private static boolean withNonLake(final Collection<River> iter) {
		return iter.stream().anyMatch(r -> River.Lake != r);
	}

	private static String countOfKind(final Pair<String, ? extends Number> pair) {
		final String key = pair.getValue0();
		final Number item = pair.getValue1();
		return "- %s of %s".formatted(item, key);
	}

	private static String countTilesWithKind(final Pair<String, Integer> entry) {
		return "- %s tiles with %s".formatted(entry.getValue1(), entry.getValue0());
	}

	private static String kindColonCount(final Pair<String, Integer> entry) {
		return "- %s: %s".formatted(entry.getValue0(), entry.getValue1());
	}

	private static String countSpaceKind(final Pair<String, Integer> entry) {
		return "  - %s %s".formatted(entry.getValue1(), entry.getValue0());
	}

	private static String townSummary(final ITownFixture t) {
		return "%s %s %s".formatted(t.getStatus(), t.getTownSize(), t.getKind());
	}

	private static Predicate<IFixture> notA(final Class<? extends IFixture> cls) {
		return not(cls::isInstance);
	}

	@SuppressWarnings("ChainOfInstanceofChecks")
	private static BigDecimal decimalize(final Number number) {
		if (number instanceof Integer || number instanceof Long
				|| number instanceof Short || number instanceof Byte) {
			return BigDecimal.valueOf(number.longValue());
		} else if (number instanceof final BigDecimal d) {
			return d;
		} else {
			return BigDecimal.valueOf(number.doubleValue());
		}
	}

	private static Stream<?> flatten(final Object item) {
		// Note that workers are counted separately; while we include their equipment and mounts we don't include them.
		return switch (item) {
			case final IWorker w -> Stream.concat(Stream.concat(StreamSupport.stream(w.spliterator(), false),
							w.getEquipment().stream()), Optional.ofNullable(w.getMount()).stream())
					.filter(Objects::nonNull);
			case final Iterable<?> iterable ->  // TODO: FixtureIterable?
					Stream.concat(StreamSupport.stream(iterable.spliterator(), false)
							.flatMap(CountingCLI::flatten), Stream.of(item));
			default -> Stream.of(item);

		};
	}

	private static String resourcePileKeyExtractor(final IResourcePile pile) {
		if (pile.getContents().contains(pile.getQuantity().units()) ||
				pile.getQuantity().units().contains(pile.getContents())) {
			return pile.getContents();
		} else {
			return pile.getQuantity().units() + " " + pile.getContents();
		}
	}

	@Override
	public void startDriver() { // TODO: Reduce duplication
		final ILegacyMap map = model.getMap();
		cli.printf("There are %d tiles in all.%n", map.getDimensions().rows() * map.getDimensions().columns());
		final EnumCounter<TileType> tileTypeCounts = new EnumCounter<>();
		tileTypeCounts.countMany(map.streamLocations()
				.map(map::getBaseTerrain).filter(Objects::nonNull).toArray(TileType[]::new));
		cli.println();
		tileTypeCounts.streamAllCounts()
				.sorted(Comparator.comparing(Pair::getValue1,
						Comparator.reverseOrder()))
				.map(entry -> "- %d are %s".formatted(entry.getValue1(), entry.getValue0()))
				.forEach(cli::println);
		cli.println();
		final List<IFixture> allFixtures = map.streamAllFixtures().flatMap(CountingCLI::flatten)
				.filter(IFixture.class::isInstance).map(IFixture.class::cast).collect(Collectors.toList());
		final MappedCounter<Forest, String, BigDecimal> forests = new MappedCounter<>(Forest::getKind,
				f -> decimalize(f.getAcres()), DecimalAccumulator::new, BigDecimal.ZERO);
		allFixtures.stream().filter(Forest.class::isInstance).map(Forest.class::cast)
				.forEach(forests::add);
		printSummary(forests,
				CountingCLI::reportForestTotal, CountingCLI::countOfKind);

		cli.println("Terrain fixtures:");
		cli.println();
		final List<Collection<TileFixture>> separateTiles = map.streamLocations().map(map::getFixtures).toList();
		cli.printf("- %d hilly tiles%n", separateTiles.stream().filter(c -> c.stream().anyMatch(Hill.class::isInstance))
				.count());
		cli.printf("- %d mountainous tiles%n", map.streamLocations().filter(map::isMountainous).count());
		cli.printf("- %d at least partly forested tiles%n",
				separateTiles.stream().filter(c -> c.stream().anyMatch(Forest.class::isInstance)).count());
		cli.printf("- %d oases%n", separateTiles.stream()
				.filter(c -> c.stream().anyMatch(Oasis.class::isInstance)).count());
		final List<Collection<River>> tilesRivers = map.streamLocations().map(map::getRivers).toList();
		cli.printf("- %d lakes%n", tilesRivers.stream().filter(CountingCLI::hasLake).count());
		cli.printf("- %d tiles with rivers%n", tilesRivers.stream()
				.filter(CountingCLI::withNonLake).count());
		// TODO: Count tiles with roads of each type
		cli.println();

		final MappedCounter<Ground, String, Integer> ground = simpleCounter(Ground::getKind);
		allFixtures.stream().filter(Ground.class::isInstance).map(Ground.class::cast)
				.forEach(ground::add);
		printSummary(ground,
				"Ground (bedrock) (counting exposed/not separately):",
				CountingCLI::countTilesWithKind, 0);

		countSimply(StoneDeposit.class, allFixtures, "Stone deposits:", StoneDeposit::getKind);
		countSimply(MineralVein.class, allFixtures, "Mineral veins:", MineralVein::getKind);
		countSimply(Mine.class, allFixtures, "Mines:", Mine::getKind);

		final MappedCounter<CacheFixture, String, Integer> caches = simpleCounter(CacheFixture::getKind);
		allFixtures.stream().filter(CacheFixture.class::isInstance).map(CacheFixture.class::cast)
				.forEach(caches::add);
		printSummary(caches, "Caches:", CountingCLI::countOfKind, 0);

		final MappedCounter<AdventureFixture, String, Integer> adventures =
				simpleCounter(AdventureFixture::getBriefDescription);
		allFixtures.stream().filter(AdventureFixture.class::isInstance)
				.map(AdventureFixture.class::cast).forEach(adventures::add);
		adventures.addDirectly("Portal to another world", (int) separateTiles.stream()
				.filter(c -> c.stream().anyMatch(Portal.class::isInstance)).count());
		adventures.addDirectly("Ancient battlefield", (int) allFixtures.stream()
				.filter(Battlefield.class::isInstance).count());
		adventures.addDirectly("Cave system", (int) allFixtures.stream()
				.filter(Cave.class::isInstance).count());
		printSummary(adventures, "Adventure Hooks and Portals:", CountingCLI::kindColonCount, 0);

		// TODO: We'd like to count active towns' populations.
		cli.println("Active Communities:");
		cli.println();
		cli.printf("- %d fortresses%n", allFixtures.stream().filter(IFortress.class::isInstance).count());
		cli.printf("- %d active towns, cities, or fortifications of any size%n",
				allFixtures.stream().filter(AbstractTown.class::isInstance)
						.map(AbstractTown.class::cast)
						.filter(t -> TownStatus.Active == t.getStatus()).count());

		final MappedCounter<Village, String, Integer> villages = simpleCounter(Village::getRace);
		allFixtures.stream().filter(Village.class::isInstance).map(Village.class::cast)
				.forEach(villages::add);
		printSummary(villages, "- Villages, grouped by race:", CountingCLI::countSpaceKind, 0);

		final MappedCounter<AbstractTown, String, Integer> inactiveTowns = simpleCounter(
				CountingCLI::townSummary);
		allFixtures.stream().filter(AbstractTown.class::isInstance).map(AbstractTown.class::cast)
				.filter(t -> TownStatus.Active != t.getStatus()).forEach(inactiveTowns::add);
		printSummary(inactiveTowns, "Inactive Communities:");

		final MappedCounter<IUnit, String, Integer> independentUnits =
				simpleCounter(IUnit::getName);
		allFixtures.stream().filter(IUnit.class::isInstance).map(IUnit.class::cast)
				.filter(u -> u.owner().isIndependent()).forEach(independentUnits::add);
		printSummary(independentUnits, "Independent Units:");

		final MappedCounter<IWorker, String, Integer> workers = simpleCounter(IWorker::getRace);
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

		final MappedCounter<Animal, String, Integer> animals =
				new MappedCounter<>(Animal::getKind, Animal::getPopulation, IntAccumulator::new, 0);
		allFixtures.stream().filter(Animal.class::isInstance).map(Animal.class::cast)
				.filter(a -> !a.isTalking()).forEach(animals::add);
		animals.addDirectly("various talking animals", (int) allFixtures.stream()
				.filter(Animal.class::isInstance).map(Animal.class::cast).filter(Animal::isTalking)
				.count());
		printSummary(animals, "Animals");

		final MappedCounter<Implement, String, Integer> equipment = new MappedCounter<>(Implement::getKind,
				Implement::getPopulation, IntAccumulator::new, 0);
		allFixtures.stream().filter(Implement.class::isInstance).map(Implement.class::cast).forEach(equipment::add);

		final Map<String, List<IResourcePile>> groupedResources = allFixtures.stream()
				.filter(IResourcePile.class::isInstance).map(IResourcePile.class::cast)
				.collect(Collectors.groupingBy(IResourcePile::getKind));

		if (!groupedResources.isEmpty()) {
			cli.println("Resources:");
			for (final Map.Entry<String, List<IResourcePile>> entry : groupedResources.entrySet()) {
				final MappedCounter<IResourcePile, String, BigDecimal> counter = new MappedCounter<>(
						CountingCLI::resourcePileKeyExtractor, r -> decimalize(r.getQuantity().number()),
						DecimalAccumulator::new, BigDecimal.ZERO);
				entry.getValue().forEach(counter::add);
				printSummary(counter, entry.getKey(), 1);
			}
		}

		final List<IFixture> remaining = allFixtures.stream().filter(notA(Animal.class))
				.filter(notA(Shrub.class)).filter(notA(Grove.class)).filter(notA(Meadow.class))
				.filter(notA(Immortal.class)).filter(notA(IFortress.class)).filter(notA(IUnit.class))
				.filter(notA(AbstractTown.class)).filter(notA(Village.class))
				.filter(notA(Portal.class)).filter(notA(AdventureFixture.class))
				.filter(notA(CacheFixture.class)).filter(notA(Mine.class))
				.filter(notA(MineralVein.class)).filter(notA(StoneDeposit.class))
				.filter(notA(Ground.class)).filter(notA(Forest.class)).filter(notA(Hill.class))
				.filter(notA(Oasis.class)).filter(notA(AnimalTracks.class)).filter(notA(Cave.class))
				.filter(notA(Battlefield.class)).filter(notA(Implement.class))
				.filter(notA(IResourcePile.class)).toList();

		if (!remaining.isEmpty()) {
			cli.println();
			cli.println("Remaining fixtures:");
			cli.println();
			for (final IFixture fixture : remaining) {
				if (fixture instanceof final TileFixture tf) {
					// TODO: Move getShortDescription up to IFixture?
					cli.printf("- %s%n", tf.getShortDescription());
				} else {
					cli.printf("- %s%n", fixture.toString());
				}
			}
		}
	}
}
