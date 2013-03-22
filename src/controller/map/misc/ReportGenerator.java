package controller.map.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.map.FixtureIterable;
import model.map.IFixture;
import model.map.IMap;
import model.map.Point;
import model.map.River;
import model.map.TerrainFixture;
import model.map.Tile;
import model.map.TileCollection;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import model.map.fixtures.resources.Battlefield;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.resources.Cave;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.HarvestableFixture;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.Shrub;
import model.map.fixtures.resources.StoneDeposit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.towns.AbstractTown;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.Village;
import util.IntMap;
import util.Pair;

/**
 * A class to produce a report based on a map for a player. TODO: Use some sort
 * of IR for lists, producing the empty string if no members, to simplify these
 * methods!
 *
 * @author Jonathan Lovelace
 *
 */
public class ReportGenerator {
	/**
	 * The HTML tag for the end of a bulleted list. Plus a newline.
	 */
	private static final String CLOSE_LIST = "</ul>\n";
	/**
	 * The HTML tag for the start of a bulleted list. Plus a newline, to keep the HTML human-readable.
	 */
	private static final String OPEN_LIST = "<ul>\n";
	/**
	 * The HTML tag for the end of a list item ... plus a newline, to keep the HTML mostly human-readable.
	 */
	private static final String CLOSE_LIST_ITEM = "</li>\n";
	/**
	 * The HTML tag for the start of a list item.
	 */
	private static final String OPEN_LIST_ITEM = "<li>";
	/**
	 * @param map the map to base the report on
	 * @return the report, in HTML, as a String
	 */
	public String createReport(final IMap map) {
		final StringBuilder builder = new StringBuilder("<html>\n");
		builder.append("<head><title>Strategic Primer map summary report</title></head>\n");
		builder.append("<body>");
		final IntMap<Pair<Point, IFixture>> fixtures = getFixtures(map);
		builder.append(fortressReport(fixtures, map.getTiles()));
		fixtures.coalesce();
		builder.append(unitReport(fixtures));
		fixtures.coalesce();
		builder.append(villageReport(fixtures));
		fixtures.coalesce();
		builder.append(townsReport(fixtures));
		fixtures.coalesce();
		builder.append(explorableReport(fixtures));
		fixtures.coalesce();
		builder.append(harvestableReport(fixtures));
		fixtures.coalesce();
		builder.append(remainderReport(fixtures));
		fixtures.coalesce();
		builder.append("</body>\n</html>\n");
		return builder.toString();
	}
	/**
	 * @param point a point
	 * @return the string "At " followed by the point's location
	 */
	private static String atPoint(final Point point) {
		return "At " + point.toString();
	}

	/**
	 * All fixtures referred to in this report are to be removed from the
	 * collection. Caves and battlefields, though HarvestableFixtures, are
	 * presumed to have been handled already.
	 *
	 * @param fixtures the set of fixtures
	 * @return the part of the report listing things that can be harvested.
	 */
	private static String harvestableReport(final IntMap<Pair<Point, IFixture>> fixtures) {
		final StringBuilder builder = new StringBuilder("<h4>Resource Sources</h4>\n");
		final StringBuilder caches = new StringBuilder(
				"<h5>Caches collected by your explorers and workers:</h5>\n")
				.append(OPEN_LIST);
		boolean anyCaches = false;
		final List<Pair<Point, Grove>> groves = new ArrayList<Pair<Point, Grove>>();
		final List<Pair<Point, Meadow>> meadows = new ArrayList<Pair<Point, Meadow>>();
		final List<Pair<Point, Mine>> mines = new ArrayList<Pair<Point, Mine>>();
		final List<Pair<Point, MineralVein>> minerals = new ArrayList<Pair<Point, MineralVein>>();
		final List<Pair<Point, Shrub>> shrubs = new ArrayList<Pair<Point, Shrub>>();
		final List<Pair<Point, StoneDeposit>> stone = new ArrayList<Pair<Point, StoneDeposit>>();
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof HarvestableFixture) {
				final HarvestableFixture harvestable = (HarvestableFixture) pair.second();
				final Point point = pair.first();
				if (harvestable instanceof CacheFixture) {
					anyCaches = true;
					final CacheFixture cache = (CacheFixture) harvestable;
					caches.append(OPEN_LIST_ITEM).append(atPoint(pair.first()))
							.append("A cache of ").append(cache.getKind())
							.append(", containing ")
							.append(cache.getContents())
							.append(CLOSE_LIST_ITEM);
					fixtures.remove(pair);
				} else if (harvestable instanceof Grove) {
					groves.add(Pair.of(point, (Grove) harvestable));
				} else if (harvestable instanceof Meadow) {
					meadows.add(Pair.of(point, (Meadow) harvestable));
				} else if (harvestable instanceof Mine) {
					mines.add(Pair.of(point, (Mine) harvestable));
				} else if (harvestable instanceof MineralVein) {
					minerals.add(Pair.of(point, (MineralVein) harvestable));
				} else if (harvestable instanceof Shrub) {
					shrubs.add(Pair.of(point, (Shrub) harvestable));
				} else if (harvestable instanceof StoneDeposit) {
					stone.add(Pair.of(point, (StoneDeposit) harvestable));
				}
			}
		}
		caches.append(CLOSE_LIST);
		if (anyCaches) {
			builder.append(caches.toString());
		}
		return builder.toString();
	}
	/**
	 * All fixtures referred to in this report are removed from the collection.
	 * @param fixtures the set of fixtures
	 * @return the part of the report listing things that can be explored.
	 */
	private static String explorableReport(final IntMap<Pair<Point, IFixture>> fixtures) {
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
	 * All fixtures referred to in this report are removed from the collection,
	 * just so's it's empty by the end. TODO: Eventually, don't list *everything*.
	 *
	 * @param fixtures the set of fixtures
	 * @return the part of the report listing the (eventually only notable)
	 *         fixtures that remain in the set.
	 */
	private static String remainderReport(final IntMap<Pair<Point, IFixture>> fixtures) {
		final StringBuilder builder = new StringBuilder("<h4>Remaining fixtures:</h4>\n").append(OPEN_LIST);
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			fixtures.remove(Integer.valueOf(pair.second().getID()));
			if (pair.second() instanceof TerrainFixture) {
				continue;
			}
			builder.append(OPEN_LIST_ITEM).append(atPoint(pair.first()))
					.append(", with ID #").append(pair.second().getID())
					.append(": ").append(pair.second().toString())
					.append(CLOSE_LIST_ITEM);
		}
		builder.append(CLOSE_LIST);
		return builder.toString();
	}
	/**
	 * All fixtures referred to in this report are removed from the collection.
	 * TODO: Figure out some way of reporting what was found at any of these.
	 *
	 * @param fixtures the set of fixtures
	 * @return the part of the report dealing with towns, sorted in a way I hope
	 *         is helpful.
	 */
	private static String townsReport(final IntMap<Pair<Point, IFixture>> fixtures) {
		final StringBuilder builder = new StringBuilder(
				"<h4>Cities, towns, and/or fortifications you know about:</h4>\n")
				.append(OPEN_LIST);
		final Map<AbstractTown, Point> townLocs = new HashMap<AbstractTown, Point>();
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof AbstractTown) {
				townLocs.put((AbstractTown) pair.second(), pair.first());
			}
		}
		final List<AbstractTown> sorted = new ArrayList<AbstractTown>(townLocs.keySet());
		Collections.sort(sorted, new TownComparator());
		for (final AbstractTown town : sorted) {
			builder.append(OPEN_LIST_ITEM).append(atPoint(townLocs.get(town)))
					.append(": ").append(town.getName()).append(", a ")
					.append(town.size().toString()).append(' ')
					.append(town.status().toString()).append(' ')
					.append(town.kind().toString()).append(CLOSE_LIST_ITEM);
			fixtures.remove(Integer.valueOf(town.getID()));
		}
		builder.append(CLOSE_LIST);
		return sorted.isEmpty() ? "" : builder.toString();
	}
	/**
	 * All fixtures referred to in this report are removed from the collection.
	 * TODO: add owners to villages, and sort this by owner.
	 * @param fixtures the set of fixtures
	 * @return the part of the report dealing with villages.
	 */
	private static String villageReport(final IntMap<Pair<Point, IFixture>> fixtures) {
		final StringBuilder builder = new StringBuilder("<h4>Villages you know about:</h4>\n").append(OPEN_LIST);
		boolean any = false;
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Village) {
				any = true;
				final Village village = (Village) pair.second();
				builder.append(OPEN_LIST_ITEM).append(atPoint(pair.first()))
						.append(": ").append(village.getName())
						.append(CLOSE_LIST_ITEM);
				fixtures.remove(Integer.valueOf(village.getID()));
			}
		}
		builder.append(CLOSE_LIST);
		return any ? builder.toString() : "";
	}

	/**
	 * All fixtures referred to in this report are removed from the collection.
	 * @param fixtures the set of fixtures
	 * @return the part of the report dealing with units
	 */
	private static String unitReport(final IntMap<Pair<Point, IFixture>> fixtures) {
		final StringBuilder builder = new StringBuilder("<h4>Units in the map</h4>\n");
		builder.append("<p>(Any units reported above are not described again.)</p>\n");
		builder.append(OPEN_LIST);
		boolean anyUnits = false;
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Unit) {
				anyUnits = true;
				builder.append(OPEN_LIST_ITEM).append(atPoint(pair.first()))
						.append(": ")
						.append(unitReport((Unit) pair.second(), fixtures))
						.append(CLOSE_LIST_ITEM);
			}
		}
		builder.append(CLOSE_LIST);
		return anyUnits ? builder.toString() : "";
	}
	/**
	 * All fixtures referred to in this report are removed from the collection.
	 * @param fixtures the set of fixtures
	 * @param tiles the tiles in the map (needed to get terrain information)
	 * @return the part of the report dealing with fortresses
	 */
	private static String fortressReport(
			final IntMap<Pair<Point, IFixture>> fixtures,
			final TileCollection tiles) {
		final StringBuilder builder = new StringBuilder(
				"<h4>Fortresses in the map:</h4>\n");
		boolean anyforts = false;
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if (pair.second() instanceof Fortress) {
				anyforts = true;
				final Fortress fort = (Fortress) pair.second();
				builder.append("<h5>Fortress ").append(fort.getName())
						.append(" belonging to ")
						.append(fort.getOwner().toString()).append("</h5>\n");
				builder.append(OPEN_LIST).append(OPEN_LIST_ITEM);
				final Tile tile = tiles.getTile(pair.first());
				builder.append(getTerrain(tile, fixtures));
				builder.append(CLOSE_LIST_ITEM);
				if (tile.hasRiver()) {
					builder.append(riversToString(EnumSet.copyOf(tile
							.getRivers().getRivers())));
			}
				if (fort.iterator().hasNext()) {
					builder.append("Units on the tile:\n<ul>\n");
					for (final Unit unit : fort) {
						builder.append(OPEN_LIST_ITEM)
								.append(unitReport(unit, fixtures))
								.append(CLOSE_LIST_ITEM);
					}
					builder.append(CLOSE_LIST).append(CLOSE_LIST_ITEM);
				}
				builder.append(CLOSE_LIST);
				fixtures.remove(Integer.valueOf(fort.getID()));
			}
		}
		return anyforts ? builder.toString() : "";
	}
	/**
	 * @param rivers a collection of rivers
	 * @return an equivalent string.
	 */
	private static String riversToString(final Set<River> rivers) {
		final StringBuilder builder =  new StringBuilder();
		if (rivers.contains(River.Lake)) {
			builder.append("<li>There is a nearby lake.</li>\n");
			rivers.remove(River.Lake);
		}
		if (!rivers.isEmpty()) {
			builder.append(OPEN_LIST_ITEM)
					.append("There is a river on the tile, flowing through the following borders: ");
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
		return builder.toString();
	}
	/**
	 * We assume we're already in the middle of a paragraph or bullet point.
	 * @param unit a unit
	 * @param fixtures the set of fixtures, so we can remove the unit and its members from it.
	 * @return a sub-report on the unit
	 */
	private static String unitReport(final Unit unit, final IntMap<Pair<Point, IFixture>> fixtures) {
		final StringBuilder builder = new StringBuilder();
		builder.append("Unit of type ");
		builder.append(unit.getKind());
		builder.append(", named ");
		builder.append(unit.getName());
		if ("independent".equalsIgnoreCase(unit.getOwner().getName())) {
			builder.append(", independent");
		} else {
			builder.append(", owned by ");
			builder.append(unit.getOwner().toString());
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
		if (worker.iterator().hasNext()) {
			builder.append(", with training or experience in the following Jobs (Skill levels in parentheses):\n<ul>\n");
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
		}
		return builder.toString();
	}
	/**
	 * @param tile a tile
	 * @param fixtures the set of fixtures, so we can schedule the removal the terrain fixtures from it
	 * @return a String describing the terrain on it
	 */
	private static String getTerrain(final Tile tile, final IntMap<Pair<Point, IFixture>> fixtures) {
		final StringBuilder builder = new StringBuilder("Surrounding terrain: ")
				.append(tile.getTerrain().toXML().replace('_', ' '));
		boolean hasForest = false;
		for (final TileFixture fix : tile) {
			if (fix instanceof Forest) {
				if (!hasForest) {
					hasForest = true;
					builder.append(", forested with " + ((Forest) fix).getKind());
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
		return builder.toString();
	}
	/**
	 * @param map a map
	 * @return the fixtures in it, a mapping from their ID to a Pair of the fixture's location and the fixture itself.
	 */
	private static IntMap<Pair<Point, IFixture>> getFixtures(final IMap map) {
		final IntMap<Pair<Point, IFixture>> retval = new IntMap<Pair<Point, IFixture>>();
		for (final Point point : map.getTiles()) {
			final Tile tile = map.getTile(point);
			for (final IFixture fix : getFixtures(tile)) {
				if (fix.getID() >= 0) {
					retval.put(Integer.valueOf(fix.getID()), Pair.of(point, fix));
				}
			}
		}
		return retval;
	}
	/**
	 * @param iter a source of tile-fixtures
	 * @return all the tile-fixtures in it, recursively.
	 */
	private static List<IFixture> getFixtures(
			final Iterable<? extends IFixture> iter) {
		final List<IFixture> retval = new ArrayList<IFixture>();
		for (final IFixture fix : iter) {
			retval.add(fix);
			if (fix instanceof FixtureIterable) {
				retval.addAll(getFixtures((FixtureIterable<?>) fix));
			}
		}
		return retval;
	}
}
