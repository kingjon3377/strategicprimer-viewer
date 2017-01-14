package controller.map.drivers;

import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.ICLIHelper;
import controller.map.misc.MapReaderAdapter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import model.map.FixtureIterable;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Point;
import model.map.TileType;
import model.map.fixtures.resources.StoneDeposit;
import model.map.fixtures.resources.StoneKind;
import org.eclipse.jdt.annotation.Nullable;
import util.TypesafeLogger;
import util.Warning;

import static view.util.SystemOut.SYS_OUT;

/**
 * A driver to check every map file in a list for errors.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class MapChecker implements UtilityDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final IDriverUsage USAGE =
			new DriverUsage(false, "-k", "--check", ParamCount.AtLeastOne,
								   "Check map for errors",
								   "Check a map file for errors, deprecated syntax, etc.");

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(MapChecker.class);
	/**
	 * Additional checks.
	 */
	private static final Collection<Check> EXTRA_CHECKS = new ArrayList<>();
	/**
	 * The map reader we'll use.
	 */
	private final MapReaderAdapter reader = new MapReaderAdapter();
	static {
		EXTRA_CHECKS.add((terrain, context, fixture, warner) -> {
			if (fixture instanceof StoneDeposit &&
						StoneKind.Laterite == ((StoneDeposit) fixture).stone() &&
						TileType.Jungle != terrain) {
				warner.warn(
						new SPContentWarning(context, "Laterite stone in non-jungle"));
			}
		});
	}
	/**
	 * Run the driver.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param args    command-line arguments
	 * @throws DriverFailedException if not enough arguments
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final String... args)
			throws DriverFailedException {
		if (args.length < 1) {
			throw new IncorrectUsageException(usage());
		}
		Stream.of(args).map(Paths::get).forEach(this::check);
	}

	/**
	 * Check a map.
	 *
	 * @param file the file to check
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void check(final Path file) {
		SYS_OUT.print("Starting ");
		SYS_OUT.println(file);
		boolean retval = true;
		final IMapNG map;
		final Warning warner = Warning.DEFAULT;
		try {
			map = reader.readMap(file, warner);
		} catch (final MapVersionException e) {
			LOGGER.log(Level.SEVERE,
					"Map version in " + file + " not acceptable to reader", e);
			return;
		} catch (final FileNotFoundException | NoSuchFileException e) {
			LOGGER.log(Level.SEVERE, file + " not found", e);
			return;
		} catch (final IOException e) {
			//noinspection HardcodedFileSeparator
			LOGGER.log(Level.SEVERE, "I/O error reading " + file, e);
			return;
		} catch (final XMLStreamException e) {
			LOGGER.log(Level.SEVERE,
					"XML stream error reading " + file, e);
			return;
		} catch (final SPFormatException e) {
			LOGGER.log(Level.SEVERE,
					"SP map format error reading " + file, e);
			return;
		}
		for (final Check checker : EXTRA_CHECKS) {
			for (final Point location : map.locations()) {
				final TileType terrain = map.getBaseTerrain(location);
				contentCheck(checker, terrain, location,
						singletonOrEmpty(map.getGround(location)), warner);
				contentCheck(checker, terrain, location,
						singletonOrEmpty(map.getForest(location)), warner);
				contentCheck(checker, terrain, location, map.getOtherFixtures(location),
						warner);
			}
		}
		SYS_OUT.print("No errors in ");
		SYS_OUT.println(file);
	}
	/**
	 * @param <T> the type of the item
	 * @param item something or null
	 * @return an empty collection if null, or a singleton collection containing the
	 * item if not
	 */
	private static <T> Collection<T> singletonOrEmpty(@Nullable final T item) {
		if (item == null) {
			return Collections.emptyList();
		} else {
			return Collections.singleton(item);
		}
	}
	/**
	 * Run the given extra check on the given fixtures from the given point.
	 * @param checker a Check
	 * @param terrain the terrain at this point in the map
	 * @param context the current point in the map
	 * @param list a series of fixtures
	 * @param warner the Warning instance to use
	 */
	private static void contentCheck(final Check checker, final TileType terrain,
									 final Point context,
									 final Iterable<? extends IFixture> list,
									 final Warning warner) {
		for (final IFixture fix : list) {
			if (fix instanceof FixtureIterable) {
				contentCheck(checker, terrain, context, (FixtureIterable<?>) fix,
						warner);
			}
			checker.check(terrain, context, fix, warner);
		}
	}

	/**
	 * The usage object.
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public IDriverUsage usage() {
		return USAGE;
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "MapChecker";
	}
	/**
	 * An interface for checks of a map's *contents* that we don't want the
	 * XML-*reading* code to do.
	 */
	@FunctionalInterface
	private interface Check {
		/**
		 * Run the check.
		 * @param terrain the terrain at the current location
		 * @param context The current location.
		 * @param fixture The tile fixture to check
		 * @param warner the Warning instance to use.
		 */
		void check(final TileType terrain, final Point context, final IFixture fixture,
				   final Warning warner);
	}
	/**
	 * An exception for "content" warnings.
	 */
	private static final class SPContentWarning extends Exception {
		/**
		 * Constructor.
		 * @param context the location in the map where the problem is
		 * @param message a message describing the problem
		 */
		protected SPContentWarning(final Point context, final String message) {
			super(String.format("At (%d, %d): %s", context.getRow(), context.getCol(),
					message));
		}
	}
}
