package report.generators.tabular;

import java.util.List;
import org.javatuples.Pair;
import lovelace.util.DelayedRemovalMap;

import common.DistanceComparator;
import common.map.IFixture;
import common.map.MapDimensions;
import common.map.Point;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.MaturityModel;
import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.AnimalOrTracks;

import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Collections;

/**
 * A report generator for {@link Animal animal populations} and {@link
 * AnimalTracks sightings of animals}.
 */
public class AnimalTabularReportGenerator implements ITableGenerator<AnimalOrTracks> {
	@Override
	public Class<AnimalOrTracks> narrowedClass() {
		return AnimalOrTracks.class;
	}

	public AnimalTabularReportGenerator(@Nullable final Point hq, final MapDimensions dimensions,
	                                    final int currentTurn) {
		this.hq = hq;
		this.dimensions = dimensions;
		this.currentTurn = currentTurn;
	}

	@Nullable
	private final Point hq;

	private final MapDimensions dimensions;

	private final int currentTurn;

	/**
	 * The header row for the table.
	 */
	@Override
	public List<String> getHeaderRow() {
		return Arrays.asList("Distance", "Location", "Number", "Kind", "Age");
	}

	/**
	 * The file-name to (by default) write this table to.
	 */
	@Override
	public String getTableName() {
		return "animals";
	}

	/**
	 * Create a table row representing the given animal.
	 */
	@Override
	public List<List<String>> produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final AnimalOrTracks item, final int key, final Point loc, final Map<Integer, Integer> parentMap) {
		final String kind;
		final String age;
		final String population;
		// TODO: To avoid unchecked-cast warnings, first check if
		// instanceof Animal, and make all the other tests inside that
		// block.
		if (item instanceof AnimalTracks) {
			kind = "tracks or traces of " + item.getKind();
			age = "---";
			population = "---";
		} else if (((Animal) item).isTalking()) {
			kind = "talking " + item.getKind();
			age = "---";
			population = "---";
		} else if (!"wild".equals(((Animal) item).getStatus())) {
			kind = String.format("%s %s", ((Animal) item).getStatus(), item.getKind());
			population = Integer.toString(((Animal) item).getPopulation());
			if (((Animal) item).getBorn() >= 0) {
				if (((Animal) item).getBorn() > currentTurn) {
					age = "unborn";
				} else if (((Animal) item).getBorn() == currentTurn) {
					age = "newborn";
				} else if (MaturityModel.getMaturityAges()
							.containsKey(item.getKind()) &&
						MaturityModel.getMaturityAges()
								.get(item.getKind()) <=
							(currentTurn - ((Animal) item).getBorn())) {
					age = "adult";
				} else {
					age = String.format("%d turns",
						currentTurn - ((Animal) item).getBorn());
				}
			} else {
				age = "adult";
			}
		} else {
			kind = item.getKind();
			age = "---";
			population = "---";
		}
		fixtures.remove(key);
		return Collections.singletonList(Arrays.asList(distanceString(loc, hq, dimensions),
			locationString(loc), population, kind, age));
	}

	/**
	 * Compare two pairs of Animals and locations.
	 */
	@Override
	public Comparator<Pair<Point, AnimalOrTracks>> comparePairs() {
		return this::comparePairsImpl;
	}

	private int comparePairsImpl(final Pair<Point, AnimalOrTracks> one, final Pair<Point, AnimalOrTracks> two) {
		final int cmp;
		if (hq != null) {
			cmp = new DistanceComparator(hq, dimensions).compare(one.getValue0(),
				two.getValue0());
		} else {
			cmp = 0;
		}
		if (cmp == 0) {
			// We'd like to extract the comparison on type to a function, which
			// we would pass in to comparing() with the distance function above
			// to reduce nesting, but there's too much here that can only be applied
			// if both are {@link Animal}s or both are {@link AnimalTracks}.
			if (one.getValue1() instanceof Animal) {
				if (two.getValue1() instanceof Animal) {
					return Comparator.comparing(Animal::isTalking)
							.thenComparing(Animal::getKind)
							.thenComparing(Animal::getPopulation,
								Comparator.reverseOrder())
							.thenComparing(Animal::getBorn)
						.compare((Animal) one.getValue1(), (Animal) two.getValue1());
				} else {
					return 1;
				}
			} else if (two.getValue1() instanceof Animal) {
				return -1;
			} else {
				return one.getValue1().getKind()
					.compareTo(two.getValue1().getKind());
			}
		} else {
			return cmp;
		}
	}
}
