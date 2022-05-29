package common.map.fixtures.mobile.worker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import java.util.function.Consumer;

import java.util.stream.StreamSupport;

import common.map.fixtures.mobile.ProxyFor;

/**
 * An implementation of ISkill whose operations act on multiple workers at once.
 *
 * TODO: Figure out how we can make this satisfy ProxyFor&lt;ISkill&gt;?
 *
 * @deprecated We're trying to get rid of the notion of 'proxies' in favor of
 * driver model methods.
 */
@Deprecated
/* package */ class ProxySkill implements ISkill, ProxyFor<IJob> {
	public ProxySkill(final String name, final boolean parallel, final IJob... proxiedJobs) {
		this.parallel = parallel;
		this.proxiedJobs = new ArrayList<>(Arrays.asList(proxiedJobs));
		this.name = name;
	}

	/**
	 * If false, the worker containing this is representing all the workers
	 * in a single unit; if true, it is representing corresponding workers
	 * in corresponding units in different maps. Thus, if true, we should
	 * use the same "random" seed repeatedly in any given adding-hours
	 * operation, and not if false.
	 *
	 * TODO: We should always probably return true in the API, since this
	 * is a {@link ProxyFor<IJob>}, and use a private variable for the
	 * parallel-or-corresponding-Worker question.
	 */
	private final boolean parallel;

	/**
	 * If false, the worker containing this is representing all the workers
	 * in a single unit; if true, it is representing corresponding workers
	 * in corresponding units in different maps. Thus, if true, we should
	 * use the same "random" seed repeatedly in any given adding-hours
	 * operation, and not if false.
	 *
	 * TODO: We should always probably return true in the API, since this
	 * is a {@link ProxyFor<IJob>}, and use a private variable for the
	 * parallel-or-corresponding-Worker question.
	 */
	@Override
	public boolean isParallel() {
		return parallel;
	}

	private final List<IJob> proxiedJobs;

	/**
	 * The name of the skill we're proxying across workers.
	 */
	private final String name;

	/**
	 * The name of the skill we're proxying across workers.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Clone this object.
	 */
	@Override
	public ISkill copy() {
		return new ProxySkill(name, parallel,
			proxiedJobs.stream().map(IJob::copy).toArray(IJob[]::new));
	}

	/**
	 * The lowest level that any proxied Job has in the skill.
	 */
	@Override
	public int getLevel() {
		return proxiedJobs.stream().flatMap(j -> StreamSupport.stream(j.spliterator(), true))
			.filter(s -> s != this).filter(s -> name.equals(s.getName()))
			.mapToInt(ISkill::getLevel).min().orElse(0);
	}

	/**
	 * The most hours any of the proxied Jobs has for the skill.
	 */
	@Override
	public int getHours() {
		return proxiedJobs.stream().flatMap(j -> StreamSupport.stream(j.spliterator(), true))
			.filter(s -> s != this).filter(s -> name.equals(s.getName()))
			.mapToInt(ISkill::getHours).max().orElse(0);
	}

	/**
	 * The name of the skills.
	 *
	 * TODO: Indicate this is a proxy?
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Add a Job to the list of Jobs we're proxying a skill for.
	 */
	@Override
	public void addProxied(final IJob item) {
		proxiedJobs.add(item);
	}

	/**
	 * Note that this is the *one* place where {@link ProxySkill} should be
	 * a {@link ProxyFor<ISkill>} instead of a {@link ProxyFor<IJob>}.
	 */
	@Override
	public Collection<IJob> getProxied() {
		// Don't return it directly so further changes here won't
		// propagate; in Ceylon we used .sequence().
		return new ArrayList<>(proxiedJobs);
	}

	/**
	 * Whether every proxied Skill is "empty".
	 */
	@Override
	public boolean isEmpty() {
		return proxiedJobs.stream().flatMap(j -> StreamSupport.stream(j.spliterator(), true))
			.filter(s -> s != this).filter(s -> name.equals(s.getName()))
			.allMatch(ISkill::isEmpty);
	}

	@Override
	public boolean isSubset(final ISkill obj, final Consumer<String> report) {
		report.accept("isSubset called on ProxySkill");
		return false;
	}

	@Override
	public boolean equals(final Object that) {
		if (that instanceof ISkill it) {
			return name.equals(it.getName()) &&
				it.getLevel() == getLevel() &&
				it.getHours() == getHours();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
