package report.generators;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import lovelace.util.DelayedRemovalMap;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import legacy.map.IFixture;
import legacy.map.Point;
import legacy.map.MapDimensions;
import legacy.map.ILegacyMap;
import legacy.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.MaturityModel;
import common.map.fixtures.mobile.AnimalPlurals;
import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.AnimalOrTracks;

/**
 * A report generator for sightings of animals.
 */
public class AnimalReportGenerator extends AbstractReportGenerator</*Animal|AnimalTracks*/AnimalOrTracks> {
	public AnimalReportGenerator(final MapDimensions dimensions, final int currentTurn) {
		this(dimensions, currentTurn, null);
	}

	public AnimalReportGenerator(final MapDimensions dimensions, final int currentTurn, final @Nullable Point hq) {
		super(dimensions, hq);
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
	                          final ILegacyMap map, final Consumer<String> ostream,
		/*Animal|AnimalTracks*/final AnimalOrTracks item, final Point loc) {
		// TODO: Extract helper method for the "At (loc):" idiom
		ostream.accept("At ");
		ostream.accept(loc.toString());
		ostream.accept(":");
		if (item instanceof AnimalTracks) {
			ostream.accept(" tracks or traces of ");
			ostream.accept(item.getKind());
		} else {
			final Animal animal = (Animal) item;
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
	public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, final ILegacyMap map,
	                    final Consumer<String> ostream) {
		// TODO: Use a multimap, either from Guava or a custom impl in lovelace.util
		final Map<String, List<Point>> items = new HashMap<>();
		for (final Triplet<Integer, Point, AnimalOrTracks> triplet : fixtures.entrySet().stream()
			.filter(e -> e.getValue().getValue1() instanceof AnimalOrTracks)
			.sorted(Map.Entry.comparingByValue(pairComparator))
			.map(e -> Triplet.with(e.getKey(), e.getValue().getValue0(),
				(AnimalOrTracks) e.getValue().getValue1())).toList()) {
			final int key = triplet.getValue0();
			final Point loc = triplet.getValue1();
			final AnimalOrTracks animal = triplet.getValue2();
			final String desc;
			if (animal instanceof AnimalTracks) {
				desc = "tracks or traces of " + animal.getKind();
			} else if (((Animal) animal).isTalking()) {
				desc = "talking " + animal.getKind();
			} else {
				desc = animal.getKind();
			}
			final List<Point> list = Optional.ofNullable(items.get(desc)).orElseGet(ArrayList::new);
			list.add(loc);
			items.put(desc, list);
			fixtures.remove(key);
		}
		if (!items.isEmpty()) {
			ostream.accept("""
				<h4>Animal sightings or encounters</h4>
				<ul>
				""");
			for (final Map.Entry<String, List<Point>> entry : items.entrySet()) {
				if (!entry.getValue().isEmpty()) {
					ostream.accept("<li>");
					ostream.accept(entry.getKey());
					ostream.accept(": at ");
					ostream.accept(commaSeparatedList(entry.getValue()));
					println(ostream, "</li>");
				}
			}
			println(ostream, "</ul>");
		}
	}
}
