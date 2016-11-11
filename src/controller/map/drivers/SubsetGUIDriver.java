package controller.map.drivers;

import controller.map.formatexceptions.SPFormatException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import model.map.IMutableMapNG;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import util.Pair;
import view.map.misc.SubsetFrame;
import view.util.ErrorShower;

/**
 * A driver to check whether player maps are subsets of the main map and display the
 * results graphically.
 *
 *
 * TODO: Unify with SubsetDriver somehow.
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
public final class SubsetGUIDriver implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(true, "-s", "--subset", ParamCount.AtLeastTwo,
								"Check players' maps against master",
								"Check that subordinate maps are subsets of the main " +
										"map, containing nothing that it does not " +
										"contain in the same place",
								SubsetGUIDriver.class);

	/**
	 * Run the driver.
	 *
	 * @param options
	 * @param model the driver model
	 */
	@Override
	public void startDriver(final SPOptions options, final IDriverModel model) {
		if (model instanceof IMultiMapModel) {
			final SubsetFrame frame = new SubsetFrame();
			SwingUtilities.invokeLater(() -> frame.setVisible(true));
			frame.loadMain(model.getMap());
			for (final Pair<IMutableMapNG, Optional<Path>> pair : ((IMultiMapModel) model)
																.getSubordinateMaps()) {
				frame.test(pair.first(), pair.second());
			}
		} else {
			ErrorShower.showErrorDialog(null,
					"The subset driver doesn't make sense on a non-multi-map driver " +
							"model.");
		}
	}

	/**
	 * Run the driver.
	 *
	 *
	 * @param options
	 * @param args command-line arguments
	 * @throws DriverFailedException if the main map fails to load
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	public void startDriver(final SPOptions options, final String... args)
			throws DriverFailedException {
		if (args.length < 2) {
			throw new IncorrectUsageException(usage());
		}
		final SubsetFrame frame = new SubsetFrame();
		SwingUtilities.invokeLater(() -> frame.setVisible(true));
		final Path first = Paths.get(args[0]);
		try {
			frame.loadMain(first);
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			throw new DriverFailedException("I/O error loading main map "
													+ first, except);
		} catch (final XMLStreamException except) {
			throw new DriverFailedException("XML error reading main map "
													+ first, except);
		} catch (final SPFormatException except) {
			throw new DriverFailedException("Invalid SP XML in main map "
													+ first, except);
		}
		Stream.of(args).skip(1).map(Paths::get).forEach(frame::test);
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE;
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "SubsetGUIDriver";
	}
}
