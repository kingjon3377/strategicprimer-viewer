package common.map.fixtures.mobile;

import lovelace.util.ArraySet;
import common.map.IFixture;
import common.map.Player;

import common.map.fixtures.mobile.worker.WorkerStats;
import common.map.fixtures.mobile.worker.Job;
import common.map.fixtures.mobile.worker.IJob;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.Optional;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.Nullable;

/**
 * A worker (or soldier) in a unit. This is deliberately not a
 * {@link strategicprimer.model.common.map.TileFixture TileFixture}: these
 * should only be part of a unit, not as a top-level tag.
 *
 * TODO: Convert some other {@link MobileFixture}s similarly?
 */
public class Worker implements IMutableWorker {
	/**
	 * Whether neither of two collections of Jobs contains a nonempty Job the other does not.
	 */
	private static boolean jobSetsEqual(Iterable<IJob> first, Iterable<IJob> second) {
		Collection<IJob> firstFiltered = StreamSupport.stream(first.spliterator(), true)
			.filter(j -> !j.isEmpty()).collect(Collectors.toList());
		Collection<IJob> secondFiltered = StreamSupport.stream(second.spliterator(), true)
			.filter(j -> !j.isEmpty()).collect(Collectors.toList());
		return firstFiltered.containsAll(secondFiltered) &&
			secondFiltered.containsAll(firstFiltered);
	}

	/**
	 * The set of Jobs the worker is trained or experienced in.
	 */
	private final Set<IJob> jobSet;

	/**
	 * The notes players have associaed with this worker
	 */
	private final Map<Integer, String> notesImpl = new HashMap<>();

	/**
	 * The worker's ID number.
	 */
	private final int id;

	/**
	 * The worker's ID number.
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * The worker's name.
	 */
	private final String name;

	/**
	 * The worker's name.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * The worker's race (elf, dwarf, human, etc.)
	 */
	private final String race;

	/**
	 * The worker's race (elf, dwarf, human, etc.)
	 */
	public String getRace() {
		return race;
	}

	public Worker(String name, String race, int id, IJob... jobs) {
		this.name = name;
		this.race = race;
		this.id = id;
		jobSet = new ArraySet<IJob>(Arrays.asList(jobs));
	}

	/**
	 * The worker's stats.
	 */
	@Nullable
	private WorkerStats stats = null;

	/**
	 * The worker's stats.
	 */
	@Override
	@Nullable
	public WorkerStats getStats() {
		return stats;
	}

	/**
	 * Set the worker's stats.
	 */
	public void setStats(@Nullable WorkerStats stats) {
		this.stats = stats;
	}

	/**
	 * The filename of an image to use as an icon for this instance.
	 */
	private String image = "";

	/**
	 * The filename of an image to use as an icon for this instance.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * Set the filename of an image to use as an icon for this instance.
	 */
	@Override
	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * The filename of an image to use as a portrait for the worker.
	 */
	private String portrait = "";

	/**
	 * The filename of an image to use as a portrait for the worker.
	 */
	@Override
	public String getPortrait() {
		return portrait;
	}

	/**
	 * Set the filename of an image to use as a portrait for the worker.
	 */
	@Override
	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}

	/**
	 * Add a Job.
	 */
	@Override
	public boolean addJob(IJob job) {
		final int size = jobSet.size();
		jobSet.add(job);
		return size != jobSet.size();
	}

	/**
	 * An iterator over the worker's Jobs.
	 */
	@Override
	public Iterator<IJob> iterator() {
		return jobSet.iterator();
	}

	@Override
	public boolean equalsIgnoringID(IFixture fixture) {
		if (fixture instanceof IWorker) {
			return ((IWorker) fixture).getName().equals(name) &&
				jobSetsEqual(jobSet, ((IWorker) fixture)) &&
				((IWorker) fixture).getRace().equals(race) &&
				Objects.equals(stats, ((IWorker) fixture).getStats());
		} else {
			return false;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IWorker) {
			return ((IWorker) obj).getId() == id && equalsIgnoringID((IWorker) obj);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * We only use the worker's name and race for {@link toString}
	 */
	@Override
	public String toString() {
		return ("human".equals(race)) ? name : String.format("%s, a %s", name, race);
	}

	/**
	 * The filename of the icon to use by default. This is just for icons
	 * in lists and such, not the map, since this isn't a {@link
	 * strategicprimer.model.common.map.TileFixture}.
	 */
	@Override
	public String getDefaultImage() {
		return "worker.png";
	}

	/**
	 * A fixture is a subset if it is a worker with the same ID, name,
	 * race, and stats, and no Jobs we don't have, and its Jobs are subsets
	 * of our corresponding Jobs.
	 */
	@Override
	public boolean isSubset(IFixture obj, Consumer<String> report) {
		if (obj.getId() == id) {
			if (obj instanceof IWorker) {
				Consumer<String> localReport =
					s -> report.accept(String.format("In worker %s (ID #%d):\t%s",
						name, id, s));
				if (!name.equals(((IWorker) obj).getName())) {
					localReport.accept("Names differ");
					return false;
				} else if (!race.equals(((IWorker) obj).getRace())) {
					localReport.accept("Races differ");
					return false;
				} else if (!Objects.equals(stats, ((IWorker) obj).getStats())) {
					localReport.accept("Stats differ");
					return false;
				}
				final Map<String, IJob> ours =
					jobSet.stream().collect(Collectors.toMap(IJob::getName,
						Function.identity()));
				boolean retval = true;
				for (IJob job : (IWorker) obj) {
					if (ours.containsKey(job.getName())) {
						if (!ours.get(job.getName()).isSubset(job, localReport)) {
							retval = false;
						}
					} else if (!job.isEmpty()) {
						localReport.accept("Extra Job: " + job.getName());
						retval = false;
					}
				}
				return retval;
			} else {
				report.accept(String.format("For ID #%d, different kinds of members", id));
				return false;
			}
		} else {
			report.accept(String.format("Called with different IDs, #%d and #%d",
				id, obj.getId()));
			return false;
		}
	}

	/**
	 * Clone the object.
	 */
	@Override
	public Worker copy(boolean zero) {
		final Worker retval = new Worker(name, race, id);
		retval.setImage(image);
		if (!zero) {
			WorkerStats localStats = stats;
			if (localStats != null) {
				retval.setStats(localStats.copy());
			}
			for (IJob job : this) {
				if (!job.isEmpty()) {
					retval.addJob(job.copy());
				}
			}
		}
		return retval;
	}

	/**
	 * Get a Job by name: the Job by that name the worker has, or a
	 * newly-constructed one if it didn't have one.
	 */
	@Override
	public IJob getJob(String name) {
		Optional<IJob> maybe = jobSet.stream().filter(j -> name.equals(j.getName())).findAny();
		if (maybe.isPresent()) {
			return maybe.get();
		} else {
			final IJob retval = new Job(name, 0);
			jobSet.add(retval);
			return retval;
		}
	}

	@Override
	public String getNote(Player player) {
		if (notesImpl.containsKey(player.getPlayerId())) {
			return notesImpl.get(player.getPlayerId());
		} else {
			return ""; // TODO: is this right?
		}
	}

	@Override
	public String getNote(int player) {
		if (notesImpl.containsKey(player)) {
			return notesImpl.get(player);
		} else {
			return ""; // TODO: is this right?
		}
	}

	@Override
	public void setNote(Player player, String note) {
		if (note.isEmpty()) {
			notesImpl.remove(player.getPlayerId());
		} else {
			notesImpl.put(player.getPlayerId(), note);
		}
	}

	@Override
	public Iterable<Integer> getNotesPlayers() {
		return notesImpl.keySet();
	}
}