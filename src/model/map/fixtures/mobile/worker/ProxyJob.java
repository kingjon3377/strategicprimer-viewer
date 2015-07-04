package model.map.fixtures.mobile.worker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import model.map.fixtures.mobile.Worker;
import util.NullCleaner;
/**
 * An IJob implementation to let the Job tree operate on a whole unit at once.
 * @author Jonathan Lovelace
 *
 */
public class ProxyJob implements IJob {
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
	private final List<Job> proxiedJobs = new ArrayList<>();
	/**
	 * @param nomen the name of the Job
	 * @param workers being proxied
	 */
	public ProxyJob(final String nomen, final Worker... workers) {
		name = nomen;
		final Set<String> skills = new HashSet<>();
		for (final Worker worker : workers) {
			boolean touched = false;
			for (final IJob job : worker) {
				if (job instanceof ProxyJob || job == null) {
					continue;
				} else if (nomen.equals(job.getName())) {
					proxiedJobs.add((Job) job);
					for (final ISkill skill : job) {
						skills.add(skill.getName());
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
		final Job[] jobsArray =
				NullCleaner.assertNotNull(proxiedJobs
						.toArray(new Job[proxiedJobs.size()]));
		for (final String skill : skills) {
			if (skill != null) {
				proxied.add(new ProxySkill(skill, jobsArray));
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
		proxied.add(new ProxySkill(skill.getName(),
				NullCleaner.assertNotNull(proxiedJobs
						.toArray(new Job[proxiedJobs.size()]))));
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
}
