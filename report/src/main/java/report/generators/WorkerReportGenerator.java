package report.generators;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import lovelace.util.ThrowingConsumer;
import java.io.IOException;
import java.util.Comparator;
import lovelace.util.DelayedRemovalMap;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import java.util.List;

import common.map.Player;
import common.map.IFixture;
import common.map.Point;
import common.map.MapDimensions;
import common.map.IMapNG;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.worker.WorkerStats;
import common.map.fixtures.mobile.worker.ISkill;
import common.map.fixtures.mobile.worker.IJob;

/**
 * A report generator for Workers.
 */
/* package */ class WorkerReportGenerator extends AbstractReportGenerator<IWorker> {

	private final boolean details;
	private final Player currentPlayer;

	public WorkerReportGenerator(final Comparator<Pair<Point, IFixture>> comp, final boolean details,
	                             final MapDimensions dimensions, final Player currentPlayer) {
		this(comp, details, dimensions, currentPlayer, null);
	}

	public WorkerReportGenerator(final Comparator<Pair<Point, IFixture>> comp, final boolean details,
	                             final MapDimensions dimensions, final Player currentPlayer, @Nullable final Point hq) {
		super(comp, dimensions, hq);
		this.details = details;
		this.currentPlayer = currentPlayer;
	}

	private static String mod(final int stat) {
		return WorkerStats.getModifierString(stat);
	}

	/**
	 * Produce the sub-sub-report on a worker's stats.
	 * TODO: Take ThrowingConsumer instead of returning String
	 */
	private static String statsString(final WorkerStats stats) {
		return String.format("He or she has the following stats: %d / %d Hit Points, Strength %s, Dexterity %s, Constitution %s, Intelligence %s, Wisdom %s, Charisma %s",
			stats.getHitPoints(), stats.getMaxHitPoints(), mod(stats.getStrength()),
			mod(stats.getDexterity()), mod(stats.getConstitution()),
			mod(stats.getIntelligence()), mod(stats.getWisdom()),
			mod(stats.getCharisma()));
	}

	// TODO: take ThrowingConsumer instead of returning String
	private static String skillString(final ISkill skill) {
		return skill.getName() + " " + Integer.toString(skill.getLevel());
	}

	/**
	 * Produce text describing the given Skills.
	 * TODO: Take ThrowingConsumer instead of returning String?
	 */
	private String skills(final Iterable<ISkill> job) {
		return (job.iterator().hasNext()) ? StreamSupport.stream(job.spliterator(), false)
			.map(WorkerReportGenerator::skillString)
			.collect(Collectors.joining(", ", "(", ")")) : "";
	}

	/**
	 * Produce a sub-sub-report on a worker (we assume we're already in the
	 * middle of a paragraph or bullet point).
	 */
	@Override
	public void produceSingle(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                          final IMapNG map, final ThrowingConsumer<String, IOException> ostream, final IWorker worker, final Point loc)
			throws IOException {
		ostream.accept(worker.getName());
		ostream.accept(", a ");
		ostream.accept(worker.getRace());
		ostream.accept(".");
		if (details && worker.getStats() != null) {
			ostream.accept(System.lineSeparator());
			ostream.accept("<p>");
			ostream.accept(statsString(worker.getStats()));
			ostream.accept("</p>");
			ostream.accept(System.lineSeparator());
		}
		// FIXME: IWorker.isEmpty() just refers to jobs, right?
		if (details && worker.iterator().hasNext()) {
			ostream.accept(System.lineSeparator());
			ostream.accept("(S)he has training or experience in the following Jobs (Skills):");
			ostream.accept(System.lineSeparator());
			ostream.accept("<ul>");
			ostream.accept(System.lineSeparator());
			for (IJob job : worker) {
				ostream.accept("<li>");
				ostream.accept(Integer.toString(job.getLevel()));
				ostream.accept(" levels in ");
				ostream.accept(job.getName());
				ostream.accept(" ");
				ostream.accept(skills(job));
				ostream.accept("</li>");
				ostream.accept(System.lineSeparator());
			}
			ostream.accept("</ul>");
			ostream.accept(System.lineSeparator());
		}
		if (details && !worker.getNote(currentPlayer).isEmpty()) {
			ostream.accept("<p>");
			ostream.accept(worker.getNote(currentPlayer));
			ostream.accept("</p>");
			ostream.accept(System.lineSeparator());
		}
	}

	/**
	 * Produce a sub-sub-report on all workers. This should never be
	 * called, but we'll implement it properly anyway.
	 */
	@Override
	public void produce(final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
	                    final IMapNG map, final ThrowingConsumer<String, IOException> ostream) throws IOException {
		List<Pair<IWorker, Point>> workers = fixtures.values().stream()
			.filter(p -> p.getValue1() instanceof IWorker)
			.sorted(pairComparator)
			.map(p -> Pair.with((IWorker) p.getValue1(), p.getValue0()))
			.collect(Collectors.toList());
		if (!workers.isEmpty()) {
			ostream.accept("<h5>Workers</h5>");
			ostream.accept(System.lineSeparator());
			ostream.accept("<ul>");
			ostream.accept(System.lineSeparator());
			for (Pair<IWorker, Point> pair : workers) {
				ostream.accept("<li>");
				produceSingle(fixtures, map, ostream, pair.getValue0(), pair.getValue1());
				ostream.accept("</li>");
				ostream.accept(System.lineSeparator());
			}
			ostream.accept("</ul>");
			ostream.accept(System.lineSeparator());
		}
	}
}
