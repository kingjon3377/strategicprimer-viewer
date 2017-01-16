package model.exploration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.map.HasKind;
import model.map.IMapNG;
import model.map.MapDimensions;
import model.map.Point;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Shrub;
import util.MultiMapHelper;

import static model.map.TileType.Desert;
import static model.map.TileType.Jungle;
import static model.map.TileType.Ocean;
import static model.map.TileType.Tundra;

/**
 * A class to facilitate a better hunting/fishing driver.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class HuntingModel {
	/**
	 * The "nothing" value we insert.
	 */
	public static final String NOTHING = "Nothing ...";
	/**
	 * The non-aquatic animals in the map.
	 */
	private final Map<Point, Collection<String>> animals = new HashMap<>();
	/**
	 * The aquatic animals in the map.
	 */
	private final Map<Point, Collection<String>> waterAnimals = new HashMap<>();
	/**
	 * The plants in the map.
	 */
	private final Map<Point, Collection<String>> plants = new HashMap<>();
	/**
	 * The size of the map.
	 */
	private final MapDimensions dims;

	/**
	 * Constructor.
	 *
	 * @param map the map to hunt in
	 */
	public HuntingModel(final IMapNG map) {
		dims = map.dimensions();
		final Collection<String> fishKinds =
				map.locationStream().filter(point -> Ocean == map.getBaseTerrain(point))
						.flatMap(map::streamOtherFixtures)
						.filter(Animal.class::isInstance).map(HasKind.class::cast)
						.map(HasKind::getKind).collect(Collectors.toSet());
		for (final Point point : map.locations()) {
			for (final TileFixture fix : map.getOtherFixtures(point)) {
				if ((fix instanceof Animal) && !((Animal) fix).isTalking()
							&& !((Animal) fix).isTraces()) {
					final String kind = ((Animal) fix).getKind();
					if (fishKinds.contains(kind)) {
						MultiMapHelper.getMapValue(waterAnimals, point,
								key -> new ArrayList<>()).add(kind);
					} else {
						MultiMapHelper
								.getMapValue(animals, point, key -> new ArrayList<>())
								.add(kind);
					}
				} else if ((fix instanceof Grove) || (fix instanceof Meadow) ||
								   (fix instanceof Shrub)) {
					MultiMapHelper.getMapValue(plants, point, key -> new ArrayList<>())
							.add(fix.toString());
				}
			}
			final Collection<String> plantList =
					MultiMapHelper.getMapValue(plants, point, key -> new ArrayList<>());
			final int len = plantList.size() - 1;
			final TileType tileType = map.getBaseTerrain(point);
			final int nothings;
			if ((tileType == Desert) || (tileType == Tundra)) {
				nothings = len * 3;
			} else if (tileType == Jungle) {
				nothings = len / 2;
			} else {
				nothings = len;
			}
			for (int i = 0; i < nothings; i++) {
				plantList.add(NOTHING);
			}
		}
	}

	/**
	 * Get a list of hunting results.
	 * @param point a point
	 * @param items how many items to limit the list to
	 * @return a list of hunting results from the surrounding area. About half will be
	 * "nothing"
	 */
	public List<String> hunt(final Point point, final int items) {
		return chooseFromMap(point, items, animals);
	}

	/**
	 * Get a list of fishing results.
	 * @param point a point
	 * @param items how many items to limit the list to
	 * @return a list of fishing results from the surrounding area. About half will be
	 * "nothing"
	 */
	public List<String> fish(final Point point, final int items) {
		return chooseFromMap(point, items, waterAnimals);
	}

	/**
	 * Get a list of gathering results.
	 * @param point a point
	 * @param items how many items to limit the list to
	 * @return a list of gathering results from the surrounding area. Many will be
	 * "nothing," especially from desert and tundra tiles and less from jungle tiles.
	 */
	public Iterable<String> gather(final Point point, final int items) {
		final List<String> choices = new SurroundingPointIterable(point, dims).stream()
											 .filter(plants::containsKey)
											 .flatMap(local -> plants.get(local)
																	   .stream())
											 .collect(Collectors.toList());
		final Collection<String> retval = new ArrayList<>();
		for (int i = 0; i < items; i++) {
			Collections.shuffle(choices);
			retval.add(choices.get(0));
		}
		return retval;
	}

	/**
	 * A helper method for hunting or fishing.
	 *
	 * @param point     what point to look around
	 * @param items     how many items to limit the results to
	 * @param chosenMap which map to look in
	 * @return a list of results, about one eighth of which will be "nothing."
	 */
	private List<String> chooseFromMap(final Point point, final int items,
									   final Map<Point, Collection<String>> chosenMap) {
		final List<String> choices = new SurroundingPointIterable(point, dims).stream()
											 .filter(chosenMap::containsKey).flatMap(
						local -> chosenMap.get(local).stream())
											 .collect(Collectors.toList());
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
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "HuntingModel";
	}
}
