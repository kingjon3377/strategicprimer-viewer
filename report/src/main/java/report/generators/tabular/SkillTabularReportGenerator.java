package report.generators.tabular;

import java.util.Comparator;

import org.javatuples.Pair;

import legacy.map.IFixture;
import legacy.map.Point;
import legacy.map.fixtures.mobile.IWorker;
import legacy.map.fixtures.mobile.worker.IJob;
import legacy.map.fixtures.mobile.worker.ISkill;
import lovelace.util.DelayedRemovalMap;
import org.jetbrains.annotations.NotNull;

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
public final class SkillTabularReportGenerator implements ITableGenerator<IWorker> {
	@Override
	public boolean canHandle(final IFixture fixture) {
		return fixture instanceof IWorker;
	}

	/**
	 * For this purpose, compare by worker name only.
	 *
	 * @return the result of the comparison
	 */
	@Override
	public Comparator<Pair<Point, IWorker>> comparePairs() {
		return Comparator.comparing(p -> p.getValue1().getName());
	}

	@Override
	public List<String> getHeaderRow() {
		return Arrays.asList("Worker", "Job", "Skill", "Containing Unit ID #");
	}

	@Override
	public List<List<String>> produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, final IWorker item,
			final int key, final Point loc, final Map<Integer, Integer> parentMap) {
		final List<List<String>> retval = new ArrayList<>();
		final String unitId = Optional.ofNullable(parentMap.get(item.getId()))
				.map(Object::toString).orElse("---");
		for (final IJob job : item) {
			boolean any = false;
			for (final ISkill skill : job) {
				if (!skill.isEmpty()) {
					any = true;
					retval.add(Arrays.asList(item.getName(),
							"%s %d".formatted(job.getName(), job.getLevel()),
							"%s %d".formatted(skill.getName(), skill.getLevel()),
							unitId));
				}
			}
			if (!any && job.getLevel() > 0) {
				retval.add(Arrays.asList(item.getName(),
						"%s %d".formatted(job.getName(), job.getLevel()),
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

	@Override
	public @NotNull Class<IWorker> getTableClass() {
		return IWorker.class;
	}
}
