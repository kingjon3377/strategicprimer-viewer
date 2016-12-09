package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import model.map.IMutableMapNG;
import model.map.Point;
import model.map.fixtures.terrain.Forest;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import util.Pair;

/**
 * A driver to fix ID mismatches between forests in the main and player maps.
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
								   "Make sure that IDs for forests in submaps match those in the main map");


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
		final IMultiMapModel mmodel = (IMultiMapModel) model;
		final IMutableMapNG mainMap = model.getMap();
		final List<Forest> mainForests = new ArrayList<>();
		final List<Forest> subForests = new ArrayList<>();
		for (final Pair<IMutableMapNG, Optional<Path>> pair : mmodel.getSubordinateMaps
																			 ()) {
			final Optional<Path> maybePath = pair.second();
			if (maybePath.isPresent()) {
				System.out.printf("Starting %s%n", maybePath.get().toString());
			} else {
				System.out.println("Starting a map with no associated path");
			}
			final IMutableMapNG map = pair.first();
			for (final Point location : map.locations()) {
				mainForests.clear();
				subForests.clear();
				Stream.concat(Stream.of(mainMap.getForest(location)),
						mainMap.streamOtherFixtures(location)
								.filter(Forest.class::isInstance)
								.map(Forest.class::cast))
						.filter(Objects::nonNull).forEach(mainForests::add);
				Stream.concat(Stream.of(map.getForest(location)),
						map.streamOtherFixtures(location)
								.filter(Forest.class::isInstance)
								.map(Forest.class::cast))
						.filter(Objects::nonNull).forEach(subForests::add);
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
						System.out.printf("Unmatched forest in %s: %s%n",
								location.toString(), forest.toString());
						mainMap.addFixture(location, forest.copy(false));
					}
				}
			}
		}
	}
}
