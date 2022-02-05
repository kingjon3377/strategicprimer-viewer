package report.generators.tabular;

import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import common.map.IFixture;
import common.map.Point;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.ISkill;
import lovelace.util.DelayedRemovalMap;
import java.util.List;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Optional;
import java.util.Map;

/**
 * A generator to produce a tabular report of workers' skill improvements.
 * Because {@link WorkerTabularReportGenerator} handles workers, we don't remove
 * anything from the {@link DelayedRemovalMap} we are passed.
 */
public class SkillTabularReportGenerator implements ITableGenerator<IWorker> {
	@Override
	public Class<IWorker> narrowedClass() {
		return IWorker.class;
	}

	/**
	 * For this purpose, compare by worker name only.
	 */
	@Override
	public int comparePairs(final Pair<Point, IWorker> one, final Pair<Point, IWorker> two) {
		return one.getValue1().getName().compareTo(two.getValue1().getName());
	}

	@Override
	public Iterable<String> getHeaderRow() {
		return Arrays.asList("Worker", "Job", "Skill", "Containing Unit ID #");
	}

	@Override
	public Iterable<Iterable<String>> produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, final IWorker item,
			final int key, final Point loc, final Map<Integer, Integer> parentMap) {
		List<Iterable<String>> retval = new ArrayList<>();
		String unitId = Optional.ofNullable(parentMap.get(item.getId()))
			.map(Object::toString).orElse("---");
		for (IJob job : item) {
			boolean any = false;
			for (ISkill skill : job) {
				if (!skill.isEmpty()) {
					any = true;
					retval.add(Arrays.asList(item.getName(),
						String.format("%s %d", job.getName(), job.getLevel()),
						String.format("%s %d", skill.getName(), skill.getLevel()),
						unitId));
				}
			}
			if (!any && job.getLevel() > 0) {
				retval.add(Arrays.asList(item.getName(),
					String.format("%s %d", job.getName(), job.getLevel()),
					"---", unitId));
			}
		}
		// We deliberately do *not* remove the worker from the collection!
		return retval;
	}
	@Override
	public String getTableName() {
		return "skills";
	}
}
