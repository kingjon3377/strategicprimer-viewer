package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import model.map.Player;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.ProxyJob;
import model.map.fixtures.mobile.worker.ProxyWorker;
import model.map.fixtures.mobile.worker.Skill;
import model.misc.IDriverModel;
import model.workermgmt.IWorkerModel;
import model.workermgmt.WorkerModel;
import util.ListMaker;
import util.NullCleaner;
import util.SingletonRandom;

import static view.util.SystemOut.SYS_OUT;

/**
 * A driver to let the user add hours of experience to a player's workers from the command
 * line.
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
public final class AdvancementCLIDriver implements SimpleCLIDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-a", "--adv", ParamCount.AtLeastOne,
								   "View a player's workers and manage their " +
										   "advancement",
								   "View a player's units, the workers in those units," +
										   " each worker's Jobs, and his or her level " +
										   "in each Skill in each Job.");

	static {
		USAGE.addSupportedOption("--current-turn=NN");
	}

	/**
	 * Let the user add experience to a player's workers.
	 *
	 * @param model  the driver model
	 * @param player the player whose workers we're interested in
	 * @param cli    the interface to the user
	 * @throws IOException on I/O error getting input from user
	 */
	private static void advanceWorkers(final IWorkerModel model,
									   final Player player, final ICLIHelper cli)
			throws IOException {
		final List<IUnit> units = model.getUnits(player);
		units.removeIf(unit -> !unit.iterator().hasNext());
		while (!units.isEmpty()) {
			final int unitNum = cli.chooseFromList(units,
					player.getName() + "'s units:",
					"No unadvanced units remain.", "Chosen unit: ", false);
			if ((unitNum >= 0) && (unitNum < units.size())) {
				advanceWorkersInUnit(NullCleaner.assertNotNull(units.remove(unitNum)),
						cli);
			} else {
				break;
			}
		}
	}

	/**
	 * Let the user add experience to a worker or workers in a unit.
	 *
	 * @param unit the unit in question
	 * @param cli  the interface to the user
	 * @throws IOException on I/O error getting input from user
	 */
	private static void advanceWorkersInUnit(final Iterable<UnitMember> unit,
											 final ICLIHelper cli) throws IOException {
		final List<IWorker> workers = StreamSupport.stream(unit.spliterator(), false)
											  .filter(IWorker.class::isInstance)
											  .map(IWorker.class::cast)
											  .collect(Collectors.toList());
		if (cli.inputBoolean("Add experience to workers individually? ")) {
			while (!workers.isEmpty()) {
				final int workerNum = cli.chooseFromList(workers, "Workers in unit:",
						"No unadvanced workers remain.", "Chosen worker: ", false);
				if ((workerNum >= 0) && (workerNum < workers.size())) {
					advanceSingleWorker(
							NullCleaner.assertNotNull(workers.remove(workerNum)), cli);
				} else {
					break;
				}
			}
		} else {
			if (workers.isEmpty()) {
				cli.println("No workers in unit.");
				return;
			}
			final List<IJob> jobs = ListMaker.toList(new ProxyWorker(unit));
			final String hdr = "Jobs in workers:";
			final String none = "No existing jobs.";
			final String prompt = "Job to advance: ";
			final ICLIHelper.ChoiceOperation choice =
					() -> cli.chooseFromList(jobs, hdr, none, prompt, false);
			for (int jobNum = choice.choose(); jobNum <= jobs.size();
					jobNum = choice.choose()) {
				final IJob job;
				if ((jobNum < 0) || (jobNum == jobs.size())) {
					final String jobName = cli.inputString("Name of new Job: ");
					workers.forEach(worker -> worker.addJob(new Job(jobName, 0)));
					jobs.clear();
					new ProxyWorker(unit).forEach(jobs::add);
					final Optional<IJob> temp =
							jobs.stream().filter(item -> jobName.equals(item.getName()))
									.findAny();
					if (temp.isPresent()) {
						job = temp.get();
					} else {
						SYS_OUT.println("Select the new job at the next prompt.");
						continue;
					}
				} else {
					job = jobs.get(jobNum);
				}
				advanceWorkersInJob(workers, job.getName(), cli);
				if (!cli.inputBoolean("Select another Job in these workers? ")) {
					break;
				}
			}
		}
	}

	/**
	 * Let the user add experience in a given Job to all of a list of workers.
	 *
	 * @param workers the workers in question
	 * @param jobName the name of the Job to consider
	 * @param cli     the interface to the user
	 * @throws IOException on I/O error getting input from user
	 */
	private static void advanceWorkersInJob(final Collection<IWorker> workers,
											final String jobName, final ICLIHelper cli)
			throws IOException {
		final Map<String, IJob> jobs = new HashMap<>();
		for (final IWorker worker : workers) {
			final IJob job = worker.getJob(jobName);
			if (job == null) {
				final IJob temp = new Job(jobName, 0);
				worker.addJob(temp);
				jobs.put(worker.getName(), temp);
			} else {
				jobs.put(worker.getName(), job);
			}
		}
		final List<ISkill> skills = ListMaker.toList(
				new ProxyJob(jobName, false, workers.stream().toArray(IWorker[]::new)));
		final String hdr = "Skills in Jobs:";
		final String none = "No existing skills.";
		final String prompt = "Skill to advance: ";
		final ICLIHelper.ChoiceOperation choice =
				() -> cli.chooseFromList(skills, hdr, none, prompt, false);
		for (int skillNum = choice.choose(); skillNum <= skills.size();
				skillNum = choice.choose()) {
			final ISkill skill;
			if ((skillNum < 0) || (skillNum == skills.size())) {
				final String skillName = cli.inputString("Name of new Skill: ");
				jobs.values().forEach(job -> job.addSkill(new Skill(skillName, 0, 0)));
				skills.clear();
				new ProxyJob(jobName, false, workers.stream().toArray(IWorker[]::new))
						.forEach(skills::add);
				final Optional<ISkill> temp =
						skills.stream().filter(item -> skillName.equals(item.getName()))
								.findAny();
				if (temp.isPresent()) {
					skill = temp.get();
				} else {
					SYS_OUT.println("Select the new skill at the next prompt.");
					continue;
				}
			} else {
				skill = skills.get(skillNum);
			}
			advanceWorkersInSkill(workers, jobName, skill.getName(), cli);
			if (!cli.inputBoolean("Select another Skill in this Job? ")) {
				break;
			}
		}
	}

	/**
	 * Let the user add experience in a single skill to all of a list of workers.
	 *
	 * @param workers   the list of workers
	 * @param jobName   the name of the Job we're considering
	 * @param skillName the name of the Skill to advance
	 * @param cli       the interface to interact with the user
	 * @throws IOException on I/O error getting input from user
	 */
	private static void advanceWorkersInSkill(final Iterable<IWorker> workers,
											  final String jobName,
											  final String skillName,
											  final ICLIHelper cli) throws IOException {
		final int hours = cli.inputNumber("Hours of experience to add: ");
		for (final IWorker worker : workers) {
			final Optional<IJob> tempJob = Optional.ofNullable(worker.getJob(jobName));
			final IJob job;
			if (tempJob.isPresent()) {
				job = tempJob.get();
			} else {
				worker.addJob(new Job(jobName, 0));
				job = worker.getJob(jobName);
				if (job == null) {
					throw new IllegalStateException("Worker lacks Job after adding it");
				}
			}
			final Optional<ISkill> tempSkill =
					Optional.ofNullable(job.getSkill(skillName));
			final ISkill skill;
			if (tempSkill.isPresent()) {
				skill = tempSkill.get();
			} else {
				job.addSkill(new Skill(skillName, 0, 0));
				skill = job.getSkill(skillName);
				if (skill == null) {
					throw new IllegalStateException("Job lacks Skill after adding it");
				}
			}
			final int oldLevel = skill.getLevel();
			skill.addHours(hours, SingletonRandom.RANDOM.nextInt(100));
			if (skill.getLevel() != oldLevel) {
				cli.printf("%s gained a level in %s%n", worker.getName(),
						skill.getName());
			}
		}
	}

	/**
	 * Let the user add experience to a worker.
	 *
	 * @param worker the worker in question
	 * @param cli    the interface to the user
	 * @throws IOException on I/O error getting input from user
	 */
	private static void advanceSingleWorker(final IWorker worker,
											final ICLIHelper cli) throws IOException {
		final List<IJob> jobs = ListMaker.toList(worker);
		final String hdr = "Jobs in worker:";
		final String none = "No existing jobs.";
		final String prompt = "Job to advance: ";
		final ICLIHelper.ChoiceOperation choice =
				() -> cli.chooseFromList(jobs, hdr, none, prompt, false);
		for (int jobNum = choice.choose(); jobNum <= jobs.size();
				jobNum = choice.choose()) {
			final IJob job;
			if ((jobNum < 0) || (jobNum == jobs.size())) {
				final String jobName = cli.inputString("Name of new Job: ");
				worker.addJob(new Job(jobName, 0));
				jobs.clear();
				worker.forEach(jobs::add);
				final Optional<IJob> temp =
						jobs.stream().filter(item -> jobName.equals(item.getName()))
								.findAny();
				if (temp.isPresent()) {
					job = temp.get();
				} else {
					SYS_OUT.println("Select the new job at the next prompt.");
					continue;
				}
			} else {
				job = jobs.get(jobNum);
			}
			advanceJob(NullCleaner.assertNotNull(job), cli);
			if (!cli.inputBoolean("Select another Job in this worker? ")) {
				break;
			}
		}
	}

	/**
	 * Let the user add hours to a skill or skills in a Job.
	 *
	 * @param job the job in question
	 * @param cli the interface to the user
	 * @throws IOException on I/O error getting input from user
	 */
	private static void advanceJob(final IJob job, final ICLIHelper cli)
			throws IOException {
		final List<ISkill> skills = ListMaker.toList(job);
		final String hdr = "Skills in Job:";
		final String none = "No existing skills.";
		final String prompt = "Skill to advance: ";
		final ICLIHelper.ChoiceOperation choice =
				() -> cli.chooseFromList(skills, hdr, none, prompt, false);
		for (int skillNum = choice.choose(); skillNum <= skills.size();
				skillNum = choice.choose()) {
			final ISkill skill;
			if ((skillNum < 0) || (skillNum == skills.size())) {
				final String skillName = cli.inputString("Name of new Skill: ");
				job.addSkill(new Skill(skillName, 0, 0));
				skills.clear();
				job.forEach(skills::add);
				final Optional<ISkill> temp =
						skills.stream().filter(item -> skillName.equals(item.getName()))
								.findAny();
				if (temp.isPresent()) {
					skill = temp.get();
				} else {
					SYS_OUT.println("Select the new skill at the next prompt.");
					continue;
				}
			} else {
				skill = skills.get(skillNum);
			}
			final int oldLevel = skill.getLevel();
			skill.addHours(cli.inputNumber("Hours of experience to add: "),
					SingletonRandom.RANDOM.nextInt(100));
			if (skill.getLevel() != oldLevel) {
				cli.printf("Worker(s) gained a level in %s%n", skill.getName());
			}
			if (!cli.inputBoolean("Select another Skill in this Job? ")) {
				break;
			}
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
	 * Returns the class's simple name.
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "AdvancementCLIDriver";
	}

	/**
	 * Run the driver. This form is, at the moment, primarily for use in test code, but
	 * that may change.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param model   the driver-model that should be used by the app
	 * @throws DriverFailedException if the driver fails for some reason
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model)
			throws DriverFailedException {
		final IWorkerModel workerModel;
		if (model instanceof IWorkerModel) {
			workerModel = (IWorkerModel) model;
		} else {
			workerModel = new WorkerModel(model);
		}
		final List<Player> playerList = workerModel.getPlayers();
		try {
			final String hdr = "Available players:";
			final String none = "No players found.";
			final String prompt = "Chosen player: ";
			final ICLIHelper.ChoiceOperation choice =
					() -> cli.chooseFromList(playerList, hdr, none, prompt, false);
			for (int playerNum = choice.choose();
					(playerNum >= 0) && (playerNum < playerList.size());
					playerNum = choice.choose()) {
				advanceWorkers(workerModel,
						NullCleaner.assertNotNull(playerList.remove(playerNum)), cli);
			}
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			throw new DriverFailedException("I/O error interacting with user",
												   except);
		}
	}
}
