package report.generators.tabular;

import legacy.map.fixtures.explorable.ExplorableFixture;

import java.util.List;

import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import lovelace.util.DelayedRemovalMap;

import legacy.DistanceComparator;
import legacy.map.Player;
import legacy.map.IFixture;
import legacy.map.MapDimensions;
import legacy.map.Point;
import legacy.map.TileFixture;
import legacy.map.fixtures.TextFixture;
import legacy.map.fixtures.explorable.Cave;
import legacy.map.fixtures.explorable.Portal;
import legacy.map.fixtures.explorable.AdventureFixture;
import legacy.map.fixtures.explorable.Battlefield;

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
    public boolean canHandle(final IFixture fixture) {
        return fixture instanceof ExplorableFixture || fixture instanceof TextFixture;
    }

    public ExplorableTabularReportGenerator(final Player player, final @Nullable Point hq, final MapDimensions dimensions) {
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
    private final @Nullable Point hq;
    private final MapDimensions dimensions;

    /**
     * The header row for the table.
     */
    @Override
    public List<String> getHeaderRow() {
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
    public List<List<String>> produce(
            final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
            /*ExplorableFixture|TextFixture*/final TileFixture item, final int key, final Point loc,
            final Map<Integer, Integer> parentMap) {
        final String brief;
        final String owner;
        final String longDesc;
        switch (item) {
            case final TextFixture tf -> {
                if (tf.getTurn() >= 0) { // TODO: Pull up to conditions on switch case (splitting it)?
                    brief = String.format("Text Note (%d)", tf.getTurn());
                } else {
                    brief = "Text Note";
                }
                owner = "---";
                longDesc = tf.getText();
            }
            case final Battlefield battlefield -> {
                brief = "ancient battlefield";
                owner = "---";
                longDesc = "";
            }
            case final Cave cave -> {
                brief = "caves nearby";
                owner = "---";
                longDesc = "";
            }
            case final Portal p -> { // TODO: Pull up to conditions on switch case (splitting it)?
                if (p.getDestinationCoordinates().isValid()) {
                    brief = "portal to world " + p.getDestinationWorld();
                } else {
                    brief = "portal to another world";
                }
                owner = "---";
                longDesc = "";
            }
            case final AdventureFixture af -> {
                brief = af.getBriefDescription();
                // TODO: Don't we have a helper method for this?
                if (player.equals(af.owner())) { // TODO: Pull up to conditions on switch case (splitting it)?
                    owner = "You";
                } else if (af.owner().isIndependent()) {
                    owner = "No-one";
                } else {
                    owner = ownerString(player, af.owner());
                }
                longDesc = af.getFullDescription();
            }
            default -> {
                return Collections.emptyList();
            }
        }
        fixtures.remove(key);
        return Collections.singletonList(Arrays.asList(distanceString(loc, hq, dimensions),
                locationString(loc), brief, owner, longDesc));
    }

    /**
     * Compare two Point-fixture pairs.
     */
    @Override
    public Comparator<Pair<Point, TileFixture>> comparePairs() {
        return Comparator.<Pair<Point, TileFixture>, Point>comparing(Pair::getValue0, distanceComparator)
                .thenComparing(p -> p.getValue1().toString());
    }
}
