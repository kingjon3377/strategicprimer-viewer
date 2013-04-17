package controller.map.report;

import util.IntMap;
import util.Pair;
import model.map.IFixture;
import model.map.Player;
import model.map.Point;
import model.map.TileCollection;
import model.map.fixtures.resources.Battlefield;
import model.map.fixtures.resources.Cave;
import model.map.fixtures.resources.HarvestableFixture;
/**
 * A report generator for caves and battlefields.
 * @author Jonathan Lovelace
 */
public class ExplorableReportGenerator extends
		AbstractReportGenerator<HarvestableFixture> {
	/**
	 * Produce the sub-report on non-town things that can be explored.
	 * All fixtures referred to in this report are removed from the collection.
	 * @param fixtures the set of fixtures
	 * @param tiles ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report listing things that can be explored.
	 */
	@Override
	public String produce(final IntMap<Pair<Point, IFixture>> fixtures,
			final TileCollection tiles, final Player currentPlayer) {
		final StringBuilder builder = new StringBuilder("<h4>Caves and Battlefields</h4>\n").append(OPEN_LIST);
		boolean anyCaves = false;
		boolean anyBattles = false;
		final StringBuilder caveBuilder = new StringBuilder(OPEN_LIST_ITEM)
				.append("Caves beneath the following tiles: ");
		final StringBuilder battleBuilder = new StringBuilder(OPEN_LIST_ITEM)
				.append("Signs of long-ago battles on the following tiles: ");
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Cave) {
				anyCaves = true;
				caveBuilder.append(", ").append(pair.first().toString());
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			} else if (pair.second() instanceof Battlefield) {
				anyBattles = true;
				battleBuilder.append(", ").append(pair.first().toString());
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			}
		}
		if (anyCaves) {
			builder.append(caveBuilder.append(CLOSE_LIST_ITEM).toString().replace(": , ", ": "));
		}
		if (anyBattles) {
			builder.append(battleBuilder.append(CLOSE_LIST_ITEM).toString().replace(": , ", ": "));
		}
		builder.append(CLOSE_LIST);
		return anyCaves || anyBattles ? builder.toString() : "";
	}
	/**
	 * Produces a more verbose sub-report on a cave or battlefield.
	 * TODO: Delegate other Harvestables to the dedicated class rather than skipping.
	 * @param fixtures the set of fixtures.
	 * @param tiles ignored
	 * @param item the item to report on
	 * @param loc its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return a sub-report (more verbose than the bulk produce() above reports) on the item
	 */
	@Override
	public String produce(final IntMap<Pair<Point, IFixture>> fixtures,
			final TileCollection tiles, final Player currentPlayer, final HarvestableFixture item, final Point loc) {
		if (item instanceof Cave) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new StringBuilder("Caves beneath ").append(loc.toString())// NOPMD
					.toString();
		} else if (item instanceof Battlefield) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new StringBuilder("Signs of a long-ago battle on ").append(// NOPMD
					loc.toString()).toString();
		} else {
			return ""; // TODO: Delegate to a HarvestableReportGenerator class.
		}
	}

}
