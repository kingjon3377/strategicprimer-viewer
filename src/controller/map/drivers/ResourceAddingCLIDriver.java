package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import model.map.Player;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.misc.IDriverModel;
import model.resources.ResourceManagementDriver;
import util.MultiMapHelper;
import util.Quantity;

/**
 * A driver to let the user enter resources etc.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class ResourceAddingCLIDriver implements SimpleCLIDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-d", "--add-resource", ParamCount.AtLeastOne,
								   "Add resources to maps",
								   "Add resources for players to maps");
	/**
	 * The kinds of resources the user has entered before.
	 */
	private final Set<String> resourceKinds = new HashSet<>();
	/**
	 * A map from resource-kinds to the resource-content types the user has entered
	 * before.
	 */
	private final Map<String, Set<String>> resourceContents = new HashMap<>();
	/**
	 * A map from resource types to units.
	 */
	private final Map<String, String> resourceUnits = new HashMap<>();

	static {
		USAGE.addSupportedOption("--current-turn=NN");
	}

	/**
	 * Ask the user to enter an Implement.
	 *
	 * @param idf    the ID factory
	 * @param model  the driver model
	 * @param cli    how to interact with the user
	 * @param player the current player
	 * @throws IOException on I/O error interacting with the user
	 */
	private static void enterImplement(final IDRegistrar idf,
									   final ResourceManagementDriver model,
									   final ICLIHelper cli, final Player player)
			throws IOException {
		model.addResource(
				new Implement(cli.inputString("Kind of equipment: "), idf.createID()),
				player);
	}

	/**
	 * Start the driver.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param model   the driver-model that should be used by the app
	 * @throws DriverFailedException on any failure
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model)
			throws DriverFailedException {
		final ResourceManagementDriver driverModel;
		if (model instanceof ResourceManagementDriver) {
			driverModel = (ResourceManagementDriver) model;
		} else {
			driverModel = new ResourceManagementDriver(model);
		}
		final List<Player> players =
				StreamSupport.stream(driverModel.getPlayers().spliterator(), false)
						.collect(
								Collectors.toList());
		final IDRegistrar idf = IDFactoryFiller.createFactory(driverModel);
		try {
			final String desc = "Players in the maps:";
			final String none = "No players found.";
			final String prompt = "Player to add resources for: ";
			cli.loopOnList(players,
					clh -> clh.chooseFromList(players, desc, none, prompt, false),
					"Choose another player? ", player -> {
				while (cli.inputBoolean("Keep going? ")) {
					if (cli.inputBooleanInSeries("Enter a (quantified) resource? ")) {
						enterResource(idf, driverModel, cli, player);
					} else if (cli.inputBooleanInSeries("Enter equipment etc.? ")) {
						enterImplement(idf, driverModel, cli, player);
					}
				}
			});
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			throw new DriverFailedException("I/O error interacting with user", except);
		}
	}

	/**
	 * Ask the user to enter a resource.
	 *
	 * @param idf    the ID factory
	 * @param model  the driver model
	 * @param cli    how to interact with the user
	 * @param player the current player
	 * @throws IOException on I/O error interacting with the user
	 */
	private void enterResource(final IDRegistrar idf,
							   final ResourceManagementDriver model,
							   final ICLIHelper cli, final Player player)
			throws IOException {
		final String kind = getResourceKind(cli);
		String contents = getResourceContents(kind, cli);
		final String units = getResourceUnits(contents, cli);
		if (cli.inputBooleanInSeries("Qualify the particular resource with a prefix? ",
				"prefix " + contents)) {
			contents = cli.inputString("Prefix to use: ").trim() + ' ' + contents;
		}
		model.addResource(new ResourcePile(idf.createID(), kind, contents,
												  new Quantity(cli.inputDecimal(
														  String.format(
																  "Quantity in %s? ",
																  units)), units)),
				player);
	}

	/**
	 * Ask the user to choose or enter a resource kind.
	 *
	 * @param cli how to interact with the user
	 * @return the chosen resource-kind
	 * @throws IOException on I/O error interacting with the user
	 */
	private String getResourceKind(final ICLIHelper cli) throws IOException {
		final List<String> list = new ArrayList<>(resourceKinds);
		final int num = cli.chooseStringFromList(list, "Possible kinds of resources:",
				"No resource kinds entered yet", "Chosen kind: ", false);
		if ((num >= 0) && (num < list.size())) {
			return list.get(num);
		} else {
			final String retval = cli.inputString("Resource kind to use: ");
			resourceKinds.add(retval);
			return retval;
		}
	}

	/**
	 * Ask the user to choose or enter a resource-content-type for a given resource kind.
	 *
	 * @param kind the chosen kind
	 * @param cli  how to interact with the user
	 * @return the chosen resource content type
	 * @throws IOException on I/O error interacting with the user
	 */
	private String getResourceContents(final String kind, final ICLIHelper cli)
			throws IOException {
		final Set<String> set = MultiMapHelper.getMapValue(resourceContents, kind,
				key -> new HashSet<>());
		final List<String> list = new ArrayList<>(set);
		final int num = cli.chooseStringFromList(list,
				String.format("Possible resources in the %s category:", kind),
				"No resources entered yet", "Choose resource: ", false);
		if ((num >= 0) && (num < list.size())) {
			return list.get(num);
		} else {
			final String retval = cli.inputString("Resource to use: ");
			set.add(retval);
			return retval;
		}
	}

	/**
	 * Ask the user to choose units for a type of resource.
	 *
	 * @param resource the resource type
	 * @param cli      how to interact with the user
	 * @return the chosen units
	 * @throws IOException on I/O error interacting with the user
	 */
	private String getResourceUnits(final String resource, final ICLIHelper cli)
			throws IOException {
		if (resourceUnits.containsKey(resource)) {
			final String unit = resourceUnits.get(resource);
			if ((unit != null) && cli.inputBooleanInSeries(
					String.format("Is %s the correct units for %s? ", unit, resource),
					"correct;" + unit + ';' + resource)) {
				return unit;
			}
		}
		final String retval =
				cli.inputString(String.format("Unit to use for %s: ", resource));
		resourceUnits.put(resource, retval);
		return retval;
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public IDriverUsage usage() {
		return USAGE;
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ResourceAddingCLIDriver";
	}
}
