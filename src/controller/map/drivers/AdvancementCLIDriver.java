package controller.map.drivers;

import static view.util.SystemOut.SYS_OUT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.misc.CLIHelper;
import controller.map.misc.ICLIHelper;
import controller.map.misc.MapReaderAdapter;
import model.map.IMutableMapNG;
import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.ProxyWorker;
import model.map.fixtures.mobile.worker.Skill;
import model.misc.IDriverModel;
import model.workermgmt.IWorkerModel;
import model.workermgmt.WorkerModel;
import util.NullCleaner;
import util.Pair;
import util.SingletonRandom;
import util.Warning;

/**
 * A driver to let the user add hours of experience to a player's workers from
 * the command line.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
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
public final class AdvancementCLIDriver implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-a",
			"--adv", ParamCount.One,
			"View a player's workers and manage their advancement",
			"View a player's units, the workers in those units, each worker's Jobs, "
					+ "and his or her level in each Skill in each Job.",
					AdvancementCLIDriver.class);
	/**
	 * The CLI helper.
	 */
	private final ICLIHelper cli = new CLIHelper();
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
		return usage().getShortDescription();
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
		return "AdvancementCLIDriver";
	}
	/**
	 * Run the driver. This form is, at the moment, primarily for use in test code, but that may change.
	 * @param dmodel the driver-model that should be used by the app
	 * @throws DriverFailedException if the driver fails for some reason
	 */
	@Override
	public void startDriver(final IDriverModel dmodel) throws DriverFailedException {
		final IWorkerModel model;
		if (dmodel instanceof IWorkerModel) {
			model = (IWorkerModel) dmodel;
		} else {
			model = new WorkerModel(dmodel);
		}
		final Set<Player> allPlayers = new HashSet<>();
		for (final Pair<IMutableMapNG, File> pair : model.getAllMaps()) {
			allPlayers.addAll(CLIHelper.toList(pair.first().players()));
		}
		final List<Player> playerList = new ArrayList<>(allPlayers);
		try {
			final String hdr = "Available players:";
			final String none = "No players found.";
			final String prpt = "Chosen player: ";
			for (int playerNum = cli.chooseFromList(playerList, hdr, none, prpt,
					false); playerNum >= 0
							&& playerNum < playerList.size(); playerNum = cli
									.chooseFromList(playerList, hdr, none, prpt, false)) {
				advanceWorkers(model,
						NullCleaner.assertNotNull(playerList.remove(playerNum)));
			}
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error interacting with user",
					except);
		}
	}
	/**
	 * Run the driver.
	 * @param args the command-line arguments (map files)
	 * @throws DriverFailedException if the driver fails to start
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			SYS_OUT.print("Usage: ");
			SYS_OUT.print(getClass().getSimpleName());
			SYS_OUT.println(" map [map ...]");
			System.exit(1);
		}
		final MapReaderAdapter reader = new MapReaderAdapter();
		final IDriverModel model = new WorkerModel(
				reader.readMultiMapModel(Warning.INSTANCE, new File(args[0]),
						MapReaderAdapter.namesToFiles(true, args)));
		startDriver(model);
		reader.writeModel(model);
	}
	/**
	 * Let the user add experience to a player's workers.
	 * @param model the driver model
	 * @param player the player whose workers we're interested in
	 * @throws IOException on I/O error getting input from user
	 */
	private void advanceWorkers(final IWorkerModel model, final Player player)
			throws IOException {
		final boolean proxy =
				!cli.inputBoolean("Add experience to workers individually? ");
		final List<IUnit> units = model.getUnits(player);
		while (!units.isEmpty()) {
			final int unitNum = cli.chooseFromList(units,
					player.getName() + "'s units:",
					"No unadvanced units remain.", "Chosen unit: ", false);
			if (unitNum >= 0 && unitNum < units.size()) {
				if (proxy) {
					advanceSingleWorker(new ProxyWorker(
							NullCleaner.assertNotNull(units.remove(unitNum))));
				} else {
					advanceWorkersInUnit(
							NullCleaner.assertNotNull(units.remove(unitNum)));
				}
			} else {
				break;
			}
		}
	}
	/**
	 * Let the user add experience to a worker or workers in a unit.
	 * @param unit the unit in question
	 * @throws IOException on I/O error getting input from user
	 */
	private void advanceWorkersInUnit(final Iterable<UnitMember> unit) throws IOException {
		final List<IWorker> workers =
				StreamSupport.stream(unit.spliterator(), false).filter(member -> member instanceof IWorker)
						.map(p -> (IWorker) p).collect(
						Collectors.toList());
		while (!workers.isEmpty()) {
			final int workerNum = cli.chooseFromList(workers, "Workers in unit:",
					"No unadvanced workers remain.", "Chosen worker: ", false);
			if (workerNum >= 0 && workerNum < workers.size()) {
				advanceSingleWorker(
						NullCleaner.assertNotNull(workers.remove(workerNum)));
			} else {
				break;
			}
		}
	}
	/**
	 * Let the user add experience to a worker.
	 * @param worker the worker in question
	 * @throws IOException on I/O error getting input from user
	 */
	private void advanceSingleWorker(final IWorker worker) throws IOException {
		final List<IJob> jobs = CLIHelper.toList(worker);
		final String hdr = "Jobs in worker:";
		final String none = "No existing jobs.";
		final String prpt = "Job to advance: ";
		for (int jobNum = cli.chooseFromList(jobs, hdr, none, prpt, false); jobNum <= jobs
				.size(); jobNum = cli.chooseFromList(jobs, hdr, none, prpt, false)) {
			if (jobNum < 0 || jobNum == jobs.size()) {
				worker.addJob(new Job(cli.inputString("Name of new Job: "), 0));
				jobs.clear();
				jobs.addAll(CLIHelper.toList(worker));
				SYS_OUT.println("Select the new job at the next prompt.");
				continue;
			} else {
				advanceJob(NullCleaner.assertNotNull(jobs.get(jobNum)));
				if (!cli.inputBoolean("Select another Job in this worker? ")) {
					break;
				}
			}
		}
	}
	/**
	 * Let the user add hours to a skill or skills in a Job.
	 * @param job the job in question
	 * @throws IOException on I/O error getting input from user
	 */
	private void advanceJob(final IJob job) throws IOException {
		final List<ISkill> skills = CLIHelper.toList(job);
		final String hdr = "Jobs in worker:";
		final String none = "No existing jobs.";
		final String prpt = "Job to advance: ";
		for (int skillNum = cli.chooseFromList(skills, hdr, none, prpt, false); skillNum <= skills
				.size(); skillNum = cli.chooseFromList(skills, hdr, none, prpt, false)) {
			if (skillNum < 0 || skillNum == skills.size()) {
				job.addSkill(new Skill(cli.inputString("Name of new Skill: "), 0, 0));
				skills.clear();
				skills.addAll(CLIHelper.toList(job));
				SYS_OUT.println("Select the new skill at the next prompt.");
				continue;
			} else {
				skills.get(skillNum).addHours(cli.inputNumber("Hours of experience to add: "),
						SingletonRandom.RANDOM.nextInt(100));
				if (!cli.inputBoolean("Select another Skill in this Job? ")) {
					break;
				}
			}
		}
	}
}
