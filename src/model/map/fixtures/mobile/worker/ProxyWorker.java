package model.map.fixtures.mobile.worker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import model.map.IFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.Worker;
import util.NullCleaner;

/**
 * An IWorker implementation to make the UI able to operate on all of a unit's
 * workers at once.
 *
 * @author Jonathan Lovelace
 *
 */
public class ProxyWorker implements IWorker {
	/**
	 * The unit we're proxying for.
	 */
	private final IUnit proxied;
	/**
	 * The proxy Jobs.
	 */
	private List<IJob> proxyJobs = new ArrayList<>();
	/**
	 * The jobs we're proxying for.
	 */
	private Set<String> jobNames;
	/**
	 * The workers in the unit.
	 */
	private List<Worker> workers;
	/**
	 * @param unit the unit to proxy for
	 */
	public ProxyWorker(final IUnit unit) {
		proxied = unit;
		workers = new ArrayList<>();
		jobNames = new HashSet<>();
		for (final UnitMember member : unit) {
			if (member instanceof Worker) {
				workers.add((Worker) member);
				for (final IJob job : (Worker) member) {
					jobNames.add(job.getName());
				}
			}
		}
		final Worker[] workerArray =
				NullCleaner.assertNotNull(workers.toArray(new Worker[workers
						.size()]));
		for (final String job : jobNames) {
			if (job != null) {
				proxyJobs.add(new ProxyJob(job, workerArray));
			}
		}
	}
	/**
	 * @return -1, since this isn't a valid fixture.
	 */
	@Override
	public int getID() {
		return -1;
	}
	/**
	 * @param fix a fixture
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof ProxyWorker
				&& ((ProxyWorker) fix).proxied.equals(proxied);
	}

	@Override
	public Iterator<IJob> iterator() {
		return NullCleaner.assertNotNull(proxyJobs.iterator());
	}
	/**
	 * @param job the job to add
	 * @return true if we weren't already proxying for it
	 */
	@Override
	public boolean addJob(final IJob job) {
		if (jobNames.contains(job.getName())) {
			return false;
		} else {
			final ProxyJob proxy =
					new ProxyJob(job.getName(),
							NullCleaner.assertNotNull(workers
									.toArray(new Worker[workers.size()])));
			jobNames.add(proxy.getName());
			proxyJobs.add(proxy);
			return true;
		}
	}
	/**
	 * @param obj ignored
	 * @param ostream a stream to report the call on
	 * @return false
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Appendable ostream)
			throws IOException {
		ostream.append("isSubset called on ProxyWorker\n");
		return false;
	}

}
