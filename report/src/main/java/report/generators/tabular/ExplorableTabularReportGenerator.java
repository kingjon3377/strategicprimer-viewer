package report.generators.tabular;

import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import lovelace.util.DelayedRemovalMap;

import common.DistanceComparator;
import common.map.Player;
import common.map.IFixture;
import common.map.MapDimensions;
import common.map.Point;
import common.map.TileFixture;
import common.map.fixtures.TextFixture;
import common.map.fixtures.explorable.ExplorableFixture;
import common.map.fixtures.explorable.Cave;
import common.map.fixtures.explorable.Portal;
import common.map.fixtures.explorable.AdventureFixture;
import common.map.fixtures.explorable.Battlefield;

import java.util.Comparator;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;

/**
 * A tabular report generator for things that can be explored and are not
 * covered elsewhere: caves, battlefields, adventure hooks, and portals.
 */
public class ExplorableTabularReportGenerator
		implements ITableGenerator</*ExplorableFixture|TextFixture*/TileFixture> {
	@Override
	public Class<TileFixture> narrowedClass() {
		return TileFixture.class;
	}

	public ExplorableTabularReportGenerator(final Player player, @Nullable final Point hq, final MapDimensions dimensions) {
		this.player = player;
		this.hq = hq;
		this.dimensions = dimensions;
		if (hq == null) {
			distanceComparator = (one, two) -> 0;
		} else {
			distanceComparator = new DistanceComparator(hq, dimensions);
		}
	}

	private final Player player;
	@Nullable
	private final Point hq;
	private final MapDimensions dimensions;

	/**
	 * The header row for the table.
	 */
	@Override
	public Iterable<String> getHeaderRow() {
		return Arrays.asList("Distance", "Location", "Brief Description", "Claimed By",
			"Long Description");
	}

	/**
	 * The file-name to (by default) write this table to.
	 */
	@Override
	public String getTableName() {
		return "explorables";
	}

	private final Comparator<Point> distanceComparator;

	/**
	 * Create a GUI table row representing the given fixture.
	 */
	@Override
	public Iterable<Iterable<String>> produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			/*ExplorableFixture|TextFixture*/final TileFixture item, final int key, final Point loc,
			final Map<Integer, Integer> parentMap) {
		String brief;
		String owner;
		String longDesc;
		if (item instanceof TextFixture) {
			if (((TextFixture) item).getTurn() >= 0) {
				brief = String.format("Text Note (%d)", ((TextFixture) item).getTurn());
			} else {
				brief = "Text Note";
			}
			owner = "---";
			longDesc = ((TextFixture) item).getText();
		} else if (item instanceof Battlefield) {
			brief = "ancient battlefield";
			owner = "---";
			longDesc = "";
		} else if (item instanceof Cave) {
			brief = "caves nearby";
			owner = "---";
			longDesc = "";
		} else if (item instanceof Portal) {
			if (((Portal) item).getDestinationCoordinates().isValid()) {
				brief = "portal to world " + ((Portal) item).getDestinationWorld();
			} else {
				brief = "portal to another world";
			}
			owner = "---";
			longDesc = "";
		} else if (item instanceof AdventureFixture) {
			brief = ((AdventureFixture) item).getBriefDescription();
			// TODO: Don't we have a helper method for this?
			if (player.equals(((AdventureFixture) item).getOwner())) {
				owner = "You";
			} else if (((AdventureFixture) item).getOwner().isIndependent()) {
				owner = "No-one";
			} else {
				owner = ownerString(player, ((AdventureFixture) item).getOwner());
			}
			longDesc = ((AdventureFixture) item).getFullDescription();
		} else {
			return Collections.emptyList();
		}
		fixtures.remove(key);
		return Collections.singleton(Arrays.asList(distanceString(loc, hq, dimensions),
			locationString(loc), brief, owner, longDesc));
	}

	/**
	 * Compare two Point-fixture pairs.
	 */
	@Override
	public int comparePairs(final Pair<Point, /*ExplorableFixture|TextFixture*/TileFixture> one,
	                        final Pair<Point, /*ExplorableFixture|TextFixture*/TileFixture> two) {
		return Comparator.<Pair<Point, TileFixture>, Point>comparing(Pair::getValue0, distanceComparator)
			.thenComparing(p -> p.getValue1().toString()).compare(one, two);
	}
}
