package controller.map.report;

import static model.map.fixtures.mobile.worker.WorkerStats.getModifierString;
import model.map.IFixture;
import model.map.Player;
import model.map.Point;
import model.map.TileCollection;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import model.map.fixtures.mobile.worker.WorkerStats;
import model.report.AbstractReportNode;
import model.report.ComplexReportNode;
import model.report.EmptyReportNode;
import model.report.ListReportNode;
import model.report.SectionListReportNode;
import model.report.SimpleReportNode;
import util.IntMap;
import util.Pair;

/**
 * A report generator for units.
 *
 * @author Jonathan Lovelace
 *
 */
public class UnitReportGenerator extends AbstractReportGenerator<Unit> {
	/**
	 * We assume we're already in the middle of a paragraph or bullet point.
	 *
	 * @param fixtures the set of fixtures, so we can remove the unit and its
	 *        members from it.
	 * @param tiles ignored
	 * @param unit a unit
	 * @param loc the unit's location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return a sub-report on the unit
	 */
	@Override
	public String produce(final IntMap<Pair<Point, IFixture>> fixtures,
			final TileCollection tiles, final Player currentPlayer,
			final Unit unit, final Point loc) {
		final StringBuilder builder = new StringBuilder();
		builder.append("Unit of type ");
		builder.append(unit.getKind());
		builder.append(", named ");
		builder.append(unit.getName());
		if (unit.getOwner().isIndependent()) {
			builder.append(", independent");
		} else {
			builder.append(", owned by ");
			builder.append(playerNameOrYou(unit.getOwner()));
		}
		boolean hasMembers = false;
		for (final UnitMember member : unit) {
			if (!hasMembers) {
				hasMembers = true;
				builder.append(". Members of the unit:\n<ul>\n");
			}
			builder.append(OPEN_LIST_ITEM);
			if (member instanceof Worker) {
				builder.append(workerReport((Worker) member,
						currentPlayer.equals(unit.getOwner())));
			} else {
				builder.append(member.toString());
			}
			builder.append(CLOSE_LIST_ITEM);
			fixtures.remove(Integer.valueOf(member.getID()));
		}
		if (hasMembers) {
			builder.append(CLOSE_LIST);
		}
		fixtures.remove(Integer.valueOf(unit.getID()));
		final String retval = builder.toString();
		assert retval != null;
		return retval;
	}

	/**
	 * We assume we're already in the middle of a paragraph or bullet point.
	 *
	 * @param fixtures the set of fixtures, so we can remove the unit and its
	 *        members from it.
	 * @param tiles ignored
	 * @param unit a unit
	 * @param loc the unit's location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return a sub-report on the unit
	 */
	@Override
	public AbstractReportNode produceRIR(
			final IntMap<Pair<Point, IFixture>> fixtures,
			final TileCollection tiles, final Player currentPlayer,
			final Unit unit, final Point loc) {
		final String simple = concat("Unit of type ", unit.getKind(),
				", named ", unit.getName(),
				unit.getOwner().isIndependent() ? ", independent"
						: ", owned by " + playerNameOrYou(unit.getOwner()));
		final AbstractReportNode retval = unit.iterator().hasNext() ? new ListReportNode(
				concat(simple, ". Members of the unit:"))
				: new SimpleReportNode(simple);
		for (final UnitMember member : unit) {
			if (member instanceof Worker) {
				retval.add(produceWorkerRIR((Worker) member,
						currentPlayer.equals(unit.getOwner())));
			} else {
				// TODO: what about others?
				retval.add(new SimpleReportNode(member.toString())); // NOPMD
			}
			fixtures.remove(Integer.valueOf(member.getID()));
		}
		fixtures.remove(Integer.valueOf(unit.getID()));
		return retval;
	}

	/**
	 * @param worker a Worker.
	 * @param details whether we should give details of the worker's stats and
	 *        experience---true only if the current player owns the worker.
	 * @return a sub-report on that worker.
	 */
	private static String workerReport(final Worker worker,
			final boolean details) {
		final StringBuilder builder = new StringBuilder();
		builder.append(worker.getName());
		builder.append(", a ");
		builder.append(worker.getRace());
		builder.append(". ");
		final WorkerStats stats = worker.getStats();
		if (stats != null && details) {
			builder.append("He or she has the following stats:").append(
					OPEN_LIST);
			builder.append(OPEN_LIST_ITEM).append("Hit points: ")
					.append(stats.getHitPoints()).append(" / ")
					.append(stats.getMaxHitPoints()).append(CLOSE_LIST_ITEM);
			builder.append(OPEN_LIST_ITEM).append("Strength: ")
					.append(getModifierString(stats.getStrength()))
					.append(CLOSE_LIST_ITEM);
			builder.append(OPEN_LIST_ITEM).append("Dexterity: ")
					.append(getModifierString(stats.getDexterity()))
					.append(CLOSE_LIST_ITEM);
			builder.append(OPEN_LIST_ITEM).append("Constitution: ")
					.append(getModifierString(stats.getConstitution()))
					.append(CLOSE_LIST_ITEM);
			builder.append(OPEN_LIST_ITEM).append("Intelligence: ")
					.append(getModifierString(stats.getIntelligence()))
					.append(CLOSE_LIST_ITEM);
			builder.append(OPEN_LIST_ITEM).append("Wisdom: ")
					.append(getModifierString(stats.getWisdom()))
					.append(CLOSE_LIST_ITEM);
			builder.append(OPEN_LIST_ITEM).append("Charisma: ")
					.append(getModifierString(stats.getCharisma()))
					.append(CLOSE_LIST_ITEM);
			builder.append(CLOSE_LIST);
		}
		if (worker.iterator().hasNext() && details) {
			builder.append(
					"He or she has training or experience in the following Jobs (Skills):\n")
					.append(OPEN_LIST);
			for (final Job job : worker) {
				builder.append(OPEN_LIST_ITEM);
				builder.append(job.getLevel());
				builder.append(" levels in ");
				builder.append(job.getName());
				builder.append(getSkills(job));
				builder.append(CLOSE_LIST_ITEM);
			}
			builder.append(CLOSE_LIST);
		}
		final String retval = builder.toString();
		assert retval != null;
		return retval;
	}

	/**
	 * @param job a Job
	 * @return a String describing its skills.
	 */
	private static String getSkills(final Job job) {
		final StringBuilder builder = new StringBuilder();
		if (job.iterator().hasNext()) {
			boolean first = true;
			for (final Skill skill : job) {
				if (first) {
					builder.append(" (");
					first = false;
				} else {
					builder.append(", ");
				}
				builder.append(skill.getName());
				builder.append(' ');
				builder.append(skill.getLevel());
			}
			builder.append(')');
		}
		final String retval = builder.toString();
		assert retval != null;
		return retval;
	}

	/**
	 * @param worker a Worker.
	 * @param details whether we should give details of the worker's stats and
	 *        experience---true only if the current player owns the worker.
	 * @return a sub-report on that worker.
	 */
	private static AbstractReportNode produceWorkerRIR(final Worker worker,
			final boolean details) {
		final AbstractReportNode retval = new ComplexReportNode(
				worker.getName() + ", a " + worker.getRace() + ". ");
		final WorkerStats stats = worker.getStats();
		if (stats != null && details) {
			final AbstractReportNode statsNode = new ListReportNode(
					"He or she has the following stats:");
			statsNode.add(new StatReportNode(stats.getHitPoints(), stats
					.getMaxHitPoints()));
			statsNode
					.add(new StatReportNode("Strength: ", stats.getStrength()));
			statsNode.add(new StatReportNode("Dexterity: ", stats
					.getDexterity()));
			statsNode.add(new StatReportNode("Constitution: ", stats
					.getConstitution()));
			statsNode.add(new StatReportNode("Intelligence: ", stats
					.getIntelligence()));
			statsNode.add(new StatReportNode("Wisdom: ", stats.getWisdom()));
			statsNode
					.add(new StatReportNode("Charisma: ", stats.getCharisma()));
			retval.add(statsNode);
		}
		if (worker.iterator().hasNext() && details) {
			final AbstractReportNode jobs = new ListReportNode(
					"He or she has training or experience in the following Jobs (Skills):");
			for (final Job job : worker) {
				jobs.add(new SimpleReportNode(Integer.toString(job.getLevel()), // NOPMD
						" levels in ", job.getName(), getSkills(job)));
			}
			retval.add(jobs);
		}
		return retval;
	}

	/**
	 * All fixtures referred to in this report are removed from the collection.
	 *
	 * @param fixtures the set of fixtures
	 * @param tiles ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report dealing with units
	 */
	@Override
	public String produce(final IntMap<Pair<Point, IFixture>> fixtures,
			final TileCollection tiles, final Player currentPlayer) {
		// This can get big; we'll say 8K.
		final StringBuilder builder = new StringBuilder(8192)
				.append("<h4>Units in the map</h4>\n");
		builder.append("<p>(Any units reported above are not described again.)</p>\n");
		builder.append(OPEN_LIST);
		boolean anyUnits = false;
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Unit) {
				anyUnits = true;
				builder.append(OPEN_LIST_ITEM)
						.append(atPoint(pair.first()))
						.append(produce(fixtures, tiles, currentPlayer,
								(Unit) pair.second(), pair.first()))
						.append(CLOSE_LIST_ITEM);
			}
		}
		builder.append(CLOSE_LIST);
		final String retval = builder.toString();
		assert retval != null;
		return anyUnits ? retval : "";
	}

	/**
	 * All fixtures referred to in this report are removed from the collection.
	 *
	 * @param fixtures the set of fixtures
	 * @param tiles ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report dealing with units
	 */
	@Override
	public AbstractReportNode produceRIR(
			final IntMap<Pair<Point, IFixture>> fixtures,
			final TileCollection tiles, final Player currentPlayer) {
		final AbstractReportNode retval = new SectionListReportNode(4,
				"Units in the map",
				"(Any units reported above are not described again.)");
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Unit) {
				final AbstractReportNode unit = produceRIR(fixtures, tiles,
						currentPlayer, (Unit) pair.second(), pair.first());
				unit.setText(concat(atPoint(pair.first()), unit.getText()));
				retval.add(unit);
			}
		}
		return retval.getChildCount() == 0 ? EmptyReportNode.NULL_NODE : retval;
	}

	/**
	 * A {@link SimpleReportNode} with a constructor designed for worker stats.
	 */
	private static class StatReportNode extends SimpleReportNode {
		/**
		 * @param hitPoints the worker's HP
		 * @param max the worker's max HP
		 */
		StatReportNode(final int hitPoints, final int max) {
			super("Hit points: ", Integer.toString(hitPoints), " / ", Integer
					.toString(max));
		}

		/**
		 * @param stat which stat
		 * @param value its value
		 */
		StatReportNode(final String stat, final int value) {
			super(stat, getModifierString(value));
		}
	}
}
