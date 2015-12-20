package controller.map.drivers;

import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.misc.CLIHelper;
import controller.map.misc.ICLIHelper;
import controller.map.misc.IDFactory;
import controller.map.misc.IDFactoryFiller;
import controller.map.misc.MapReaderAdapter;
import model.map.Player;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.misc.IDriverModel;
import model.resources.ResourceManagementDriver;
import util.Warning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A driver to let the user enter resources etc.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2015 Jonathan Lovelace
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
public class ResourceAddingCLIDriver implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-d",
			"--add-resource", ParamCount.Many, "Add resources to maps",
			"Add resources for players to maps", ResourceAddingCLIDriver.class);

	@Override
	public void startDriver(final IDriverModel model) throws DriverFailedException {
		final ResourceManagementDriver dmodel;
		if (model instanceof ResourceManagementDriver) {
			dmodel = (ResourceManagementDriver) model;
		} else {
			dmodel = new ResourceManagementDriver(model);
		}
		final ICLIHelper cli = new CLIHelper();
		final List<Player> players = StreamSupport.stream(dmodel.getPlayers().spliterator(), false).collect(
				Collectors.toList());
		final String desc = "Players in the maps:";
		final String none = "No players found.";
		final String prpt = "Player to add resources for: ";
		final IDFactory idf = IDFactoryFiller.createFactory(dmodel);
		try {
			for (int playerNum = cli.chooseFromList(players, desc, none, prpt, false);
					playerNum >= 0 && playerNum < players.size();
					playerNum = cli.chooseFromList(players, desc, none, prpt, false)) {
				final Player player = players.get(playerNum);
				while (cli.inputBoolean("Keep going? ")) {
					if (cli.inputBoolean("Enter a (quantified) resource? ")) {
						enterResource(idf, dmodel, cli, player);
					} else if (cli.inputBoolean("Enter equipment etc.? ")) {
						enterImplement(idf, dmodel, cli, player);
					}
				}
			}
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error interacting with user", except);
		}
	}

	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		final MapReaderAdapter reader = new MapReaderAdapter();
		final ResourceManagementDriver model = new ResourceManagementDriver(reader.readMultiMapModel(Warning.INSTANCE, new File(args[0]), MapReaderAdapter.namesToFiles(true, args)));
		startDriver(model);
		reader.writeModel(model);
	}
	private void enterResource(final IDFactory idf, final ResourceManagementDriver model, final ICLIHelper cli, final Player player) throws IOException {
		final String kind = getResourceKind(cli);
		String contents = getResourceContents(kind, cli);
		final String units = getResourceUnits(contents, cli);
		if (cli.inputBoolean("Qualify the particular resource with a prefix? ")) {
			contents = cli.inputString("Prefix to use: ").trim() + ' ' + contents;
		}
		model.addResource(
				new ResourcePile(idf.createID(), kind, contents,
						                cli.inputNumber(String.format("Quantity in %s? ", units)), units), player);
	}
	private void enterImplement(final IDFactory idf, final ResourceManagementDriver model, final ICLIHelper cli, final Player player) throws  IOException {
		model.addResource(new Implement(idf.createID(), cli.inputString("Kind of equipment: ")), player);
	}
	private final Set<String> resourceKinds = new HashSet<>();
	private String getResourceKind(final ICLIHelper cli) throws IOException {
		final List<String> list = new ArrayList<>(resourceKinds);
		final int num = cli.chooseStringFromList(list, "Possible kinds of resources:", "No resource kinds entered yet", "Chosen kind: ", false);
		if (num >= 0 && num < list.size()) {
			return list.get(num);
		} else {
			final String retval = cli.inputString("Resource kind to use: ");
			resourceKinds.add(retval);
			return retval;
		}
	}
	private final Map<String, Set<String>> resourceContents = new HashMap<>();
	private String getResourceContents(final String kind, final ICLIHelper cli) throws IOException {
		final Set<String> set;
		if (resourceContents.containsKey(kind)) {
			set = resourceContents.get(kind);
		} else {
			set = new HashSet<>();
			resourceContents.put(kind, set);
		}
		final List<String> list = new ArrayList<>(set);
		final int num = cli.chooseStringFromList(list, String.format("Possible resources in the %s category:", kind), "No resources entered yet", "Choose resource: ", false);
		if (num >= 0 && num < list.size()) {
			return list.get(num);
		} else {
			final String retval = cli.inputString("Resource to use: ");
			set.add(retval);
			return retval;
		}
	}
	private final Map<String, String> resourceUnits = new HashMap<>();
	private String getResourceUnits(final String resource, final ICLIHelper cli) throws IOException {
		if (resourceUnits.containsKey(resource)) {
			final String unit = resourceUnits.get(resource);
			if (cli.inputBoolean(String.format("Is %s the correct units for %s? ", unit, resource))) {
				return unit;
			}
		}
		final String retval = cli.inputString(String.format("Unit to use for %s: ", resource));
		resourceUnits.put(resource, retval);
		return retval;
	}
	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE_OBJ;
	}

	/**
	 * @return what to call the driver in a CLI list.
	 */
	@Override
	public String getName() {
		return USAGE_OBJ.getShortDescription();
	}

	/**
	 * @param nomen ignored
	 */
	@Override
	public void setName(final String nomen) {
		throw new IllegalStateException("Can't rename a driver");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ResourceAddingCLIDriver";
	}
}
