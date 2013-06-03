package controller.map.report;

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
import static model.map.fixtures.mobile.worker.WorkerStats.getModifierString;
import util.IntMap;
import util.Pair;
/**
 * A report generator for units.
 * @author Jonathan Lovelace
 *
 */
public class UnitReportGenerator extends AbstractReportGenerator<Unit> {
	/**
	 * We assume we're already in the middle of a paragraph or bullet point.
	 * @param fixtures the set of fixtures, so we can remove the unit and its members from it.
	 * @param tiles ignored
	 * @param unit a unit
	 * @param loc the unit's location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return a sub-report on the unit
	 */
	@Override
	public String produce(final IntMap<Pair<Point, IFixture>> fixtures,
			final TileCollection tiles, final Player currentPlayer, final Unit unit, final Point loc) {
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
				builder.append("\n<ul>Members of the unit:\n");
			}
			builder.append(OPEN_LIST_ITEM);
			if (member instanceof Worker) {
				builder.append(workerReport((Worker) member));
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
		return builder.toString();
	}
	/**
	 * @param worker a Worker.
	 * @return a sub-report on that worker.
	 */
	private static String workerReport(final Worker worker) {
		final StringBuilder builder = new StringBuilder();
		builder.append(worker.getName());
		builder.append(", a ");
		builder.append(worker.getRace());
		builder.append(". ");
		if (worker.getStats() != null) {
			final WorkerStats stats = worker.getStats();
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
		if (worker.iterator().hasNext()) {
			builder.append(
					"He or she has training or experience in the following Jobs (Skills):\n")
					.append(OPEN_LIST);
			for (final Job job : worker) {
				builder.append(OPEN_LIST_ITEM);
				builder.append(job.getLevel());
				builder.append(" levels in ");
				builder.append(job.getName());
				if (job.iterator().hasNext()) {
					boolean first = true;
					for (final Skill skill : job) {
						// We had written this using an if statement rather than
						// a ternary, but static analysis complained about the
						// block depth ... and I don't want to factor out *yet
						// another function*.
						builder.append(first ? " (" : ", ");
						first = false;
						builder.append(skill.getName());
						builder.append(' ');
						builder.append(skill.getLevel());
					}
					builder.append(')');
				}
				builder.append(CLOSE_LIST_ITEM);
			}
			builder.append(CLOSE_LIST);
		}
		return builder.toString();
	}
	/**
	 * All fixtures referred to in this report are removed from the collection.
	 * @param fixtures the set of fixtures
	 * @param tiles ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report dealing with units
	 */
	@Override
	public String produce(final IntMap<Pair<Point, IFixture>> fixtures, final TileCollection tiles, final Player currentPlayer) {
		final StringBuilder builder = new StringBuilder("<h4>Units in the map</h4>\n");
		builder.append("<p>(Any units reported above are not described again.)</p>\n");
		builder.append(OPEN_LIST);
		boolean anyUnits = false;
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Unit) {
				anyUnits = true;
				builder.append(OPEN_LIST_ITEM).append(atPoint(pair.first()))
						.append(produce(fixtures, tiles, currentPlayer, (Unit) pair.second(), pair.first()))
						.append(CLOSE_LIST_ITEM);
			}
		}
		builder.append(CLOSE_LIST);
		return anyUnits ? builder.toString() : "";
	}
}
