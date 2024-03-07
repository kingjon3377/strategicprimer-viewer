package report.generators.tabular;

import java.util.List;

import org.javatuples.Pair;
import lovelace.util.DelayedRemovalMap;

import legacy.DistanceComparator;
import legacy.map.IFixture;
import legacy.map.MapDimensions;
import legacy.map.Point;
import legacy.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.MaturityModel;
import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.AnimalOrTracks;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * A report generator for {@link Animal animal populations} and {@link
 * AnimalTracks sightings of animals}.
 */
public class AnimalTabularReportGenerator implements ITableGenerator<AnimalOrTracks> {
	@Override
	public boolean canHandle(final IFixture fixture) {
		return fixture instanceof AnimalOrTracks;
	}

	public AnimalTabularReportGenerator(final @Nullable Point hq, final MapDimensions dimensions,
										final int currentTurn) {
		this.hq = hq;
		this.dimensions = dimensions;
		this.currentTurn = currentTurn;
	}

	private final @Nullable Point hq;

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
		if (item instanceof AnimalTracks) {
			kind = "tracks or traces of " + item.getKind();
			age = "---";
			population = "---";
		} else if (item instanceof final Animal animal) {
			if (animal.isTalking()) {
				kind = "talking " + item.getKind();
				age = "---";
				population = "---";
			} else if ("wild".equals(animal.getStatus())) {
				kind = item.getKind();
				age = "---";
				population = "---";
			} else {
				kind = String.format("%s %s", animal.getStatus(), item.getKind());
				population = Integer.toString(animal.getPopulation());
				if (animal.getBorn() >= 0) {
					final String lkey = animal.getKind();
					if (animal.getBorn() > currentTurn) {
						age = "unborn";
					} else if (animal.getBorn() == currentTurn) {
						age = "newborn";
					} else if (MaturityModel.getMaturityAges().containsKey(lkey) &&
							MaturityModel.getMaturityAges().get(lkey) <=
									(currentTurn - animal.getBorn())) {
						age = "adult";
					} else {
						age = String.format("%d turns", currentTurn - animal.getBorn());
					}
				} else {
					age = "adult";
				}
			}
		} else {
			throw new IllegalStateException("Unexpected AnimalOrTracks subtype");
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
		if (Objects.isNull(hq)) {
			cmp = 0;
		} else {
			cmp = new DistanceComparator(hq, dimensions).compare(one.getValue0(),
					two.getValue0());
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
