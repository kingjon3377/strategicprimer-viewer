package controller.map.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.map.misc.TownComparator;
import util.IntMap;
import util.Pair;
import model.map.IFixture;
import model.map.Player;
import model.map.Point;
import model.map.TileCollection;
import model.map.fixtures.towns.AbstractTown;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.ITownFixture;
import model.map.fixtures.towns.Village;
import model.report.AbstractReportNode;
import model.report.SectionListReportNode;
import model.report.SimpleReportNode;
/**
 * A report generator for towns.
 * @author Jonathan Lovelace
 *
 */
public class TownReportGenerator extends AbstractReportGenerator<ITownFixture> {
	/**
	 * Produce the part of the report dealing with towns. Note that while this
	 * class specifies {@link ITownFixture}, this method ignores
	 * {@link Fortress}es and {@link Village}s. All fixtures referred to in this
	 * report are removed from the collection.
	 *
	 * TODO: Figure out some way to report what was found at any of the towns.
	 *
	 * @param fixtures the set of fixtures
	 * @param tiles ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report dealing with towns, sorted in a way I hope
	 *         is helpful.
	 */
	@Override
	public String produce(final IntMap<Pair<Point, IFixture>> fixtures,
			final TileCollection tiles, final Player currentPlayer) {
		final Map<AbstractTown, Point> townLocs = new HashMap<>();
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof AbstractTown) {
				townLocs.put((AbstractTown) pair.second(), pair.first());
			}
		}
		final List<AbstractTown> sorted = new ArrayList<>(townLocs.keySet());
		Collections.sort(sorted, new TownComparator());
		final int len = 80 + sorted.size() * 512;
		final StringBuilder builder = new StringBuilder(len)
				.append("<h4>Cities, towns, and/or fortifications you know about:</h4>\n")
				.append(OPEN_LIST);
		for (final AbstractTown town : sorted) {
			builder.append(OPEN_LIST_ITEM)
					.append(produce(fixtures, tiles, currentPlayer, town, townLocs.get(town)))
					.append(CLOSE_LIST_ITEM);
		}
		builder.append(CLOSE_LIST);
		return sorted.isEmpty() ? "" : builder.toString();
	}
	/**
	 * Produce the part of the report dealing with towns. Note that while this
	 * class specifies {@link ITownFixture}, this method ignores
	 * {@link Fortress}es and {@link Village}s. All fixtures referred to in this
	 * report are removed from the collection.
	 *
	 * TODO: Figure out some way to report what was found at any of the towns.
	 *
	 * @param fixtures the set of fixtures
	 * @param tiles ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report dealing with towns, sorted in a way I hope
	 *         is helpful.
	 */
	@Override
	public AbstractReportNode produceRIR(
			final IntMap<Pair<Point, IFixture>> fixtures, final TileCollection tiles,
			final Player currentPlayer) {
		final AbstractReportNode retval = new SectionListReportNode(4,
				"Cities, towns, and/or fortifications you know about:");
		final Map<AbstractTown, Point> townLocs = new HashMap<>();
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof AbstractTown) {
				townLocs.put((AbstractTown) pair.second(), pair.first());
			}
		}
		final List<AbstractTown> sorted = new ArrayList<>(townLocs.keySet());
		Collections.sort(sorted, new TownComparator());
		for (final AbstractTown town : sorted) {
			retval.add(produceRIR(fixtures, tiles, currentPlayer, town, townLocs.get(town)));
		}
		return sorted.isEmpty() ? null : retval;
	}
	/**
	 * Produce a report for a town. Handling of fortresses and villages is
	 * delegated to their dedicated report-generating classes. We remove the town from the set of fixtures.
	 *
	 * @param fixtures the set of fixtures
	 * @param tiles ignored
	 * @param item the town to report on
	 * @param loc its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the sub-report dealing with the town.
	 */
	@Override
	public String produce(final IntMap<Pair<Point, IFixture>> fixtures,
			final TileCollection tiles, final Player currentPlayer, final ITownFixture item, final Point loc) {
		if (item instanceof Village) {
			return new VillageReportGenerator().produce(fixtures, tiles, currentPlayer, (Village) item, loc); // NOPMD
		} else if (item instanceof Fortress) {
			return new FortressReportGenerator().produce(fixtures, tiles, currentPlayer, (Fortress) item, loc); // NOPMD
		} else if (item instanceof AbstractTown) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat(atPoint(loc), item.getName(), item.getOwner()
					.isIndependent() ? ", an independent " : ", a ", item
					.size().toString(), " ", item.status().toString(), " ",
					((AbstractTown) item).kind().toString(), item.getOwner()
							.isIndependent() ? "" : " allied with "
							+ playerNameOrYou(item.getOwner()));
		} else {
			throw new IllegalStateException("Unhandled ITownFixture subclass");
		}
	}
	/**
	 * Produce a report for a town. Handling of fortresses and villages is
	 * delegated to their dedicated report-generating classes. We remove the town from the set of fixtures.
	 *
	 * @param fixtures the set of fixtures
	 * @param tiles ignored
	 * @param item the town to report on
	 * @param loc its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the sub-report dealing with the town.
	 */
	@Override
	public AbstractReportNode produceRIR(
			final IntMap<Pair<Point, IFixture>> fixtures, final TileCollection tiles,
			final Player currentPlayer, final ITownFixture item, final Point loc) {
		if (item instanceof Village) {
			return new VillageReportGenerator().produceRIR(fixtures, tiles, currentPlayer, (Village) item, loc); // NOPMD
		} else if (item instanceof Fortress) {
			return new FortressReportGenerator().produceRIR(fixtures, tiles, currentPlayer, (Fortress) item, loc); // NOPMD
		} else if (item instanceof AbstractTown) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(atPoint(loc), item.getName(), item
					.getOwner().isIndependent() ? ", an independent " : ", a ",
					item.size().toString(), " ", item.status().toString(), " ",
					((AbstractTown) item).kind().toString(), item.getOwner()
							.isIndependent() ? "" : " allied with "
							+ playerNameOrYou(item.getOwner()));
		} else {
			throw new IllegalStateException("Unhandled ITownFixture subclass");
		}
	}

}
