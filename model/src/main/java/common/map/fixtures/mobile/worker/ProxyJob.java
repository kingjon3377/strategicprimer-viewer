package common.map.fixtures.mobile.worker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import java.util.function.Consumer;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import common.map.fixtures.mobile.IMutableWorker;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.ProxyFor;

import java.util.logging.Logger;

/**
 * An IJob implementation to let the Job tree operate on a whole unit at once.
 */
public final class ProxyJob implements IJob, ProxyFor<IJob> {
	private static final Logger LOGGER = Logger.getLogger(ProxyJob.class.getName());

	public ProxyJob(String name, boolean parallel, IWorker... proxiedWorkers) {
		this.parallel = parallel;
		this.name = name;

		for (IWorker worker : proxiedWorkers) {
			boolean unmodified = true;
			for (IJob job : worker) {
				if (name.equals(job.getName())) {
					proxiedJobs.add(job);
					StreamSupport.stream(job.spliterator(), false)
						.map(ISkill::getName).forEach(skillNames::add);
					unmodified = false;
				}
			}
			if (unmodified) { // TODO: Reduce block depth by combining conditions, then repeating this condition in else block
				if (worker instanceof IMutableWorker) { // FIXME: This can't still be needed, can it?
					final IMutableJob job = new Job(name, 0);
					((IMutableWorker) worker).addJob(job);
					proxiedJobs.add(StreamSupport.stream(worker.spliterator(), false)
						.filter(j -> name.equals(j.getName()))
						.findAny().orElse(job));
				} else {
					LOGGER.warning("Can't add job to immutable worker");
				}
			}
		}

		proxiedSkills = new ArrayList<ProxySkill>(skillNames.stream().map(this::proxyForSkill)
			.collect(Collectors.toList()));
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

	private ProxySkill proxyForSkill(String skill) {
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
		for (IJob job : proxiedJobs) {
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
	 * Delegates to {@link getName}.
	 *
	 * TODO: Indicate we're a proxy?
	 */
	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean isSubset(IJob obj, Consumer<String> report) {
		report.accept("\tisSubset called on ProxyJob");
		return false;
	}

	/**
	 * Proxy an additional Job.
	 */
	@Override
	public void addProxied(IJob item) {
		if (item == this || !name.equals(item.getName())) {
			return;
		}
		proxiedJobs.add(item);
		for (ProxyFor<IJob> skill : proxiedSkills) {
			skill.addProxied(item);
		}
		IJob[] proxiedJobsArray = new IJob[proxiedJobs.size()];
		proxiedJobsArray = proxiedJobs.toArray(proxiedJobsArray);
		for (ISkill skill : item) {
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
	public Iterable<IJob> getProxied() {
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
	public ISkill getSkill(String skillName) {
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