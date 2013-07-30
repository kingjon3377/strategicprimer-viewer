package controller.map.report;

import java.util.ArrayList;
import java.util.List;

import model.map.FixtureIterable;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.IMap;
import model.map.Player;
import model.map.Point;
import model.map.Tile;
import model.map.TileCollection;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Fortress;
import util.IntMap;
import util.Pair;

/**
 * A class to produce a report based on a map for a player.
 *
 * TODO: Use an IR for lists, producing "" if empty, to simplify these methods!
 *
 * @author Jonathan Lovelace
 *
 */
public final class ReportGenerator {
	/**
	 * No non-static members anymore.
	 */
	private ReportGenerator() {
		// So don't instantiate.
	}
	/**
	 * @param map the map to base the report on
	 * @return the report, in HTML, as a String
	 */
	public static String createReport(final IMap map) {
		final StringBuilder builder = new StringBuilder("<html>\n");
		builder.append("<head><title>Strategic Primer map summary report</title></head>\n");
		builder.append("<body>");
		final IntMap<Pair<Point, IFixture>> fixtures = getFixtures(map);
		final TileCollection tiles = map.getTiles();
		final Player player = map.getPlayers().getCurrentPlayer();
		builder.append(new FortressReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append(new UnitReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append(new TownReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append(new ExplorableReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append(new HarvestableReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append(new AnimalReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append(new VillageReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append(new ImmortalsReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append("</body>\n</html>\n");
		return builder.toString();
	}
	/**
	 * Creates a slightly abbreviated report, omitting the player's fortresses and units.
	 * @param map the map to base the report on
	 * @return the report, in HTML, as a string.
	 * @param player the player to report on
	 */
	public static String createAbbreviatedReport(final IMap map, final Player player) {
		final StringBuilder builder = new StringBuilder("<html>\n");
		builder.append("<head><title>Strategic Primer map summary abbreviated report</title></head>\n");
		builder.append("<body>");
		final IntMap<Pair<Point, IFixture>> fixtures = getFixtures(map);
		final TileCollection tiles = map.getTiles();
		for (final Pair<Point, IFixture> pair : fixtures.values()) {
			if ((pair.second() instanceof Unit || pair.second() instanceof Fortress)
					&& player.equals(((HasOwner) pair.second()).getOwner())) {
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			}
		}
		fixtures.coalesce();
		builder.append(new FortressReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append(new UnitReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append(new TownReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append(new ExplorableReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append(new HarvestableReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append(new AnimalReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append(new VillageReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append(new ImmortalsReportGenerator().produce(fixtures, tiles, player));
		fixtures.coalesce();
		builder.append("</body>\n</html>\n");
		return builder.toString();
	}
	/**
	 * @param map a map
	 * @return the fixtures in it, a mapping from their ID to a Pair of the fixture's location and the fixture itself.
	 */
	private static IntMap<Pair<Point, IFixture>> getFixtures(final IMap map) {
		final IntMap<Pair<Point, IFixture>> retval = new IntMap<>();
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
		final List<IFixture> retval = new ArrayList<>();
		for (final IFixture fix : iter) {
			retval.add(fix);
			if (fix instanceof FixtureIterable) {
				retval.addAll(getFixtures((FixtureIterable<?>) fix));
			}
		}
		return retval;
	}
}
