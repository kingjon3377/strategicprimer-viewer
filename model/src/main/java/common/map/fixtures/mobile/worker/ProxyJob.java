package common.map.fixtures.mobile.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import java.util.function.Consumer;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import common.map.fixtures.mobile.IMutableWorker;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.ProxyFor;

import lovelace.util.LovelaceLogger;

/**
 * An IJob implementation to let the Job tree operate on a whole unit at once.
 *
 * @deprecated We're trying to get rid of the notion of 'proxies' in favor of
 * driver model methods.
 */
@Deprecated
public final class ProxyJob implements IJob, ProxyFor<IJob> {
	public ProxyJob(final String name, final boolean parallel, final IWorker... proxiedWorkers) {
		this.parallel = parallel;
		this.name = name;

        final Consumer<String> addSkillName = skillNames::add;
        final Predicate<IJob> isMatchingJob = j -> name.equals(j.getName());
		for (final IWorker worker : proxiedWorkers) {
			boolean unmodified = true;
			for (final IJob job : worker) {
				if (name.equals(job.getName())) {
					proxiedJobs.add(job);
					StreamSupport.stream(job.spliterator(), false)
						.map(ISkill::getName).forEach(addSkillName);
					unmodified = false;
				}
			}
			if (unmodified && worker instanceof IMutableWorker mw) { // FIXME: This can't still be needed, can it?
				final IMutableJob job = new Job(name, 0);
				mw.addJob(job);
				proxiedJobs.add(StreamSupport.stream(worker.spliterator(), false)
					.filter(isMatchingJob)
					.findAny().orElse(job));
			} else if (unmodified) {
				LovelaceLogger.warning("Can't add job to immutable worker");
				LovelaceLogger.trace(new Exception(), "Stack trace for immutable-worker condition");
			}
		}

		proxiedSkills = skillNames.stream().map(this::proxyForSkill).collect(Collectors.toList());
	}

	/**
	 * If false, the worker containing this is representing all the workers
	 * in a single unit; if true, it is representing corresponding workers
	 * in corresponding units in different maps. Thus, if true, we should
	 * use the same "random" seed repeatedly in any given adding-hours
	 * operation, and not if false.
	 */
	private final boolean parallel;

	/**
	 * If false, the worker containing this is representing all the workers
	 * in a single unit; if true, it is representing corresponding workers
	 * in corresponding units in different maps. Thus, if true, we should
	 * use the same "random" seed repeatedly in any given adding-hours
	 * operation, and not if false.
	 */
	@Override
	public boolean isParallel() {
		return parallel;
	}

	/**
	 * Jobs we're proxying.
	 */
	private final List<IJob> proxiedJobs = new ArrayList<>();

	/**
	 * The names of skills we're proxying.
	 */
	private final Set<String> skillNames = new HashSet<>();

	/**
	 * The name of the Job.
	 */
	private final String name;

	/**
	 * The name of the Job.
	 */
	@Override
	public String getName() {
		return name;
	}

	private ProxySkill proxyForSkill(final String skill) {
		final IJob[] temp = new IJob[proxiedJobs.size()];
		return new ProxySkill(skill, parallel, proxiedJobs.toArray(temp));
	}

	/**
	 * Proxy-skills.
	 */
	// TODO: Extract a marker interface that extends both ISkill and ProxyFor<IJob>?
	private final List<ProxySkill> proxiedSkills;

	@Override
	public IJob copy() {
		final ProxyJob retval = new ProxyJob(name, parallel);
		for (final IJob job : proxiedJobs) {
			retval.addProxied(job.copy());
		}
		return retval;
	}

	@Override
	public Iterator<ISkill> iterator() {
		return (Iterator<ISkill>)(Iterator<? extends ISkill>) proxiedSkills.iterator();
	}

	/**
	 * The lowest level among proxied jobs.
	 */
	@Override
	public int getLevel() {
		return proxiedJobs.stream().mapToInt(IJob::getLevel).min().orElse(0);
	}

	/**
	 * Delegates to {@link #getName}.
	 *
	 * TODO: Indicate we're a proxy?
	 */
	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean isSubset(final IJob obj, final Consumer<String> report) {
		report.accept("\tisSubset called on ProxyJob");
		return false;
	}

	/**
	 * Proxy an additional Job.
	 */
	@Override
	public void addProxied(final IJob item) {
		if (item == this || !name.equals(item.getName())) {
			return;
		}
		proxiedJobs.add(item);
		for (final ProxyFor<IJob> skill : proxiedSkills) {
			skill.addProxied(item);
		}
		IJob[] proxiedJobsArray = new IJob[proxiedJobs.size()];
		proxiedJobsArray = proxiedJobs.toArray(proxiedJobsArray);
		for (final ISkill skill : item) {
			if (!skillNames.contains(skill.getName())) {
				proxiedSkills.add(new ProxySkill(skill.getName(), parallel,
					proxiedJobsArray));
			}
		}
	}

	/**
	 * A view of the proxied Jobs.
	 */
	@Override
	public Collection<IJob> getProxied() {
		return new ArrayList<>(proxiedJobs);
	}

	/**
	 * Whether all of the Jobs this is a proxy for are "empty," ie having
	 * no levels and containing no Skills that report either levels or
	 * hours of experience.
	 */
	@Override
	public boolean isEmpty() {
		return proxiedJobs.stream().allMatch(IJob::isEmpty);
	}

	/**
	 * Get a Skill by name.
	 */
	@Override
	public ISkill getSkill(final String skillName) {
		final Optional<ProxySkill> matching = proxiedSkills.stream()
			.filter(s -> skillName.equals(s.getName())).findAny();
		if (matching.isPresent()) {
			return matching.get();
		}
		final IJob[] temp = new IJob[proxiedJobs.size()];
		final ProxySkill retval = new ProxySkill(skillName, parallel, proxiedJobs.toArray(temp));
		proxiedSkills.add(retval);
		return retval;
	}
}
