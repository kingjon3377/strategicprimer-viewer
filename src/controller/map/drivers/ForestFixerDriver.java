package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.Point;
import model.map.fixtures.Ground;
import model.map.fixtures.terrain.Forest;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import util.Pair;

/**
 * A driver to fix ID mismatches between forests and Ground in the main and player maps.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class ForestFixerDriver implements SimpleCLIDriver {
	/**
	 * Usage object.
	 */
	private static final IDriverUsage USAGE =
			new DriverUsage(false, "-f", "--fix-forest", ParamCount.AtLeastTwo,
								   "Fix forest IDs",
								   "Make sure that forest IDs in submaps match the main map");


	@Override
	public IDriverUsage usage() {
		return USAGE;
	}

	/**
	 * Run the driver. This form is, at the moment, primarily for use in test code, but
	 * that may change. At the moment implementations should *not* interact with the
	 * filesystem, including calling methods that will.
	 *
	 * @param cli     the interface to interact with the console user
	 * @param options any options that were passed on the command line.
	 * @param model   the driver-model that should be used by the app
	 * @throws DriverFailedException if the driver fails for some reason
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model) throws DriverFailedException {
		final IMultiMapModel mapModel = (IMultiMapModel) model;
		final IMutableMapNG mainMap = model.getMap();
		final List<Forest> mainForests = new ArrayList<>();
		final List<Forest> subForests = new ArrayList<>();
		final List<Ground> mainGround = new ArrayList<>();
		final List<Ground> subGround = new ArrayList<>();
		for (final Pair<IMutableMapNG, Optional<Path>> pair :
				mapModel.getSubordinateMaps()) {
			final Optional<Path> maybePath = pair.second();
			cli.printf("Starting %s%n", maybePath.map(Path::toString)
												.orElse("a map with no associated path"));
			final IMutableMapNG map = pair.first();
			for (final Point location : map.locations()) {
				extractForests(mainMap, location, mainForests);
				extractForests(map, location, subForests);
				for (final Forest forest : subForests) {
					if (mainForests.contains(forest)) {
						continue;
					}
					final Optional<Forest> matching =
							mainForests.stream().filter(forest::equalsIgnoringID)
									.findAny();
					if (matching.isPresent()) {
						forest.setID(matching.get().getID());
					} else {
						cli.printf("Unmatched forest in %s: %s%n", location.toString(),
								forest.toString());
						mainMap.addFixture(location, forest.copy(false));
					}
				}
				extractGround(mainMap, location, mainGround);
				extractGround(map, location, subGround);
				for (final Ground ground : subGround) {
					if (mainGround.contains(ground)) {
						continue;
					}
					final Optional<Ground> matching =
							mainGround.stream().filter(ground::equalsIgnoringID)
									.findAny();
					if (matching.isPresent()) {
						ground.setID(matching.get().getID());
					} else {
						cli.printf("Unmatched ground in %s: %s%n", location.toString(),
								ground.toString());
						mainMap.addFixture(location, ground.copy(false));
					}
				}
			}
		}
	}
	/**
	 * We clear the list, then add all forests at the given point in the given map to
	 * the list.
	 * @param map a map
	 * @param point a location
	 * @param list a list to add forests to
	 */
	private static void extractForests(final IMapNG map, final Point point,
									   final List<Forest> list) {
		list.clear();
		map.streamAllFixtures(point).filter(Forest.class::isInstance)
				.map(Forest.class::cast).filter(Objects::nonNull).forEach(list::add);
	}
	/**
	 * We clear the list, then add all Ground at the given point in the given map to
	 * the list.
	 * @param map a map
	 * @param point a location
	 * @param list a list to add ground to
	 */
	private static void extractGround(final IMapNG map, final Point point,
									   final List<Ground> list) {
		list.clear();
		map.streamAllFixtures(point).filter(Ground.class::isInstance)
				.map(Ground.class::cast).filter(Objects::nonNull).forEach(list::add);
	}
}
