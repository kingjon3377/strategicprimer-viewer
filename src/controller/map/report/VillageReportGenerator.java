package controller.map.report;

import model.map.IFixture;
import model.map.Player;
import model.map.Point;
import model.map.TileCollection;
import model.map.fixtures.towns.Village;
import model.report.AbstractReportNode;
import model.report.SectionListReportNode;
import model.report.SectionReportNode;
import model.report.SimpleReportNode;
import util.IntMap;
import util.Pair;
/**
 * A report generator for Villages.
 * @author Jonathan Lovelace
 *
 */
public class VillageReportGenerator extends AbstractReportGenerator<Village> {
	/**
	 * Produce the report on all villages. All fixtures referred to in this
	 * report are removed from the collection. TODO: sort this by owner.
	 *
	 * @param fixtures the set of fixtures
	 * @return the part of the report dealing with villages.
	 * @param currentPlayer the player for whom the report is being produced
	 * @param tiles ignored
	 */
	@Override
	public String produce(final IntMap<Pair<Point, IFixture>> fixtures,
			final TileCollection tiles, final Player currentPlayer) {
		// TODO: Move HtmlList to AbstractReportGenerator and use it here.
		final StringBuilder builderOthers = new StringBuilder(1024).append(
				"<h4>Villages you know about:</h4>\n").append(OPEN_LIST);
		boolean anyOthers = false;
		final StringBuilder builderOwn = new StringBuilder(1024).append(
				"<h4>Villages pledged to your service:</h4>\n").append(
				OPEN_LIST);
		boolean anyOwn = false;
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Village) {
				final Village village = (Village) pair.second();
				// ESCA-JAVA0177:
				final StringBuilder appropriateBuilder; // NOPMD
				if (village.getOwner().isCurrent()) {
					anyOwn = true;
					appropriateBuilder = builderOwn;
				} else {
					anyOthers = true;
					appropriateBuilder = builderOthers;
				}
				appropriateBuilder.append(OPEN_LIST_ITEM)
						.append(produce(fixtures, tiles, currentPlayer,
								(Village) pair.second(), pair.first()))
						.append(CLOSE_LIST_ITEM);
			}
		}
		builderOthers.append(CLOSE_LIST);
		builderOwn.append(CLOSE_LIST);
		return (anyOwn ? builderOwn.toString() : "") + (anyOthers ? builderOthers.toString() : "");
	}
	/**
	 * Produce the report on all villages. All fixtures referred to in this
	 * report are removed from the collection. TODO: sort this by owner.
	 *
	 * @param fixtures the set of fixtures
	 * @return the part of the report dealing with villages.
	 * @param currentPlayer the player for whom the report is being produced
	 * @param tiles ignored
	 */
	@Override
	public AbstractReportNode produceRIR(
			final IntMap<Pair<Point, IFixture>> fixtures, final TileCollection tiles,
			final Player currentPlayer) {
		final AbstractReportNode retval = new SectionReportNode(4, "Villages:");
		final AbstractReportNode others = new SectionListReportNode(5, "Villages you know about:");
		final AbstractReportNode own = new SectionListReportNode(5, "Villages pledged to your service:");
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Village) {
				final AbstractReportNode node = ((Village) pair.second())
						.getOwner().isCurrent() ? own : others;
				node.add(produceRIR(fixtures, tiles, currentPlayer,
						(Village) pair.second(), pair.first()));
			}
		}
		if (own.getChildCount() != 0) {
			retval.add(own);
		}
		if (others.getChildCount() != 0) {
			retval.add(others);
		}
		return retval.getChildCount() == 0 ? null : retval;
	}
	/**
	 * Produce the (very brief) report for a particular village. We're probably
	 * in the middle of a bulleted list, but we don't assume that.
	 *
	 * @param fixtures the set of fixtures---we remove the specified village from it.
	 * @param tiles ignored
	 * @param item the village to report on
	 * @param loc its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the report on the village (its location and name, nothing more)
	 */
	@Override
	public String produce(final IntMap<Pair<Point, IFixture>> fixtures,
			final TileCollection tiles, final Player currentPlayer, final Village item, final Point loc) {
		fixtures.remove(Integer.valueOf(item.getID()));
		return concat(atPoint(loc), item.getName(), ", a(n) ", item.getRace(),
				" village", item.getOwner().isIndependent() ? ", independent"
						: ", sworn to " + playerNameOrYou(item.getOwner()));
	}
	/**
	 * Produce the (very brief) report for a particular village.
	 *
	 * @param fixtures the set of fixtures---we remove the specified village from it.
	 * @param tiles ignored
	 * @param item the village to report on
	 * @param loc its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the report on the village (its location and name, nothing more)
	 */
	@Override
	public AbstractReportNode produceRIR(
			final IntMap<Pair<Point, IFixture>> fixtures, final TileCollection tiles,
			final Player currentPlayer, final Village item, final Point loc) {
		fixtures.remove(Integer.valueOf(item.getID()));
		return new SimpleReportNode(atPoint(loc), item.getName(), ", a(n) ",
				item.getRace(), " village",
				item.getOwner().isIndependent() ? ", independent"
						: ", sworn to " + playerNameOrYou(item.getOwner()));
	}

}
