package common.map.fixtures.mobile.worker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import java.util.function.Consumer;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import common.map.Player;
import common.map.IFixture;
import common.map.fixtures.UnitMember;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.ProxyFor;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.WorkerProxy;
import java.util.logging.Logger;
import common.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.ProxyJob;

import org.jetbrains.annotations.Nullable;

/**
 * An IWorker implementation to make the UI able to operate on all of a unit's workers at once.
 */
public class ProxyWorker implements WorkerProxy {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ProxyWorker.class.getName());

	/**
	 * If false, this is representing all the workers in a single unit; if
	 * true, it is representing the corresponding workers in corresponding
	 * units in different maps.
	 */
	private final boolean parallel;

	/**
	 * If false, this is representing all the workers in a single unit; if
	 * true, it is representing the corresponding workers in corresponding
	 * units in different maps.
	 */
	public boolean isParallel() {
		return parallel;
	}

	/**
	 * The proxy Jobs.
	 */
	// TODO: Extract "IJob&ProxyFor<IJob>" interface?
	private final List<ProxyJob> proxyJobs = new ArrayList<>();

	/**
	 * The jobs we're proxying for.
	 */
	private final Set<String> jobNames = new HashSet<>();

	/**
	 * The workers being proxied.
	 */
	private final List<IWorker> workers = new ArrayList<>();

	/**
	 * Cached stats for the workers. Null if there are no workers being
	 * proxied or if they do not share identical stats.
	 */
	@Nullable
	private WorkerStats statsCache;

	private ProxyWorker(boolean parallelProxy) {
		parallel = parallelProxy;
		statsCache = null;
	}

	public ProxyWorker(IUnit unit) {
		this(false);
		for (UnitMember member : unit) {
			if (member instanceof IWorker) {
				WorkerStats tempStats = ((IWorker) member).getStats();
				WorkerStats priorStats = statsCache;
				if (workers.isEmpty()) {
					statsCache = tempStats;
				} else if (!Objects.equals(tempStats, priorStats)) {
					statsCache = null;
				}
				workers.add((IWorker) member);
				StreamSupport.stream(((IWorker) member).spliterator(), true)
					.map(IJob::getName).forEach(jobNames::add);
			}
		}
		for (String job : jobNames) {
			IWorker[] array = new IWorker[workers.size()];
			proxyJobs.add(new ProxyJob(job, false, workers.toArray(array)));
		}
	}

	public ProxyWorker(IWorker... proxiedWorkers) {
		this(true);
		for (IWorker worker : proxiedWorkers) {
				WorkerStats tempStats = (worker).getStats();
				WorkerStats priorStats = statsCache;
				if (workers.isEmpty()) {
					statsCache = tempStats;
				} else if (!Objects.equals(tempStats, priorStats)) {
					statsCache = null;
				}
				workers.add(worker);
				StreamSupport.stream(worker.spliterator(), true)
					.map(IJob::getName).forEach(jobNames::add);
		}
		for (String job : jobNames) {
			proxyJobs.add(new ProxyJob(job, false, proxiedWorkers));
		}
	}

	@Override
	public IWorker copy(boolean zero) {
		ProxyWorker retval = new ProxyWorker(parallel);
		for (IWorker worker : workers) {
			retval.addProxied(worker.copy(zero));
		}
		return retval;
	}

	@Override
	public int getId() {
		if (parallel) {
			Integer retval = getConsensus(IWorker::getId);
			if (retval == null) {
				return -1;
			} else {
				return retval;
			}
		} else {
			return -1;
		}
	}

	@Override
	public boolean equalsIgnoringID(IFixture fixture) {
		if (fixture instanceof ProxyWorker) {
			return parallel == ((ProxyWorker) fixture).parallel &&
				proxyJobs.equals(((ProxyWorker) fixture).proxyJobs);
		} else {
			return false;
		}
	}

	@Override
	public Iterator<IJob> iterator() {
		return (Iterator<IJob>)(Iterator<? extends IJob>)proxyJobs.iterator();
	}

	@Override
	public boolean isSubset(IFixture obj, Consumer<String> report) {
		report.accept("isSubset called on ProxyWorker");
		return false;
	}

	@Override
	public void addProxied(IWorker item) {
		if (item == this) {
			return;
		}
		WorkerStats tempStats = item.getStats();
		WorkerStats priorStats = statsCache;
		// FIXME: The algorithm here doesn't quite match that of the constructors
		if (workers.isEmpty()) {
			statsCache = tempStats;
		} else if (tempStats != null) {
			if (priorStats != null && !Objects.equals(tempStats, priorStats)) {
				statsCache = null;
			}
		} else if (priorStats != null) {
			statsCache = null;
		}
		workers.add(item);
		for (IJob job : item) {
			String name = job.getName();
			if (jobNames.contains(name)) {
				for (ProxyJob proxyJob : proxyJobs) {
					if (proxyJob.getName().equals(name)) {
						proxyJob.addProxied(job);
					}
				}
			} else {
				jobNames.add(name);
				IWorker[] array = new IWorker[workers.size()];
				proxyJobs.add(new ProxyJob(name, parallel, workers.toArray(array)));
			}
		}
	}

	@Override
	public Iterable<IWorker> getProxied() {
		return new ArrayList<>(workers);
	}

	@Override
	public String getDefaultImage() {
		String retval = null;
		for (IWorker worker : workers) {
			if (retval != null) {
				if (!retval.equals(worker.getDefaultImage())) {
					return "worker.png";
				}
			} else {
				retval = worker.getDefaultImage();
			}
		}
		return (retval == null) ? "worker.png" : retval;
	}

	@Override
	public String getImage() {
		String retval = getConsensus(IWorker::getImage);
		if (retval == null) {
			return "";
		} else {
			return retval;
		}
	}

	@Override
	public String getRace() {
		String retval = getConsensus(IWorker::getRace);
		return (retval == null) ? "proxied" : retval;
	}

	@Override
	public String getName() {
		String retval = getConsensus(IWorker::getName);
		return (retval == null) ? "proxied" : retval;
	}

	@Override
	public String getPortrait() {
		String retval = getConsensus(IWorker::getPortrait);
		return (retval == null) ? "" : retval;
	}

	@Override
	public IJob getJob(String jobName) {
		Optional<ProxyJob> temp =
			proxyJobs.stream().filter(j -> jobName.equals(j.getName())).findAny();
		if (temp.isPresent()) {
			return temp.get();
		}
		IWorker[] array = new IWorker[workers.size()];
		ProxyJob retval = new ProxyJob(jobName, parallel, workers.toArray(array));
		jobNames.add(jobName);
		proxyJobs.add(retval);
		return retval;
	}

	@Override
	@Nullable
	public WorkerStats getStats() {
		return statsCache;
	}

	@Override
	public String getNote(Player player) {
		String retval = getConsensus(worker -> worker.getNote(player));
		return (retval == null) ? "" : retval;
	}

	@Override
	public String getNote(int player) {
		String retval = getConsensus(worker -> worker.getNote(player));
		return (retval == null) ? "" : retval;
	}

	@Override
	public void setNote(Player key, String item) {
		for (IWorker proxy : workers) {
			proxy.setNote(key, item);
		}
	}

	@Override
	public Iterable<Integer> getNotesPlayers() {
		return workers.stream().flatMap(w -> StreamSupport.stream(w.getNotesPlayers().spliterator(),
			true)).collect(Collectors.toSet());
	}
}