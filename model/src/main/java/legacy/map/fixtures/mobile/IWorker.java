package legacy.map.fixtures.mobile;

import legacy.map.HasImage;
import legacy.map.HasName;
import legacy.map.HasNotes;
import legacy.map.HasKind;
import legacy.map.HasPortrait;
import legacy.map.fixtures.Implement;
import legacy.map.fixtures.UnitMember;
import legacy.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.WorkerStats;

import java.util.Collection;

import org.jspecify.annotations.Nullable;

/**
 * An interface for Workers.
 */
public interface IWorker extends UnitMember, Iterable<IJob>, HasName, HasKind, HasImage,
		HasNotes, HasPortrait {
	/**
	 * The worker's race.
	 */
	String getRace();

	/**
	 * An alias for (alternate method of querying) the worker's race.
	 */
	@Override
	default String getKind() {
		return getRace();
	}

	/**
	 * The worker's stats.
	 */
	@Nullable
	WorkerStats getStats();

	/**
	 * Clone the object.
	 */
	@Override
	IWorker copy(CopyBehavior zero);

	/**
	 * Get the Job that the worker has with the given name, or a
	 * newly-constructed one if it didn't have one before.
	 */
	IJob getJob(String jobName);

	@Override
	default String getPlural() {
		return "Workers";
	}

	/**
	 * The animal, if any, this worker is personally mounted on.
	 */
	@Nullable
	Animal getMount();

	/**
	 * Any equipment associated with this worker personally. (Generally not much more than clothing.)
	 */
	Collection<Implement> getEquipment();
}
