package drivers.advancement;

import drivers.common.cli.ICLIHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lovelace.util.SingletonRandom;

import legacy.map.fixtures.mobile.IWorker;
import legacy.map.fixtures.mobile.IUnit;

import legacy.map.fixtures.mobile.worker.IJob;
import legacy.map.fixtures.mobile.worker.ISkill;
import legacy.map.fixtures.mobile.worker.Skill;

import drivers.common.IAdvancementModel;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

/**
 * Logic extracted from the advancement CLI to also use in the turn-running CLI as well.
 */
public final class AdvancementCLIHelperImpl implements AdvancementCLIHelper {
	public AdvancementCLIHelperImpl(final IAdvancementModel model, final ICLIHelper cli) {
		this.cli = cli;
		this.model = model;
	}

	private final IAdvancementModel model;
	private final ICLIHelper cli;

	private final Collection<LevelGainListener> levelListeners = new ArrayList<>();

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
		for (final LevelGainListener listener : levelListeners) {
			listener.level(workerName, jobName, skillName, gains, currentLevel);
		}
	}

	/**
	 * Let the user add hours to a Skill or Skills in a Job.
	 *
	 * TODO: Avoid Boolean parameters
	 */
	private void advanceJob(final IWorker worker, final IJob job, final boolean allowExpertMentoring) {
		final List<ISkill> skills = StreamSupport.stream(job.spliterator(), false).collect(Collectors.toList());
		final Consumer<ISkill> addToList = skills::add;
		while (true) {
			final Pair<Integer, @Nullable ISkill> chosen = cli.chooseFromList(skills, "Skills in Job:",
					"No existing Skills.", "Skill to advance: ", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT);
			final ISkill skill;
			if (!Objects.isNull(chosen.getValue1())) {
				skill = chosen.getValue1();
			} else if (chosen.getValue0() <= skills.size()) {
				final String skillName = cli.inputString("Name of new Skill: ");
				if (Objects.isNull(skillName)) {
					return;
				} else {
					model.addSkillToWorker(worker, job.getName(), skillName);
					skills.clear();
					job.forEach(addToList);
					final ISkill temp = skills.stream().filter(s -> skillName.equals(s.getName()))
							.findFirst().orElse(null);
					if (Objects.isNull(temp)) {
						cli.println("Select the new item at the next prompt.");
						continue;
					} else {
						skill = temp;
					}
				}
			} else {
				break;
			}
			final int oldLevel = skill.getLevel();
			final int hours = Optional.ofNullable(cli.inputNumber("Hours of experience to add: ")).orElse(0);
			if (allowExpertMentoring) {
				final int hoursPerHour = Optional.ofNullable(cli.inputNumber("'Hours' between hourly checks: "))
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
				final String count = (skill.getLevel() - oldLevel == 1) ? "a level"
						: (skill.getLevel() - oldLevel) + " levels";
				cli.printf("%s gained %s in %s%n", worker.getName(), count, skill.getName());
				fireLevelEvent(worker.getName(), job.getName(), skill.getName(), skill.getLevel() - oldLevel,
						skill.getLevel());
			}
			final Boolean continuation = cli.inputBoolean("Select another Skill in this Job?");
			if (!Boolean.TRUE.equals(continuation)) {
				break;
			}
		}
	}

	/**
	 * Let the user add experience to a worker.
	 */
	private void advanceSingleWorker(final IWorker worker, final boolean allowExpertMentoring) {
		final List<IJob> jobs = StreamSupport.stream(worker.spliterator(), false).collect(Collectors.toList());
		final Consumer<IJob> addToList = jobs::add;
		while (true) {
			final Pair<Integer, @Nullable IJob> chosen = cli.chooseFromList(jobs, "Jobs in worker:",
					"No existing Jobs.", "Job to advance: ", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT);
			final IJob job;
			if (!Objects.isNull(chosen.getValue1())) {
				job = chosen.getValue1();
			} else if (chosen.getValue0() <= jobs.size()) {
				final String jobName = cli.inputString("Name of new Job: ");
				if (Objects.isNull(jobName)) {
					return;
				}
				model.addJobToWorker(worker, jobName);
				jobs.clear();
				worker.forEach(addToList);
				final IJob temp = jobs.stream().filter(j -> jobName.equals(j.getName()))
						.findFirst().orElse(null);
				if (Objects.isNull(temp)) {
					cli.println("Select the new item at the next prompt.");
					continue;
				} else {
					job = temp;
				}
			} else {
				break;
			}
			advanceJob(worker, job, allowExpertMentoring);
			final Boolean continuation = cli.inputBoolean("Select another Job in this worker?");
			if (!Boolean.TRUE.equals(continuation)) {
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
		final int hours = Optional.ofNullable(cli.inputNumber("Hours of experience to add: ")).orElse(0);
		if (hours <= 0) {
			return;
		}
		for (final IWorker worker : workers) {
			final IJob job = worker.getJob(jobName);
			final ISkill skill = job.getSkill(skillName);
			final int oldLevel = skill.getLevel();
			model.addHoursToSkill(worker, jobName, skillName, hours,
					SingletonRandom.SINGLETON_RANDOM.nextInt(100));
			if (skill.getLevel() != oldLevel) {
				if (oldLevel == 0 && "miscellaneous".equals(skill.getName())) {
					final Boolean chooseAnother = cli.inputBooleanInSeries(
							"%s gained %d level(s) in miscellaneous, choose another skill?".formatted(
							worker.getName(), skill.getLevel()), "misc-replacement");
					if (Objects.isNull(chooseAnother)) {
						return;
					} else if (!chooseAnother) {
						continue;
					}
					final Collection<String> gains = new ArrayList<>();
					for (int i = 0; i < skill.getLevel(); i++) {
						final ISkill replacement;
						final Pair<Integer, @Nullable ISkill> choice = cli.chooseFromList(
								StreamSupport.stream(job.spliterator(), false)
										.filter(s -> !"miscellaneous".equals(s.getName()))
										.collect(Collectors.toList()),
								"Skill to gain level in:", "No other skill", "Chosen skill:",
								ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT);
						if (!Objects.isNull(choice.getValue1())) {
							replacement = choice.getValue1();
							model.replaceSkillInJob(worker, job.getName(), skill, replacement);
							model.addHoursToSkill(worker, job.getName(), replacement.getName(),
									100, 0);
							// FIXME: Add to {@link gains}? The listener should cover that
							// already, but in practice it doesn't.
							continue;
						}
						final String replacementName = cli.inputString("Skill to gain level in: ");
						if (Objects.isNull(replacementName)) {
							return;
						}
						replacement = new Skill(replacementName, 1, 0);
						model.replaceSkillInJob(worker, job.getName(), skill, replacement);
						gains.add(replacementName);
					}
					final Map<String, Long> frequencies = gains.stream()
							.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
					for (final Map.Entry<String, Long> entry : frequencies.entrySet()) {
						final String name = entry.getKey();
						final long count = entry.getValue();
						if (count == 1L) {
							cli.printf("%s gained a level in %s%n", worker.getName(), name);
						} else {
							cli.printf("%s gained %d levels in %s%n", worker.getName(), count, name);
						}
						fireLevelEvent(worker.getName(), job.getName(), name, (int) count,
								job.getSkill(name).getLevel());
					}
				} else {
					cli.printf("%s gained a level in %s%n", worker.getName(), skill.getName());
					fireLevelEvent(worker.getName(), job.getName(), skill.getName(),
							skill.getLevel() - oldLevel, skill.getLevel());
				}
			}
		}
	}

	/**
	 * Let the user add experience in a given Job to all of a list of workers.
	 */
	private void advanceWorkersInJob(final String jobName, final IUnit unit) {
		final List<String> skills = unit.stream().filter(IWorker.class::isInstance).map(IWorker.class::cast)
				.flatMap(w -> StreamSupport.stream(w.getJob(jobName).spliterator(), false)).map(ISkill::getName)
				.distinct().collect(Collectors.toList());
		final Consumer<String> addToList = skills::add;
		final Predicate<Object> isWorker = IWorker.class::isInstance;
		final Function<Object, IWorker> workerCast = IWorker.class::cast;
		while (true) {
			final Pair<Integer, @Nullable String> chosen = cli.chooseStringFromList(skills, "Skills in Jobs:",
					"No existing skills.", "Skill to advance: ", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT);
			final String skill;
			if (!Objects.isNull(chosen.getValue1())) {
				skill = chosen.getValue1();
			} else if (chosen.getValue0() > skills.size()) {
				break;
			} else {
				final String skillName = cli.inputString("Name of the new skill: ");
				if (Objects.isNull(skillName)) {
					return;
				}
				model.addSkillToAllWorkers(unit, jobName, skillName);
				skills.clear();
				unit.stream().filter(isWorker).map(workerCast)
						.flatMap(w -> StreamSupport.stream(w.getJob(jobName).spliterator(), false)).map(ISkill::getName)
						.distinct().forEach(addToList);
				skill = skillName;
			}
			advanceWorkersInSkill(jobName, skill, unit.stream().filter(isWorker).map(workerCast)
					.toArray(IWorker[]::new));
			final Boolean continuation = cli.inputBoolean("Select another Skill in this Job?");
			if (!Boolean.TRUE.equals(continuation)) {
				break;
			}
		}
	}

	/**
	 * Let the user add experience to a worker or workers in a unit.
	 */
	@Override
	public void advanceWorkersInUnit(final IUnit unit, final boolean allowExpertMentoring) {
		final List<IWorker> workers = unit.stream()
				.filter(IWorker.class::isInstance).map(IWorker.class::cast).collect(Collectors.toList());
		final Boolean individualAdvancement = cli.inputBooleanInSeries("Add experience to workers individually? ");
		final Predicate<Object> isWorker = IWorker.class::isInstance;
		final Function<Object, IWorker> workerCast = IWorker.class::cast;
		if (Objects.isNull(individualAdvancement)) {
			return;
		} else if (individualAdvancement) {
			while (!workers.isEmpty()) {
				final IWorker chosen = cli.chooseFromList((List<? extends IWorker>) workers, "Workers in unit:",
						"No unadvanced workers remain.", "Chosen worker: ",
						ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
				if (Objects.isNull(chosen)) {
					break;
				}
				workers.remove(chosen);
				advanceSingleWorker(chosen, allowExpertMentoring);
				final Boolean continuation = cli.inputBoolean("Choose another worker?");
				if (!Boolean.TRUE.equals(continuation)) {
					break;
				}
			}
		} else {
			if (workers.isEmpty()) {
				cli.println("No workers in unit.");
				return;
			}
			final List<String> jobs = unit.stream().filter(isWorker).map(workerCast)
					.flatMap(w -> StreamSupport.stream(w.spliterator(), false)).map(IJob::getName).distinct()
					.collect(Collectors.toList());
			final Consumer<String> addJob = jobs::add;
			while (true) {
				final Pair<Integer, @Nullable String> chosen = cli.chooseStringFromList(jobs, "Jobs in workers:",
						"No existing jobs.", "Job to advance: ", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT);
				final String job;
				if (!Objects.isNull(chosen.getValue1())) {
					job = chosen.getValue1();
				} else if (chosen.getValue0() > jobs.size()) {
					break;
				} else {
					final String jobName = cli.inputString("Name of new Job: ");
					if (Objects.isNull(jobName)) {
						return;
					}
					for (final IWorker worker : workers) {
						model.addJobToWorker(worker, jobName);
					}
					jobs.clear();
					job = jobName;
					unit.stream().filter(isWorker).map(workerCast)
							.flatMap(w -> StreamSupport.stream(w.spliterator(), false)).map(IJob::getName)
							.distinct().forEach(addJob);
				}
				advanceWorkersInJob(job, unit);
				final Boolean continuation = cli.inputBoolean("Select another Job in these workers?");
				if (!Boolean.TRUE.equals(continuation)) {
					break;
				}
			}
		}
	}
}
