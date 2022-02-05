package drivers.advancement;

import drivers.common.cli.ICLIHelper;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lovelace.util.SingletonRandom;

import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.IUnit;

import common.map.fixtures.mobile.worker.ProxyWorker;
import common.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.ProxyJob;
import common.map.fixtures.mobile.worker.ISkill;
import common.map.fixtures.mobile.worker.Skill;

import drivers.common.IAdvancementModel;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

/**
 * Logic extracted from the advancement CLI so it can be used by the turn-running CLI as well.
 */
public class AdvancementCLIHelper implements LevelGainSource {
	public AdvancementCLIHelper(final IAdvancementModel model, final ICLIHelper cli) {
		this.cli = cli;
		this.model = model;
	}

	private final IAdvancementModel model;
	private final ICLIHelper cli;

	private final List<LevelGainListener> levelListeners = new ArrayList<>();

	@Override
	public void addLevelGainListener(final LevelGainListener listener) {
		levelListeners.add(listener);
	}

	@Override
	public void removeLevelGainListener(final LevelGainListener listener) {
		levelListeners.remove(listener);
	}

	private void fireLevelEvent(final String workerName, final String jobName, final String skillName,
	                            final int gains, final int currentLevel) {
		for (LevelGainListener listener : levelListeners) {
			listener.level(workerName, jobName, skillName, gains, currentLevel);
		}
	}

	/**
	 * Let the user add hours to a Skill or Skills in a Job.
	 *
	 * TODO: Avoid Boolean parameters
	 */
	private void advanceJob(final IWorker worker, final IJob job, final boolean allowExpertMentoring) {
		List<ISkill> skills = new ArrayList<>(StreamSupport.stream(job.spliterator(), false)
			.collect(Collectors.toList()));
		while (true) {
			Pair<Integer, @Nullable ISkill> chosen = cli.chooseFromList(skills, "Skills in Job:",
				"No existing Skills.", "Skill to advance: ", false);
			ISkill skill;
			if (chosen.getValue1() != null) {
				skill = chosen.getValue1();
			} else if (chosen.getValue0() <= skills.size()) {
				String skillName = cli.inputString("Name of new Skill: ");
				if (skillName != null) { // TODO: invert?
					model.addHoursToSkill(worker, job.getName(), skillName, 0, 200); // TODO: Have a better way to add the skill without any hours, or else rework this method
					skills.clear();
					job.forEach(skills::add);
					ISkill temp = skills.stream().filter(s -> skillName.equals(s.getName()))
						.findFirst().orElse(null);
					if (temp != null) { // TODO: invert
						skill = temp;
					} else {
						cli.println("Select the new item at the next prompt.");
						continue;
					}
				} else {
					return;
				}
			} else {
				break;
			}
			int oldLevel = skill.getLevel();
			int hours = Optional.ofNullable(cli.inputNumber("Hours of experience to add: ")).orElse(0);
			if (allowExpertMentoring) {
				int hoursPerHour = Optional.ofNullable(cli.inputNumber("'Hours' between hourly checks: "))
					.orElse(0);
				int remaining;
				if (hoursPerHour > 0) {
					remaining = hours;
				} else {
					remaining = 0;
				}
				while (remaining > 0) {
					model.addHoursToSkill(worker, job.getName(), skill.getName(),
						Math.max(remaining, hoursPerHour),
						SingletonRandom.SINGLETON_RANDOM.nextInt(100));
					remaining -= hoursPerHour;
				}
			} else {
				for (int hour = 0; hour < hours; hour++) {
					model.addHoursToSkill(worker, job.getName(), skill.getName(), 1,
						SingletonRandom.SINGLETON_RANDOM.nextInt(100));
				}
			}
			if (skill.getLevel() != oldLevel) {
				String count = (skill.getLevel() - oldLevel == 1) ? "a level"
					: (skill.getLevel() - oldLevel) + " levels";
				cli.println(String.format("%s gained %s in %s", worker.getName(), count, skill.getName()));
				fireLevelEvent(worker.getName(), job.getName(), skill.getName(), skill.getLevel() - oldLevel,
					skill.getLevel());
			}
			Boolean continuation = cli.inputBoolean("Select another Skill in this Job?");
			if (continuation == null || !continuation) {
				break;
			}
		}
	}

	/**
	 * Let the user add experience to a worker.
	 */
	private void advanceSingleWorker(final IWorker worker, final boolean allowExpertMentoring) {
		List<IJob> jobs = new ArrayList<>(StreamSupport.stream(worker.spliterator(), false)
			.collect(Collectors.toList()));
		while (true) {
			Pair<Integer, @Nullable IJob> chosen = cli.chooseFromList(jobs,
				"Jobs in worker:", "No existing Jobs.", "Job to advance: ", false);
			IJob job;
			if (chosen.getValue1() != null) {
				job = chosen.getValue1();
			} else if (chosen.getValue0() <= jobs.size()) {
				String jobName = cli.inputString("Name of new Job: ");
				if (jobName == null) {
					return;
				}
				model.addJobToWorker(worker, jobName);
				jobs.clear();
				worker.forEach(jobs::add);
				IJob temp = jobs.stream().filter(j -> jobName.equals(j.getName()))
					.findFirst().orElse(null);
				if (temp != null) {
					job = temp;
				} else {
					cli.println("Select the new item at the next prompt.");
					continue;
				}
			} else {
				break;
			}
			advanceJob(worker, job, allowExpertMentoring);
			Boolean continuation = cli.inputBoolean("Select another Job in this worker?");
			if (continuation == null || !continuation) {
				break;
			}
		}
	}

	/**
	 * Let the user add experience in a single Skill to all of a list of workers.
	 *
	 * TODO: Support expert mentoring
	 */
	private void advanceWorkersInSkill(final String jobName, final String skillName, final IWorker... workers) {
		int hours = Optional.ofNullable(cli.inputNumber("Hours of experience to add: ")).orElse(0);
		for (IWorker worker : workers) {
			IJob job = worker.getJob(jobName);
			ISkill skill = job.getSkill(skillName);
			int oldLevel = skill.getLevel();
			model.addHoursToSkill(worker, jobName, skillName, hours,
				SingletonRandom.SINGLETON_RANDOM.nextInt(100));
			if (skill.getLevel() != oldLevel) {
				if (oldLevel == 0 && "miscellaneous".equals(skill.getName())) {
					Boolean chooseAnother = cli.inputBooleanInSeries(String.format(
						"%s gained %d level(s) in miscellaneous, choose another skill?",
						worker.getName(), skill.getLevel()), "misc-replacement");
					if (chooseAnother == null) {
						return;
					} else if (!chooseAnother) {
						continue;
					}
					List<String> gains = new ArrayList<>();
					for (int i = 0; i < skill.getLevel(); i++) {
						ISkill replacement;
						Pair<Integer, @Nullable ISkill> choice = cli.chooseFromList(
							StreamSupport.stream(job.spliterator(), false)
								.filter(s -> !"miscellaneous".equals(s.getName()))
								.collect(Collectors.toList()),
							"Skill to gain level in:", "No other skill", "Chosen skill:",
							false);
						if (choice.getValue1() != null) {
							replacement = choice.getValue1();
							model.replaceSkillInJob(worker, job.getName(), skill, replacement);
							model.addHoursToSkill(worker, job.getName(), replacement.getName(),
								100, 0);
							// FIXME: Add to {@link gains}? The listener should cover that
							// already, but in practice it doesn't.
							continue;
						}
						String replacementName = cli.inputString("Skill to gain level in: ");
						if (replacementName == null) {
							return;
						}
						replacement = new Skill(replacementName, 1, 0);
						model.replaceSkillInJob(worker, job.getName(), skill, replacement);
						gains.add(replacementName);
					}
					Map<String, Long> frequencies = gains.stream()
						.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
					for (Map.Entry<String, Long> entry : frequencies.entrySet()) {
						String name = entry.getKey();
						long count = entry.getValue();
						if (count == 1L) {
							cli.println(String.format("%s gained a level in %s",
								worker.getName(), name));
						} else {
							cli.println(String.format("%s gained %d levels in %s",
								worker.getName(), count, name));
						}
						fireLevelEvent(worker.getName(), job.getName(), name, (int) count,
							job.getSkill(name).getLevel());
					}
				} else {
					cli.println(String.format("%s gained a level in %s", worker.getName(),
						skill.getName()));
					fireLevelEvent(worker.getName(), job.getName(), skill.getName(),
						skill.getLevel() - oldLevel, skill.getLevel());
				}
			}
		}
	}

	/**
	 * Let the user add experience in a given Job to all of a list of workers.
	 */
	private void advanceWorkersInJob(final String jobName, final IWorker... workers) {
		List<ISkill> skills = new ArrayList<>(StreamSupport.stream(
			new ProxyJob(jobName, false, workers).spliterator(), false).collect(Collectors.toList()));
		while (true) {
			Pair<Integer, @Nullable ISkill> chosen = cli.chooseFromList(skills,
				"Skills in Jobs:", "No existing skills.", "Skill to advance: ", false);
			ISkill skill;
			if (chosen.getValue1() != null) {
				skill = chosen.getValue1();
			} else if (chosen.getValue0() <= skills.size()) { // TODO: invert?
				String skillName = cli.inputString("Name of the new skill: ");
				if (skillName == null) {
					return;
				}
				for (IWorker worker : workers) {
					model.addHoursToSkill(worker, jobName, skillName, 0, 200);
				}
				skills.clear();
				new ProxyJob(jobName, false, workers).forEach(skills::add);
				ISkill temp = skills.stream().filter(s -> skillName.equals(s.getName()))
					.findAny().orElse(null);
				if (temp != null) { // TODO: invert
					skill = temp;
				} else {
					cli.println("Select the new item at the next prompt.");
					continue;
				}
			} else {
				break;
			}
			advanceWorkersInSkill(jobName, skill.getName(), workers);
			Boolean continuation = cli.inputBoolean("Select another Skill in this Job?");
			if (continuation == null || !continuation) {
				break;
			}
		}
	}

	/**
	 * Let the user add experience to a worker or workers in a unit.
	 */
	public void advanceWorkersInUnit(final IUnit unit, final boolean allowExpertMentoring) {
		List<IWorker> workers = new ArrayList<>(unit.stream()
			.filter(IWorker.class::isInstance).map(IWorker.class::cast).collect(Collectors.toList()));
		Boolean individualAdvancement = cli.inputBooleanInSeries("Add experience to workers individually? ");
		if (individualAdvancement == null) {
			return;
		} else if (individualAdvancement) {
			while (!workers.isEmpty()) {
				IWorker chosen = cli.chooseFromList(workers, "Workers in unit:",
					"No unadvanced workers remain.", "Chosen worker: ", false).getValue1();
				if (chosen == null) {
					break;
				}
				workers.remove(chosen);
				advanceSingleWorker(chosen, allowExpertMentoring);
				Boolean continuation = cli.inputBoolean("Choose another worker?");
				if (continuation == null || !continuation) {
					break;
				}
			}
		} else {
			if (workers.isEmpty()) {
				cli.println("No workers in unit.");
				return;
			}
			List<IJob> jobs = new ArrayList<>(StreamSupport.stream(new ProxyWorker(unit).spliterator(), false)
				.collect(Collectors.toList()));
			while (true) {
				Pair<Integer, @Nullable IJob> chosen = cli.chooseFromList(jobs, "Jobs in workers:",
					"No existing jobs.", "Job to advance: ", false);
				IJob job;
				if (chosen.getValue1() != null) {
					job = chosen.getValue1();
				} else if (chosen.getValue0() <= jobs.size()) { // TODO: invert
					String jobName = cli.inputString("Name of new Job: ");
					if (jobName == null) {
						return;
					}
					for (IWorker worker : workers) {
						model.addJobToWorker(worker, jobName);
					}
					jobs.clear();
					new ProxyWorker(unit).forEach(jobs::add);
					IJob temp = jobs.stream().filter(j -> jobName.equals(j.getName()))
						.findFirst().orElse(null);
					if (temp != null) { // TODO: invert
						job = temp;
					} else {
						cli.println("Select the new item at the next prompt.");
						continue;
					}
				} else {
					break;
				}
				advanceWorkersInJob(job.getName(), workers.toArray(new IWorker[0]));
				Boolean continuation = cli.inputBoolean("Select another Job in these workers?");
				if (continuation == null || !continuation) {
					break;
				}
			}
		}
	}
}
