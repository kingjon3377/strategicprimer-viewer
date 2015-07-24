package model.map.fixtures.mobile.worker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.ProxyFor;
import util.NullCleaner;
/**
 * An IJob implementation to let the Job tree operate on a whole unit at once.
 * @author Jonathan Lovelace
 *
 */
public class ProxyJob implements IJob, ProxyFor<IJob> {
	/**
	 * If false, the worker containing this is representing all the workers in a
	 * single unit; if true, it is representing corresponding workers in
	 * corresponding units in different maps. Thus, if true, we should use the
	 * same "random" seed repeatedly in any given adding-hours operation, and
	 * not if false.
	 */
	private final boolean parallel;
	/**
	 * The name of the Job.
	 */
	private String name;
	/**
	 * Proxy-skills.
	 */
	private final List<ISkill> proxied = new ArrayList<>();
	/**
	 * Jobs we're proxying.
	 */
	private final List<IJob> proxiedJobs = new ArrayList<>();
	/**
	 * The names of skills we're proxying.
	 */
	private Set<String> skillNames;
	/**
	 * @param nomen the name of the Job
	 * @param parall whether the workers containing these jobs are corresponding workers in different maps (if true) or workers in the same unit (if false)
	 * @param workers being proxied
	 */
	public ProxyJob(final String nomen, final boolean parall, final IWorker... workers) {
		parallel = parall;
		name = nomen;
		skillNames = new HashSet<>();
		for (final IWorker worker : workers) {
			boolean touched = false;
			for (final IJob job : worker) {
				if (job == this || job == null) {
					continue;
				} else if (nomen.equals(job.getName())) {
					proxiedJobs.add(job);
					for (final ISkill skill : job) {
						skillNames.add(skill.getName());
					}
					touched = true;
				}
			}
			if (!touched) {
				final Job job = new Job(nomen, 0);
				proxiedJobs.add(job);
				worker.addJob(job);
			}
		}
		final IJob[] jobsArray =
				NullCleaner.assertNotNull(proxiedJobs
						.toArray(new IJob[proxiedJobs.size()]));
		for (final String skill : skillNames) {
			if (skill != null) {
				proxied.add(new ProxySkill(skill, parallel, jobsArray));
			}
		}
	}
	/**
	 * @return the name of the Job.
	 */
	@Override
	public String getName() {
		return name;
	}
	/**
	 * @param nomen the new name
	 */
	@Override
	public void setName(final String nomen) {
		name = nomen;
	}
	/**
	 * @return an iterator over the skills that any of the proxied jobs has.
	 */
	@Override
	public Iterator<ISkill> iterator() {
		return NullCleaner.assertNotNull(proxied.iterator());
	}
	/**
	 * @param skill a skill to proxy
	 * @return true if we weren't proxying that skill already
	 */
	@Override
	public boolean addSkill(final ISkill skill) {
		for (final ISkill proxy : proxied) {
			if (proxy != null && proxy.getName().equals(skill.getName())) {
				return false;
			}
		}
		proxied.add(new ProxySkill(skill.getName(), parallel,
				NullCleaner.assertNotNull(proxiedJobs
						.toArray(new IJob[proxiedJobs.size()]))));
		return true;
	}
	/**
	 * @return 0
	 */
	@Override
	public int getLevel() {
		return 0;
	}
	/**
	 * @return a String representation of the Job.
	 */
	@Override
	public String toString() {
		return getName();
	}
	/**
	 * @param obj ignored
	 * @param ostream a stream to report this call on
	 * @param context
	 *            a string to print before every line of output, describing the
	 *            context
	 * @return false
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(final IJob obj, final Appendable ostream,
			final String context) throws IOException {
		ostream.append(context);
		ostream.append("\tisSubset called on ProxyJob\n");
		return false;
	}
	/**
	 * Proxy an additional Job.
	 * @param item the job to proxy for
	 */
	@Override
	public void addProxied(final IJob item) {
		if (item == this || !name.equals(item.getName())) {
			return;
		}
		proxiedJobs.add(item);
		for (ISkill skill : proxied) {
			((ProxySkill) skill).addProxied(item);
		}
		final Job[] jobsArray =
				NullCleaner.assertNotNull(proxiedJobs
						.toArray(new Job[proxiedJobs.size()]));
		for (ISkill skill : item) {
			if (!skillNames.contains(skill.getName())) {
				proxied.add(new ProxySkill(skill.getName(), parallel, jobsArray));
			}
		}
	}
	/**
	 * @return the proxied Jobs.
	 */
	@Override
	public Iterable<IJob> getProxied() {
		return proxiedJobs;
	}
}
