package controller.map.report;

import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.explorable.AdventureFixture;
import model.map.fixtures.explorable.Battlefield;
import model.map.fixtures.explorable.Cave;
import model.map.fixtures.explorable.ExplorableFixture;
import model.map.fixtures.explorable.Portal;
import model.report.AbstractReportNode;
import model.report.ComplexReportNode;
import model.report.EmptyReportNode;
import model.report.ListReportNode;
import model.report.SectionListReportNode;
import model.report.SimpleReportNode;
import util.DelayedRemovalMap;
import util.NullCleaner;
import util.Pair;

/**
 * A report generator for caves and battlefields.
 *
 * @author Jonathan Lovelace
 */
public class ExplorableReportGenerator extends
		AbstractReportGenerator<ExplorableFixture> {
	/**
	 * A common string in this class.
	 */
	private static final String COLON_COMMA = ": , ";

	/**
	 * Produce the sub-report on non-town things that can be explored. All
	 * fixtures referred to in this report are removed from the collection.
	 *
	 * @param fixtures the set of fixtures
	 * @param map ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report listing things that can be explored.
	 */
	@Override
	public String produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer) {
		// At only three (albeit potentially rather long) list items, I doubt this
		// will ever be over one K ... but we'll give it two just in case.
		final StringBuilder builder = new StringBuilder(2048).append(
				"<h4>Caves, Battlefields, and Portals</h4>\n").append(OPEN_LIST);
		boolean anyCaves = false;
		boolean anyBattles = false;
		boolean anyPortals = false;
		boolean anyAdventures = false;
		// Similarly, I doubt either of these will ever be over half a K, but
		// we'll give each a whole K just in case.
		final StringBuilder caveBuilder = new StringBuilder(1024).append(
				OPEN_LIST_ITEM).append("Caves beneath the following tiles: ");
		final StringBuilder battleBuilder = new StringBuilder(1024).append(
				OPEN_LIST_ITEM).append(
				"Signs of long-ago battles on the following tiles: ");
		final StringBuilder portalBuilder = new StringBuilder(1024)
				.append(OPEN_LIST_ITEM).append("Portals to other worlds: ");
		// I doubt this will ever be over a K either
		final StringBuilder adventureBuilder = new StringBuilder(1024)
				.append("<h4>Possible Adventures</h4>").append(OPEN_LIST);
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Cave) {
				anyCaves = true;
				caveBuilder.append(", ").append(pair.first().toString());
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			} else if (pair.second() instanceof Battlefield) {
				anyBattles = true;
				battleBuilder.append(", ").append(pair.first().toString());
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			} else if (pair.second() instanceof AdventureFixture) {
				anyAdventures = true;
				adventureBuilder.append(OPEN_LIST_ITEM)
						.append(produce(fixtures, map, currentPlayer,
								(ExplorableFixture) pair.second(),
								pair.first()))
						.append(CLOSE_LIST_ITEM);
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			} else if (pair.second() instanceof Portal) {
				anyPortals = true;
				portalBuilder.append(", ").append(pair.first().toString());
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			}
		}
		if (anyCaves) {
			builder.append(caveBuilder.append(CLOSE_LIST_ITEM).toString()
					.replace(COLON_COMMA, ": "));
		}
		if (anyBattles) {
			builder.append(battleBuilder.append(CLOSE_LIST_ITEM).toString()
					.replace(COLON_COMMA, ": "));
		}
		if (anyPortals) {
			builder.append(portalBuilder.append(CLOSE_LIST_ITEM).toString()
					.replace(COLON_COMMA, ": "));
		}
		adventureBuilder.append(CLOSE_LIST);
		builder.append(CLOSE_LIST);
		if (anyCaves || anyBattles || anyPortals) {
			if (anyAdventures) {
				builder.append(adventureBuilder.toString());
			}
			return NullCleaner.assertNotNull(builder.toString()); // NOPMD
		} else {
			if (anyAdventures) {
				return NullCleaner.assertNotNull(adventureBuilder.toString());
			} else {
				return "";
			}
		}
	}

	/**
	 * Produce the sub-report on non-town things that can be explored. All
	 * fixtures referred to in this report are removed from the collection.
	 *
	 * @param fixtures the set of fixtures
	 * @param map ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report listing things that can be explored.
	 */
	@Override
	public AbstractReportNode produceRIR(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer) {
		final AbstractReportNode retval = new SectionListReportNode(4,
				"Caves, Battlefields, and Portals");
		final AbstractReportNode adventures =
				new SectionListReportNode(4, "Possible Adventures");
		final AbstractReportNode caves = new ListReportNode("Caves");
		final AbstractReportNode battles = new ListReportNode("Battlefields");
		final AbstractReportNode portals = new ListReportNode("Portals");
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Cave) {
				caves.add(produceRIR(fixtures, map, currentPlayer,
						(ExplorableFixture) pair.second(), pair.first()));
			} else if (pair.second() instanceof Battlefield) {
				battles.add(produceRIR(fixtures, map, currentPlayer,
						(ExplorableFixture) pair.second(), pair.first()));
			} else if (pair.second() instanceof AdventureFixture) {
				adventures.add(produceRIR(fixtures, map, currentPlayer,
						(ExplorableFixture) pair.second(), pair.first()));
			} else if (pair.second() instanceof Portal) {
				portals.add(produceRIR(fixtures, map, currentPlayer,
						(ExplorableFixture) pair.second(), pair.first()));
			}
		}
		if (caves.getChildCount() > 0) {
			retval.add(caves);
		}
		if (battles.getChildCount() > 0) {
			retval.add(battles);
		}
		if (portals.getChildCount() > 0) {
			retval.add(portals);
		}
		if (retval.getChildCount() > 0) {
			if (adventures.getChildCount() > 0) {
				final AbstractReportNode real = new ComplexReportNode("");
				real.add(retval);
				real.add(adventures);
				return real;
			} else {
				return retval; // NOPMD
			}
		} else if (adventures.getChildCount() > 0) {
			return adventures;
		} else {
			return EmptyReportNode.NULL_NODE;
		}
	}

	/**
	 * Produces a more verbose sub-report on a cave or battlefield.
	 *
	 * @param fixtures
	 *            the set of fixtures.
	 * @param map
	 *            ignored
	 * @param item
	 *            the item to report on
	 * @param loc
	 *            its location
	 * @param currentPlayer
	 *            the player for whom the report is being produced
	 * @return a sub-report (more verbose than the bulk produce() above reports,
	 *         for caves and battlefields) on the item
	 */
	@Override
	public String produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer,
			final ExplorableFixture item, final Point loc) {
		if (item instanceof Cave) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat("Caves beneath ", loc.toString()); // NOPMD
		} else if (item instanceof Battlefield) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat("Signs of a long-ago battle on ", loc.toString()); // NOPMD
		} else if (item instanceof AdventureFixture) {
			if (((AdventureFixture) item).getOwner().isIndependent()) {
				return concat(((AdventureFixture) item).getBriefDescription(),
						" at ", loc.toString(), ": ",
						((AdventureFixture) item).getFullDescription());
			} else if (currentPlayer.equals(((AdventureFixture) item).getOwner())) {
				return concat(((AdventureFixture) item).getBriefDescription(),
						" at ", loc.toString(), ": ",
						((AdventureFixture) item).getFullDescription(),
						" (already investigated by you)");
			} else {
				return concat(((AdventureFixture) item).getBriefDescription(),
						" at ", loc.toString(), ": ",
						((AdventureFixture) item).getFullDescription(),
						" (already investigated by another player)");
			}
		} else if (item instanceof Portal) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat("A portal to another world at ", loc.toString());
		} else {
			throw new IllegalArgumentException("Unexpected ExplorableFixture type");
		}
	}

	/**
	 * Produces a more verbose sub-report on a cave or battlefield.
	 *
	 * @param fixtures the set of fixtures.
	 * @param map ignored
	 * @param item the item to report on
	 * @param loc its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return a sub-report (more verbose than the bulk produce() above reports)
	 *         on the item
	 */
	@Override
	public SimpleReportNode produceRIR(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer,
			final ExplorableFixture item, final Point loc) {
		if (item instanceof Cave) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(loc, "Caves beneath ", loc.toString());
		} else if (item instanceof Battlefield) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(loc, "Signs of a long-ago battle on ",
					loc.toString());
		} else if (item instanceof AdventureFixture) {
			fixtures.remove(Integer.valueOf(item.getID()));
			if (((AdventureFixture) item).getOwner().isIndependent()) {
				return new SimpleReportNode(loc,
						((AdventureFixture) item).getBriefDescription(), " at ",
						loc.toString(),
						((AdventureFixture) item).getFullDescription());
			} else if (currentPlayer.equals(((AdventureFixture) item).getOwner())) {
				return new SimpleReportNode(loc,
						((AdventureFixture) item).getBriefDescription(), " at ",
						loc.toString(),
						((AdventureFixture) item).getFullDescription(),
						" (already investigated by you)");
			} else {
				return new SimpleReportNode(loc,
						((AdventureFixture) item).getBriefDescription(), " at ",
						loc.toString(),
						((AdventureFixture) item).getFullDescription(),
						" (already investigated by another player)");
			}
		} else if (item instanceof Portal) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(loc, "A portal to another world at ",
					loc.toString());
		} else {
			throw new IllegalArgumentException("Unexpected ExplorableFixture type");
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ExplorableReportGenerator";
	}
}
