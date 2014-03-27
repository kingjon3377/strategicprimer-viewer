package controller.map.report;

import model.map.IFixture;
import model.map.ITileCollection;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.towns.Village;
import model.report.AbstractReportNode;
import model.report.EmptyReportNode;
import model.report.SectionListReportNode;
import model.report.SectionReportNode;
import model.report.SimpleReportNode;
import util.DelayedRemovalMap;
import util.Pair;

/**
 * A report generator for Villages.
 *
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
	public String produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final ITileCollection tiles, final Player currentPlayer) {
		final HeadedList<String> others = new HtmlList(
				"<h4>Villages you know about:</h4>");
		final HeadedList<String> own = new HtmlList(
				"<h4>Villages pledged to your service:</h4>");
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Village) {
				final Village village = (Village) pair.second();
				// ESCA-JAVA0177:
				final HeadedList<String> appropriateList;
				if (village.getOwner().isCurrent()) {
					appropriateList = own;
				} else {
					appropriateList = others;
				}
				appropriateList.add(produce(fixtures, tiles, currentPlayer,
						(Village) pair.second(), pair.first()));
			}
		}
		// HtmlLists will return the empty string if they are empty.
		return own.toString() + others.toString();
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
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final ITileCollection tiles, final Player currentPlayer) {
		final AbstractReportNode retval = new SectionReportNode(4, "Villages:");
		final AbstractReportNode others = new SectionListReportNode(5,
				"Villages you know about:");
		final AbstractReportNode own = new SectionListReportNode(5,
				"Villages pledged to your service:");
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Village) {
				final AbstractReportNode node;
				if (((Village) pair.second()).getOwner().isCurrent()) {
					node = own;
				} else {
					node = others;
				}
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
		if (retval.getChildCount() == 0) {
			return EmptyReportNode.NULL_NODE;
		} else {
			return retval;
		}
	}

	/**
	 * Produce the (very brief) report for a particular village. We're probably
	 * in the middle of a bulleted list, but we don't assume that.
	 *
	 * @param fixtures the set of fixtures---we remove the specified village
	 *        from it.
	 * @param tiles ignored
	 * @param item the village to report on
	 * @param loc its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the report on the village (its location and name, nothing more)
	 */
	@Override
	public String produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final ITileCollection tiles, final Player currentPlayer,
			final Village item, final Point loc) {
		fixtures.remove(Integer.valueOf(item.getID()));
		if (item.getOwner().isIndependent()) {
			return concat(atPoint(loc), item.getName(), ", a(n) ",
					item.getRace(), " village", ", independent");
		} else {
			return concat(atPoint(loc), item.getName(), ", a(n) ",
					item.getRace(), " village", ", sworn to "
							+ playerNameOrYou(item.getOwner()));
		}
	}

	/**
	 * Produce the (very brief) report for a particular village.
	 *
	 * @param fixtures the set of fixtures---we remove the specified village
	 *        from it.
	 * @param tiles ignored
	 * @param item the village to report on
	 * @param loc its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the report on the village (its location and name, nothing more)
	 */
	@Override
	public AbstractReportNode produceRIR(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final ITileCollection tiles, final Player currentPlayer,
			final Village item, final Point loc) {
		fixtures.remove(Integer.valueOf(item.getID()));
		if (item.getOwner().isIndependent()) {
			return new SimpleReportNode(atPoint(loc), item.getName(),
					", a(n) ", item.getRace(), " village", ", independent");
		} else {
			return new SimpleReportNode(atPoint(loc), item.getName(),
					", a(n) ", item.getRace(), " village", ", sworn to "
							+ playerNameOrYou(item.getOwner()));
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "VillageReportGenerator";
	}
}
