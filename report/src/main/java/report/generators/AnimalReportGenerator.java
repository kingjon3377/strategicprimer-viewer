package report.generators;

import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import lovelace.util.ThrowingConsumer;
import lovelace.util.DelayedRemovalMap;
import java.util.Comparator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Optional;
import java.io.IOException;

import common.map.IFixture;
import common.map.Point;
import common.map.MapDimensions;
import common.map.IMapNG;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.MaturityModel;
import common.map.fixtures.mobile.AnimalPlurals;
import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.AnimalOrTracks;

/**
 * A report generator for sightings of animals.
 */
public class AnimalReportGenerator extends AbstractReportGenerator</*Animal|AnimalTracks*/AnimalOrTracks> {
	public AnimalReportGenerator(final Comparator<Pair<Point, IFixture>> comp, final MapDimensions dimensions,
	                             final int currentTurn) {
		this(comp, dimensions, currentTurn, null);
	}

	public AnimalReportGenerator(final Comparator<Pair<Point, IFixture>> comp, final MapDimensions dimensions,
	                             final int currentTurn, @Nullable final Point hq) {
		super(comp, dimensions, hq);
		this.currentTurn = currentTurn;
	}

	private final int currentTurn;

	/**
	 * Produce the sub-report about an individual Animal. We assume that
	 * individual Animals are members of the player's units, or that for
	 * some other reason the player is allowed to see the precise count of the population.
	 */
	@Override
	public void produceSingle(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                          final IMapNG map, final Consumer<String> ostream,
			/*Animal|AnimalTracks*/final AnimalOrTracks item, final Point loc) {
		// TODO: Extract helper method for the "At (loc):" idiom
		ostream.accept("At ");
		ostream.accept(loc.toString());
		ostream.accept(":");
		if (item instanceof AnimalTracks) {
			// TODO: AnimalOrTracks should inherit HasKind, right?
			ostream.accept(" tracks or traces of ");
			ostream.accept(((AnimalTracks) item).getKind());
		} else {
			Animal animal = (Animal) item;
			if (animal.isTalking()) {
				ostream.accept(" talking");
			}
			if (animal.getBorn() >= 0 && currentTurn >= 0) {
				if (animal.getBorn() > currentTurn) {
					ostream.accept(" unborn");
				} else if (animal.getBorn() == currentTurn) {
					ostream.accept(" newborn");
				} else if (!MaturityModel.getMaturityAges().containsKey(animal.getKind()) ||
						           MaturityModel.getMaturityAges().get(animal.getKind()) > (currentTurn - animal.getBorn())) {
					ostream.accept(String.format(" %d-turn-old", currentTurn - animal.getBorn()));
				}
			}
			ostream.accept(" ");
			if (animal.getPopulation() == 1) {
				ostream.accept(animal.getKind());
			} else {
				ostream.accept(Integer.toString(animal.getPopulation()));
				ostream.accept(" ");
				ostream.accept(AnimalPlurals.get(animal.getKind()));
				if ("wild".equals(animal.getStatus())) {
					ostream.accept(" (ID #");
					ostream.accept(Integer.toString(item.getId()));
					ostream.accept(")");
				}
			}
		}
		ostream.accept(" ");
		ostream.accept(distanceString.apply(loc));
	}

	/**
	 * Produce the sub-report about animals.
	 */
	@Override
	public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, final IMapNG map,
	                    final Consumer<String> ostream) {
		// TODO: Use a multimap, either from Guava or a custom impl in lovelace.util
		final Map<String, List<Point>> items = new HashMap<>();
		for (Triplet<Integer, Point, AnimalOrTracks> triplet : fixtures.entrySet().stream()
				.filter(e -> e.getValue().getValue1() instanceof AnimalOrTracks)
				.sorted(Map.Entry.comparingByValue(pairComparator))
				.map(e -> Triplet.with(e.getKey(), e.getValue().getValue0(),
					(AnimalOrTracks) e.getValue().getValue1()))
				.collect(Collectors.toList())) {
			int key = triplet.getValue0();
			Point loc = triplet.getValue1();
			AnimalOrTracks animal = triplet.getValue2();
			String desc;
			if (animal instanceof AnimalTracks) {
				desc = "tracks or traces of " + ((AnimalTracks) animal).getKind();
			} else if (((Animal) animal).isTalking()) {
				desc = "talking " + ((Animal) animal).getKind();
			} else {
				desc = ((Animal) animal).getKind();
			}
			List<Point> list = Optional.ofNullable(items.get(desc)).orElseGet(ArrayList::new);
			list.add(loc);
			items.put(desc, list);
			fixtures.remove(key);
		}
		if (!items.isEmpty()) {
			ostream.accept("<h4>Animal sightings or encounters</h4>");
			ostream.accept(System.lineSeparator());
			ostream.accept("<ul>");
			ostream.accept(System.lineSeparator());
			for (Map.Entry<String, List<Point>> entry : items.entrySet()) {
				if (!entry.getValue().isEmpty()) {
					ostream.accept("<li>");
					ostream.accept(entry.getKey());
					ostream.accept(": at ");
					ostream.accept(commaSeparatedList(entry.getValue()));
					ostream.accept("</li>");
					ostream.accept(System.lineSeparator());
				}
			}
			ostream.accept("</ul>");
			ostream.accept(System.lineSeparator());
		}
	}
}
