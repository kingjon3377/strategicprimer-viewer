package controller.map.report;

import java.util.EnumSet;
import java.util.Set;

import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.River;
import model.map.TileFixture;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.towns.Fortress;
import model.report.AbstractReportNode;
import model.report.ComplexReportNode;
import model.report.EmptyReportNode;
import model.report.ListReportNode;
import model.report.SectionListReportNode;
import model.report.SectionReportNode;
import model.report.SimpleReportNode;
import util.DelayedRemovalMap;
import util.NullCleaner;
import util.Pair;

/**
 * A report generator for fortresses.
 *
 * @author Jonathan Lovelace
 */
public class FortressReportGenerator extends AbstractReportGenerator<Fortress> {
	/**
	 * Instance we use.
	 */
	private final UnitReportGenerator urg = new UnitReportGenerator();

	/**
	 * The longest a river report could be.
	 */
	private static final int RIVER_RPT_LEN = ("There is a river on the tile, "
			+ "flowing through the following borders: "
			+ "north, south, east, west").length();
	/**
	 * All fixtures referred to in this report are removed from the collection.
	 *
	 * @param fixtures the set of fixtures
	 * @param currentPlayer the player for whom the report is being produced
	 * @param map the map (needed to get terrain information)
	 * @return the part of the report dealing with fortresses
	 */
	@Override
	public String produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer) {
		// This can get long. We'll give it 16K.
		final StringBuilder ours = new StringBuilder(16384)
				.append("<h4>Your fortresses in the map:</h4>\n");
		boolean anyours = false;
		final StringBuilder builder =
				new StringBuilder(16384)
						.append("<h4>Foreign fortresses in the map:</h4>\n");
		boolean anyforts = false;
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Fortress) {
				final Fortress fort = (Fortress) pair.second();
				if (currentPlayer.equals(fort.getOwner())) {
					anyours = true;
					ours.append(produce(fixtures, map, currentPlayer, fort,
							pair.first()));
				} else {
					anyforts = true;
					builder.append(produce(fixtures, map, currentPlayer,
							fort, pair.first()));
				}
			}
		}
		if (anyours) {
			if (anyforts) {
				ours.append(builder.toString());
			}
			return NullCleaner.assertNotNull(ours.toString()); // NOPMD
		} else if (anyforts) {
			return NullCleaner.assertNotNull(builder.toString()); // NOPMD
		} else {
			return "";
		}
	}

	/**
	 * All fixtures referred to in this report are removed from the collection.
	 *
	 * @param fixtures the set of fixtures
	 * @param currentPlayer the player for whom the report is being produced
	 * @param map the map (needed to get terrain information)
	 * @return the part of the report dealing with fortresses
	 */
	@Override
	public AbstractReportNode produceRIR(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer) {
		final AbstractReportNode retval = new ComplexReportNode("");
		final AbstractReportNode ours = new SectionReportNode(4,
				"Your fortresses in the map:");
		final AbstractReportNode foreign = new SectionReportNode(4,
				"Foreign fortresses in the map:");
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Fortress) {
				final Fortress fort = (Fortress) pair.second();
				if (currentPlayer.equals(fort.getOwner())) {
					ours.add(produceRIR(fixtures, map, currentPlayer,
							(Fortress) pair.second(), pair.first()));
				} else {
					foreign.add(produceRIR(fixtures, map, currentPlayer,
							(Fortress) pair.second(), pair.first()));
				}
			}
		}
		if (ours.getChildCount() != 0) {
			retval.add(ours);
		}
		if (foreign.getChildCount() != 0) {
			retval.add(foreign);
		}
		if (retval.getChildCount() == 0) {
			return EmptyReportNode.NULL_NODE; // NOPMD
		} else {
			return retval;
		}
	}

	/**
	 * @param map the map
	 * @param point a point
	 * @param fixtures the set of fixtures, so we can schedule the removal the
	 *        terrain fixtures from it
	 * @return a String describing the terrain on it
	 */
	private static String getTerrain(final IMapNG map, final Point point,
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures) {
		final StringBuilder builder = new StringBuilder(130).append(
				"Surrounding terrain: ").append(
				map.getBaseTerrain(point).toXML().replace('_', ' '));
		boolean hasForest = false;
		Forest forest = map.getForest(point);
		if (forest != null) {
			builder.append(", forested with ").append(forest.getKind());
			hasForest = true;
		}
		if (map.isMountainous(point)) {
			builder.append(", mountainous");
		}
		for (final TileFixture fix : map.getOtherFixtures(point)) {
			if (fix instanceof Forest) {
				if (!hasForest) {
					hasForest = true;
					builder.append(", forested with ").append(
							((Forest) fix).getKind());
				}
				fixtures.remove(Integer.valueOf(fix.getID()));
			} else if (fix instanceof Mountain) {
				builder.append(", mountainous");
				fixtures.remove(Integer.valueOf(fix.getID()));
			} else if (fix instanceof Hill) {
				builder.append(", hilly");
				fixtures.remove(Integer.valueOf(fix.getID()));
			} else if (fix instanceof Oasis) {
				builder.append(", with a nearby oasis");
				fixtures.remove(Integer.valueOf(fix.getID()));
			}
		}
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * @param rivers a collection of rivers
	 * @return an equivalent string.
	 */
	private static String riversToString(final Set<River> rivers) {
		final StringBuilder builder = new StringBuilder(64);
		if (rivers.contains(River.Lake)) {
			builder.append("<li>There is a nearby lake.</li>\n");
			rivers.remove(River.Lake);
		}
		if (!rivers.isEmpty()) {
			builder.append(OPEN_LIST_ITEM);
			builder.append("There is a river on the tile, "); // NOPMD
			builder.append("flowing through the following borders: ");
			boolean first = true;
			for (final River river : rivers) {
				if (first) {
					first = false;
				} else {
					builder.append(", ");
				}
				builder.append(river.getDescription());
			}
			builder.append(CLOSE_LIST_ITEM);
		}
		return NullCleaner.assertNotNull(builder.toString());
	}
	/**
	 * @param parent the node to add nodes describing rivers to
	 * @param rivers the collection of rivers
	 */
	private static void riversToNode(final AbstractReportNode parent,
			final Set<River> rivers) {
		if (rivers.contains(River.Lake)) {
			parent.add(new SimpleReportNode("There is a nearby lake."));
			rivers.remove(River.Lake);
		}
		if (!rivers.isEmpty()) {
			final StringBuilder builder = new StringBuilder(RIVER_RPT_LEN)
					.append("There is a river on the tile, ");
			builder.append("flowing through the following borders: ");
			boolean first = true;
			for (final River river : rivers) {
				if (first) {
					first = false;
				} else {
					builder.append(", ");
				}
				builder.append(river.getDescription());
			}
			parent.add(new SimpleReportNode(builder.toString()));
		}
	}

	/**
	 * All fixtures referred to in this report are removed from the collection.
	 *
	 * @param item the fortress to report on
	 * @param loc its location
	 * @param fixtures the set of fixtures
	 * @param map the map (needed to get terrain information)
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report dealing with fortresses
	 */
	@Override
	public String produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer,
			final Fortress item, final Point loc) {
		// This can get long. we'll give it 16K.
		final StringBuilder builder = new StringBuilder(16384)
				.append("<h5>Fortress ").append(item.getName())
				.append(" belonging to ")
				.append(playerNameOrYou(item.getOwner())).append("</h5>\n")
				.append(OPEN_LIST).append(OPEN_LIST_ITEM).append("Located at ")
				.append(loc).append(CLOSE_LIST_ITEM).append(OPEN_LIST_ITEM);
		builder.append(getTerrain(map, loc, fixtures)).append(CLOSE_LIST_ITEM);
		if (map.getRivers(loc).iterator().hasNext()) {
			final Set<River> copy = NullCleaner.assertNotNull(EnumSet
					.noneOf(River.class));
			for (final River river : map.getRivers(loc)) {
				copy.add(river);
			}
			builder.append(riversToString(copy));
		}
		if (item.iterator().hasNext()) {
			builder.append(OPEN_LIST_ITEM).append("Units on the tile:\n")
					.append(OPEN_LIST);
			for (final IUnit unit : item) {
				if (unit instanceof Unit) {
					builder.append(OPEN_LIST_ITEM)
							.append(urg.produce(fixtures, map, currentPlayer,
									(Unit) unit, loc)).append(CLOSE_LIST_ITEM);
				}
			}
			builder.append(CLOSE_LIST).append(CLOSE_LIST_ITEM);
		}
		builder.append(CLOSE_LIST);
		fixtures.remove(Integer.valueOf(item.getID()));
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * All fixtures referred to in this report are removed from the collection.
	 *
	 * @param item the fortress to report on
	 * @param loc its location
	 * @param fixtures the set of fixtures
	 * @param map the map (needed to get terrain information)
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report dealing with the fortress
	 */
	@Override
	public SectionListReportNode produceRIR(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer,
			final Fortress item, final Point loc) {
		final SectionListReportNode retval = new SectionListReportNode(5, concat(
				"Fortress ", item.getName(), " belonging to ",
				playerNameOrYou(item.getOwner())));
		retval.add(new SimpleReportNode("Located at ", loc.toString()));
		retval.add(new SimpleReportNode(getTerrain(map, loc, fixtures)));
		if (map.getRivers(loc).iterator().hasNext()) {
			final Set<River> copy = NullCleaner.assertNotNull(EnumSet
					.noneOf(River.class));
			for (final River river : map.getRivers(loc)) {
				copy.add(river);
			}
			riversToNode(retval, copy);
		}
		if (item.iterator().hasNext()) {
			final AbstractReportNode units = new ListReportNode(
					"Units on the tile:");
			for (final IUnit unit : item) {
				if (unit instanceof Unit) {
					units.add(urg.produceRIR(fixtures, map, currentPlayer,
							(Unit) unit, loc));
				}
			}
			retval.add(units);
		}
		fixtures.remove(Integer.valueOf(item.getID()));
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "FortressReportGenerator";
	}
}
