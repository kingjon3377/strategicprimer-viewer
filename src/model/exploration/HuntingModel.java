package model.exploration;

import static model.map.TileType.Ocean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import model.map.HasKind;
import model.map.IMapNG;
import model.map.MapDimensions;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Shrub;
import util.NullCleaner;

/**
 * A class to facilitate a better hunting/fishing driver.
 *
 * TODO: Use MultiMaps once we add the Guava dependency.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class HuntingModel {
	/**
	 * The "nothing" value we insert.
	 */
	public static final String NOTHING = "Nothing ...";
	/**
	 * The non-aquatic animals in the map.
	 */
	private final Map<Point, List<String>> animals = new HashMap<>();
	/**
	 * The aquatic animals in the map.
	 */
	private final Map<Point, List<String>> waterAnimals = new HashMap<>();
	/**
	 * The plants in the map.
	 */
	private final Map<Point, List<String>> plants = new HashMap<>();
	/**
	 * The size of the map.
	 */
	protected final MapDimensions dims;
	/**
	 * Constructor.
	 * @param map the map to hunt in
	 */
	public HuntingModel(final IMapNG map) {
		dims = map.dimensions();
		final Collection<String> fishKinds = StreamSupport.stream(map.locations().spliterator(), false)
				                                     .filter(point -> Ocean == map.getBaseTerrain(point)).flatMap(
						point -> StreamSupport.stream(map.getOtherFixtures(point).spliterator(), false))
				                                     .filter(fix -> fix instanceof Animal)
				                                     .map(fix -> ((HasKind) fix).getKind())
				                                     .collect(Collectors.toSet());
		for (final Point point : map.locations()) {
			for (final TileFixture fix : map.getOtherFixtures(point)) {
				if (fix instanceof Animal && !((Animal) fix).isTalking()
						&& !((Animal) fix).isTraces()) {
					if (fishKinds.contains(((Animal) fix).getKind())) {
						addToMap(waterAnimals, point, ((Animal) fix).getKind());
					} else {
						addToMap(animals, point, ((Animal) fix).getKind());
					}
				} else if (fix instanceof Grove || fix instanceof Meadow
						|| fix instanceof Shrub) {
					addToMap(plants, point,
							NullCleaner.assertNotNull(fix.toString()));
				}
			}
			addToMap(plants, point, NOTHING);
			final List<String> plantList =
					NullCleaner.assertNotNull(plants.get(point));
			final int len = plantList.size() - 1;
			final int nothings; // NOPMD: TODO: extract method?
			switch (map.getBaseTerrain(point)) {
			case Desert:
			case Tundra:
				nothings = len * 3;
				break;
			case Jungle:
				nothings = len / 2;
				break;
			default:
				nothings = len;
				break;
			}
			for (int i = 0; i < nothings; i++) {
				plantList.add(NOTHING);
			}
		}
	}
	/**
	 * @param map one of the mappings
	 * @param point a point
	 * @param string a string to put in the map at that point.
	 */
	private static void addToMap(final Map<Point, List<String>> map,
			final Point point, final String string) {
		final List<String> list; // NOPMD
		if (map.containsKey(point)) {
			list = NullCleaner.assertNotNull(map.get(point));
		} else {
			list = new ArrayList<>();
			map.put(point, list);
		}
		list.add(string);
	}

	/**
	 * @param point
	 *            a point
	 * @param items
	 *            how many items to limit the list to
	 * @return a list of hunting results from the surrounding area. About half
	 *         will be "nothing"
	 */
	public List<String> hunt(final Point point, final int items) {
		return chooseFromMap(point, items, animals);
	}

	/**
	 * @param point
	 *            a point
	 * @param items
	 *            how many items to limit the list to
	 * @return a list of fishing results from the surrounding area. About half
	 *         will be "nothing"
	 */
	public List<String> fish(final Point point, final int items) {
		return chooseFromMap(point, items, waterAnimals);
	}

	/**
	 * @param point a point
	 * @param items how many items to limit the list to
	 * @return a list of gathering results from the surrounding area. Many will
	 *         be "nothing," especially from desert and tundra tiles and less
	 *         from jungle tiles.
	 */
	public Iterable<String> gather(final Point point, final int items) {
		final List<String> choices =
				StreamSupport.stream(new SurroundingPointIterable(point, dims).spliterator(), false)
						.filter(plants::containsKey)
						.flatMap(local -> StreamSupport.stream(plants.get(local).spliterator(), false))
						.collect(Collectors.toList());
		final List<String> retval = new ArrayList<>();
		for (int i = 0; i < items; i++) {
			Collections.shuffle(choices);
			retval.add(choices.get(0));
		}
		return retval;
	}
	/**
	 * A helper method for hunting or fishing.
	 * @param point what point to look around
	 * @param items how many items to limit the results to
	 * @param chosenMap which map to look in
	 * @return a list of results, about one eighth of which will be "nothing."
	 */
	private List<String> chooseFromMap(final Point point, final int items,
			final Map<Point, List<String>> chosenMap) {
		final List<String> choices = new ArrayList<>();
		choices.addAll(StreamSupport.stream(new SurroundingPointIterable(point, dims).spliterator(), false)
				               .filter(chosenMap::containsKey)
				               .flatMap(local -> StreamSupport.stream(chosenMap.get(local).spliterator(), false))
				               .collect(Collectors.toList()));
		final int nothings = choices.size();
		for (int i = 0; i < nothings; i++) {
			choices.add(NOTHING);
		}
		final List<String> retval = new ArrayList<>();
		for (int i = 0; i < items; i++) {
			Collections.shuffle(choices);
			retval.add(choices.get(0));
		}
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "HuntingModel";
	}
}
