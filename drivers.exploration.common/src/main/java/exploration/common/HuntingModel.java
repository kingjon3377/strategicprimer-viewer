package exploration.common;

import java.util.function.Supplier;

import lovelace.util.LovelaceLogger;
import org.javatuples.Pair;

import legacy.map.TileType;
import legacy.map.MapDimensions;
import legacy.map.Point;
import legacy.map.ILegacyMap;
import legacy.map.TileFixture;
import legacy.map.IFixture;
import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.resources.Grove;
import legacy.map.fixtures.resources.Shrub;
import legacy.map.fixtures.resources.Meadow;
import lovelace.util.SingletonRandom;

import java.util.Set;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

/**
 * A class to facilitate a better hunting/fishing driver.
 */
public final class HuntingModel {
	/**
	 * A class and object for "nothing found".
	 *
	 * In Ceylon this was a class unconnected to any other type, but in
	 * Java we can't easily say "this type <em>or</em> this other type ..."
	 * so I make it implement TileFixture. TODO: Could use Optional instead?
	 *
	 * TODO: Log when most of these methods (and {@link
	 * TileFixture#compareTo}) are called
	 */
	public static final class NothingFound implements TileFixture {
		private NothingFound() {
		}

		public static final NothingFound INSTANCE = new NothingFound();

		@Override
		public String getShortDescription() {
			return "Nothing ...";
		}

		@Override
		public NothingFound copy(final CopyBehavior zero) {
			return this;
		}

		@Override
		public int getDC() {
			return 0;
		}

		@Override
		public boolean subsetShouldSkip() {
			return true;
		}

		@Override
		public String getPlural() {
			return "Nothing";
		}

		@Override
		public int getId() {
			return -1;
		}

		@Override
		public boolean equalsIgnoringID(final IFixture fixture) {
			return equals(fixture);
		}
	}

	/**
	 * The "nothing" value we insert.
	 */
	public static final String NO_RESULTS = "Nothing ...";

	/**
	 * How long it should take, in man-hours, to process a carcass of the
	 * specified mass, in pounds. This formula was calculated using
	 * quadratic regression on a set of nine data-points drawn from what I
	 * could find in online research, plus the origin twice. The quadratic
	 * trend curve fit better than the linear trendline for all but three
	 * of the points, and better than a cubic trend curve for all but the
	 * origin.
	 */
	@SuppressWarnings("MagicNumber")
	public static double processingTime(final int weight) {
		return 0.855 + 0.0239 * weight - 0.000000872 * weight * weight;
	}

	@SuppressWarnings("MagicNumber")
	private static int dcIfFound(final TileFixture/*|NothingFound*/ item) {
		if (item instanceof NothingFound) {
			return 60;
		} else {
			return item.getDC();
		}
	}

	/**
	 * An Supplier that takes items randomly, but in proportions such that lower-discovery-DC items are found more
	 * often, from a given stream, with {@link NothingFound} interspersed in a given percentage.
	 */
	private static final class ResultSupplier<Type> implements Supplier<Type> {
		public ResultSupplier(final Collection<Type> stream, final double nothingProportion, final Type nothingValue,
		                      final ToIntFunction<Type> dcGetter) {
			this.stream = new ArrayList<>(stream);
			this.nothingProportion = nothingProportion;
			this.nothingValue = nothingValue;
			this.dcGetter = dcGetter;
		}

		private final double nothingProportion;
		private final List<Type> stream;
		private final Type nothingValue;
		private final ToIntFunction<Type> dcGetter;

		private Type impl() {
			return stream.get(SingletonRandom.SINGLETON_RANDOM.nextInt(stream.size()));
		}

		/**
		 * @return an item selected randomly (see {@link ResultSupplier} class documentation)
		 */
		@Override
		public Type get() {
			if (stream.isEmpty() || SingletonRandom.SINGLETON_RANDOM.nextDouble() < nothingProportion) {
				return nothingValue;
			}
			final Optional<Type> retval = Stream.generate(this::impl)
					.filter(f -> SingletonRandom.SINGLETON_RANDOM.nextInt(20) + 15 >= dcGetter.applyAsInt(f))
					// TODO: Do a .limit(MAX_ITERATIONS) here?
					.findAny();
			if (retval.isPresent()) {
				return retval.get();
			} else {
				LovelaceLogger.warning("Somehow ran out of items to encounter");
				return nothingValue;
			}
		}
	}

	/**
	 * A *non-infinite* stream that returns 'nothing found' values in the
	 * desired proportion.
	 */
	private static <Type> Stream<Type> finiteResultStream(final Collection<Type> stream, final double nothingProportion,
                                                          final Type nothingValue) {
		return Stream.concat(stream.stream(), Stream.generate(() -> nothingValue)
				.limit(Math.round(stream.size() * nothingProportion)));
	}

	/**
	 * The map to hunt in.
	 */
	private final ILegacyMap map;

	public HuntingModel(final ILegacyMap map) {
		this.map = map;
		fishKinds = map.streamLocations()
				.filter(l -> TileType.Ocean == map.getBaseTerrain(l))
				.flatMap(map::streamFixtures)
				.filter(Animal.class::isInstance)
				.map(Animal.class::cast)
				.map(Animal::getKind)
				.collect(Collectors.toSet());
		dimensions = map.getDimensions();
	}

	private final MapDimensions dimensions;

	private final Set<String> fishKinds;

	/**
	 * Animals (outside fortresses and units), both aquatic and
	 * non-aquatic, at the given location in the map.
	 */
	private Stream<Animal> baseAnimals(final Point point) {
		return map.streamFixtures(point).filter(Animal.class::isInstance)
				.map(Animal.class::cast).filter(not(Animal::isTalking));
	}

	/**
	 * Non-aquatic animals (outside fortresses and units) at the given location in the map.
	 */
	private Stream<Animal> animals(final Point point) {
		return baseAnimals(point).filter(a -> !fishKinds.contains(a.getKind()));
	}

	/**
	 * Aquatic animals (outside fortresses and units) at the given location in the map.
	 */
	private Stream<Animal> waterAnimals(final Point point) {
		return baseAnimals(point).filter(a -> fishKinds.contains(a.getKind()));
	}

	/**
	 * Plant-type harvestable fixtures in the map ({@link Grove}, {@link
	 * Meadow}, {@link Shrub}), followed by a number of "nothing found"
	 * sufficient to give the proportion we want for that tile type.
	 */
	private Stream<TileFixture> plants(final Point point) {
		final Collection<TileFixture> retval = map.streamFixtures(point)
				.filter(f -> f instanceof Grove || f instanceof Meadow || f instanceof Shrub)
				.collect(Collectors.toList());
		final double nothingProportion;
		final TileType tileType = map.getBaseTerrain(point);
		if (TileType.Desert == tileType || TileType.Tundra == tileType) {
			nothingProportion = 0.75;
		} else if (TileType.Jungle == tileType) {
			nothingProportion = 1.0 / 3.0;
		} else {
			nothingProportion = 0.5;
		}
		return finiteResultStream(retval, nothingProportion, NothingFound.INSTANCE);
	}

	/**
	 * A helper method for the helper method for hunting, fishing, etc.
	 */
	private static Function<Point, Stream<Pair<Point, ? extends TileFixture>>> chooseFromMapImpl(
			final Function<Point, Stream<? extends TileFixture>> chosenMap) {
		return loc -> chosenMap.apply(loc).map(f -> Pair.with(loc, f));
	}

	private static final double NOTHING_PROPORTION = 0.5;

	/**
	 * A helper method for hunting or fishing.
	 *
	 * @param point     Whereabouts to search
	 * @param map Filter/provider to use to find the animals.
	 */
	private Supplier<Pair<Point, ? extends TileFixture>> chooseFromMap(final Point point,
	                                                         final Function<Point, Stream<? extends TileFixture>> map) {
		return new ResultSupplier<>(
				new SurroundingPointIterable(point, dimensions).stream()
						.flatMap(chooseFromMapImpl(map)).collect(Collectors.toList()), NOTHING_PROPORTION,
				Pair.with(point, NothingFound.INSTANCE), p -> dcIfFound(p.getValue1()));
	}

	/**
	 * Get a stream of hunting results from the area surrounding the given
	 * tile. About half will be "nothing". May be an infinite stream.
	 *
	 * If Java supported union types the fixture type would be
	 * {@code Animal|AnimalTracks|NothingFound}.
	 *
	 * TODO: We'd like to allow callers(?) to specify a proportion that
	 * *should* be tracks, perhaps replacing some of the NothingFound
	 *
	 * @param point Whereabouts to search
	 */
	public Supplier<Pair<Point, ? extends TileFixture>> hunt(final Point point) {
		return chooseFromMap(point, this::animals);
	}

	/**
	 * Get a stream of fishing results from the area surrounding the given
	 * tile. About half will be "nothing". This may be an infinite stream.
	 *
	 * @param point Whereabouts to search
	 */
	public Supplier<Pair<Point, ? extends TileFixture>> fish(final Point point) {
		return chooseFromMap(point, this::waterAnimals);
	}

	/**
	 * Given a location, return the stream of gathering results from just that tile.
	 */
	private Stream<Pair<Point, TileFixture>> gatherImpl(final Point point) {
		return plants(point).map(f -> Pair.with(point, f));
	}

	/**
	 * Get a source of gathering-results from the area surrounding the
	 * given tile. Many will be "nothing," especially from desert and
	 * tundra tiles and less from jungle tiles.
	 *
	 * @param point Whereabouts to search
	 */
	public Supplier<Pair<Point, TileFixture>> gather(final Point point) {
		final List<Pair<Point, TileFixture>> retval =
				new SurroundingPointIterable(point, dimensions).stream()
						.flatMap(this::gatherImpl).collect(Collectors.toList());
		return new PairSupplier(retval);
	}

	private record PairSupplier(List<Pair<Point, TileFixture>> retval) implements Supplier<Pair<Point, TileFixture>> {

		@Override
		public Pair<Point, TileFixture> get() {
			return retval.get(SingletonRandom.SINGLETON_RANDOM.nextInt(retval.size()));
		}
	}
}
