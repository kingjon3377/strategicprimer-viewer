package controller.map.report;

import java.util.ArrayList;
import java.util.List;

import model.map.FixtureIterable;
import model.map.IFixture;
import model.map.IMap;
import model.map.Point;
import model.map.TerrainFixture;
import model.map.Tile;
import model.map.TileCollection;
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
		final TileCollection tiles = map.getTiles();
		builder.append(new FortressReportGenerator().produce(fixtures, tiles));
		fixtures.coalesce();
		builder.append(new UnitReportGenerator().produce(fixtures, tiles));
		fixtures.coalesce();
		builder.append(new TownReportGenerator().produce(fixtures, tiles));
		fixtures.coalesce();
		builder.append(new ExplorableReportGenerator().produce(fixtures, tiles));
		fixtures.coalesce();
		builder.append(new HarvestableReportGenerator().produce(fixtures, tiles));
		fixtures.coalesce();
		builder.append(new AnimalReportGenerator().produce(fixtures, tiles));
		fixtures.coalesce();
		builder.append(new VillageReportGenerator().produce(fixtures, tiles));
		fixtures.coalesce();
		builder.append(remainderReport(fixtures));
		fixtures.coalesce();
		builder.append("</body>\n</html>\n");
		return builder.toString();
	}
	/**
	 * TODO: Move to an abstract superclass.
	 * @param point a point
	 * @return the string "At " followed by the point's location
	 */
	private static String atPoint(final Point point) {
		return "At " + point.toString();
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
		boolean any = false;
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			fixtures.remove(Integer.valueOf(pair.second().getID()));
			if (pair.second() instanceof TerrainFixture) {
				continue;
			}
			any = true;
			builder.append(OPEN_LIST_ITEM).append(atPoint(pair.first()))
					.append(", with ID #").append(pair.second().getID())
					.append(": ").append(pair.second().toString())
					.append(CLOSE_LIST_ITEM);
		}
		builder.append(CLOSE_LIST);
		return any ? builder.toString() : "";
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
