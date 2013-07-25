package controller.map.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import model.map.IFixture;
import model.map.Player;
import model.map.Point;
import model.map.TileCollection;
import model.map.fixtures.mobile.Animal;
import util.IntMap;
import util.Pair;
/**
 * A report generator for sightings of animals.
 * @author Jonathan Lovelace
 *
 */
public class AnimalReportGenerator extends AbstractReportGenerator<Animal> {
	/**
	 * Produce the sub-report on sightings of animals.
	 * @param fixtures the set of fixtures
	 * @param tiles ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the report
	 */
	@Override
	public String produce(final IntMap<Pair<Point, IFixture>> fixtures,
			final TileCollection tiles, final Player currentPlayer) {
		final Map<String, List<Point>> sightings = new HashMap<>();
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Animal) {
				final Animal animal = (Animal) pair.second();
				// ESCA-JAVA0177:
				final String string; // NOPMD
				if (animal.isTraces()) {
					string = "tracks or traces of " + animal.getKind();
				} else if (animal.isTalking()) {
					string = "talking " + animal.getKind();
				} else {
					string = animal.getKind();
				}
				// ESCA-JAVA0177:
				final List<Point> points; // NOPMD
				if (sightings.containsKey(string)) {
					points = sightings.get(string);
				} else {
					points = new ArrayList<>(); // NOPMD
					sightings.put(string, points);
				}
				points.add(pair.first());
				fixtures.remove(Integer.valueOf(animal.getID()));
			}
		}
		if (sightings.isEmpty()) {
			return ""; // NOPMD
		} else {
			final StringBuilder builder = new StringBuilder("<h4>Animal sightings or encounters</h4>\n").append(OPEN_LIST);
			for (Entry<String, List<Point>> entry : sightings.entrySet()) {
				builder.append(OPEN_LIST_ITEM).append(entry.getKey())
						.append(": at ").append(pointCSL(entry.getValue()))
						.append(CLOSE_LIST_ITEM);
			}
			return builder.append(CLOSE_LIST).toString(); // NOPMD
		}
	}
	/**
	 * @param fixtures the set of fixtures
	 * @param currentPlayer the player for whom the report is being produced
	 * @param tiles ignored
	 * @param item an animal
	 * @param loc its location
	 * @return a sub-report on the animal
	 */
	@Override
	public String produce(final IntMap<Pair<Point, IFixture>> fixtures,
			final TileCollection tiles, final Player currentPlayer, final Animal item, final Point loc) {
		return new StringBuilder(atPoint(loc)).append(
				item.isTraces() ? "tracks or traces of "
						: (item.isTalking() ? "talking " : "")).append(
				item.getKind()).toString();
	}

}
