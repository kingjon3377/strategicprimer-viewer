package query;

import common.map.fixtures.IResourcePile;
import common.map.fixtures.towns.CommunityStats;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import common.map.fixtures.FixtureIterable;

import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

import common.map.IFixture;
import common.map.Player;
import common.map.HasOwner;
import common.map.TileType;
import common.map.MapDimensions;
import common.map.Point;
import common.map.IMapNG;

import java.util.Set;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Deque;

import drivers.common.IDriverModel;
import drivers.common.ReadOnlyDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import common.map.fixtures.Ground;

import drivers.common.cli.ICLIHelper;
import drivers.common.cli.SimpleApplet;
import drivers.common.cli.AppletChooser;

import common.map.fixtures.terrain.Forest;

import common.map.fixtures.mobile.IWorker;

import exploration.common.SurroundingPointIterable;
import exploration.common.PathfinderFactory;
import exploration.common.Pathfinder;

import common.DistanceComparator;

import common.map.fixtures.towns.ITownFixture;
import common.map.fixtures.towns.TownStatus;
import common.map.fixtures.towns.Village;
import either.Either;

/**
 * A driver for 'querying' the driver model about various things.
 *
 * FIXME: Write GUI equivalent of query CLI
 */
public class QueryCLI implements ReadOnlyDriver {
    /**
     * Count the workers in an Iterable belonging to a player.
     */
    private static int countWorkersInIterable(final Player player, final Iterable<? extends IFixture> fixtures) {
        int retval = 0;
        for (final IFixture fixture : fixtures) {
            if (fixture instanceof IWorker && fixtures instanceof final HasOwner owned &&
                    player.equals(owned.owner())) {
                retval++;
            } else if (fixture instanceof final FixtureIterable<?> iter) {
                retval += countWorkersInIterable(player, iter);
            }
        }
        return retval;
    }

    /**
     * The distance between two points in a map with the given dimensions.
     */
    private static double distance(final Point base, final Point destination, final MapDimensions dimensions) {
        return dimensions.distance(base, destination);
    }

    private final IDriverModel model;

    @Override
    public IDriverModel getModel() {
        return model;
    }

    private final ICLIHelper cli;
    private final IMapNG map;

    @Override
    public SPOptions getOptions() {
        return EmptyOptions.EMPTY_OPTIONS;
    }

    public QueryCLI(final ICLIHelper cli, final IDriverModel model) {
        this.cli = cli;
        this.model = model;
        map = model.getMap();
        pather = PathfinderFactory.pathfinder(map);
        appletChooser = new AppletChooser<>(cli,
                new SimpleApplet(() -> fortressInfo(cli.inputPoint("Location of fortress?")),
                        "Show what a player automatically knows about a fortress's tile.",
                        "fortress"),
                new SimpleApplet(this::printDistance, "Report the distance between two points.",
                        "distance"),
                new SimpleApplet(() -> countWorkers(StreamSupport.stream(
                                model.getMap().getPlayers().spliterator(), true)
                        .collect(Collectors.toList())),
                        "Count how many workers belong to a player", "count"),
                new SimpleApplet(this::findUnexploredCommand,
                        "Find the nearest unexplored tile not behind water.", "unexplored"),
                new SimpleApplet(this::tradeCommand, "Suggest possible trading partners.", "trade"),
                new SimpleApplet(this::findVillagesWithExpertise, "Find villages with a skill", "village-skill"));
    }

    private final Pathfinder pather;

    /**
     * Count the workers belonging to a player.
     */
    private void countWorkers(final List<Player> players) {
        final Player player = cli.chooseFromList((List<? extends Player>) players, "Players in the map:", "Map contains no players", "Owner of workers to count: ", ICLIHelper.ListChoiceBehavior.AUTO_CHOOSE_ONLY).getValue1();
        if (player != null) {
            final int count = countWorkersInIterable(player, map.streamAllFixtures()
                    .collect(Collectors.toList()));
            cli.println(String.format("%s has %d workers", player.getName(), count));
        }
    }

    private void findVillagesWithExpertise() {
        final String skill = cli.inputString("Job to look for: ");
        if (skill == null) {
            return;
        }
        final Point point = cli.inputPoint("Central point for search: ");
        if (point == null) {
            return;
        }
        // FIXME: Also limit search radius
        for (final Pair<Point, ITownFixture> pair : map.streamLocations()
                .sorted(Comparator.comparing(l -> distance(point, l, map.getDimensions())))
                .flatMap(l -> map.getFixtures(l).stream().filter(ITownFixture.class::isInstance)
                        .map(ITownFixture.class::cast).filter(t -> t.getPopulation() != null)
                        .map(f -> Pair.with(l, f))).toList()) {
            final Point loc = pair.getValue0();
            final double delta = distance(point, loc, map.getDimensions());
            final ITownFixture town = pair.getValue1();
            final CommunityStats population = town.getPopulation();
            assert (population != null);
            for (final Map.Entry<String, Integer> entry : population.getHighestSkillLevels().entrySet()) {
                final String expert = entry.getKey();
                final int level = entry.getValue();
                if (expert.toLowerCase().contains(skill.toLowerCase())) {
                    cli.print("- At ", town.getName(), ", at ", loc.toString(), " (", String.format("%.1f", delta),
                            " tiles away), a level-", Integer.toString(level), " ");
                    cli.println(expert);
                }
            }
        }
    }

    /**
     * Report the distance between two points.
     *
     * FIXME: abort after EOF at any point
     */
    private void printDistance() {
        final Point start = cli.inputPoint("Starting point:\t");
        final Point end = cli.inputPoint("Destination:\t");
        final Boolean groundTravel = cli.inputBoolean("Compute ground travel distance?");
        if (start != null && end != null && groundTravel != null) {
            if (groundTravel) {
                cli.print("Distance (on the ground, in MP cost):\t");
                cli.println(Integer.toString(pather.getTravelDistance(start, end)
                        .getValue0()));
            } else {
                cli.print("Distance (as the crow files, in tiles):\t");
                cli.println(String.format("%.0f", distance(start, end,
                        map.getDimensions())));
            }
        }
    }

    /**
     * Give the data about a tile that the player is supposed to
     * automatically know if he has a fortress on it. For the convenience
     * of the sole caller, this method accepts null, and does nothing when
     * that is the parameter.
     */
    private void fortressInfo(final @Nullable Point location) {
        if (location != null) {
            cli.println(String.format("Terrain is %s",
                    Optional.ofNullable(map.getBaseTerrain(location))
                            .map(TileType::toString).orElse("unknown")));
            final List<Ground> ground = map.getFixtures(location).stream()
                    .filter(Ground.class::isInstance).map(Ground.class::cast).toList();
            final List<Forest> forests = map.getFixtures(location).stream()
                    .filter(Forest.class::isInstance).map(Forest.class::cast).toList();
            if (!ground.isEmpty()) {
                cli.println("Kind(s) of ground (rock) on the tile:");
                ground.stream().map(Object::toString).forEach(cli::println);
            }
            if (!forests.isEmpty()) {
                cli.println("Kind(s) of forests on the tile:");
                forests.stream().map(Object::toString).forEach(cli::println);
            }
        }
    }

    /**
     * Find the nearest obviously-reachable unexplored location.
     */
    private @Nullable Point findUnexplored(final Point base) {
        final Deque<Point> queue = new LinkedList<>();
        queue.addLast(base);
        final MapDimensions dimensions = map.getDimensions();
        final Set<Point> considered = new HashSet<>();
        final List<Point> retval = new ArrayList<>();
        while (!queue.isEmpty()) {
            final Point current = queue.removeFirst();
            final TileType currentTerrain = map.getBaseTerrain(current);
            if (considered.contains(current)) {
                continue;
            } else if (currentTerrain == null) {
                retval.add(current);
            } else {
                if (TileType.Ocean != currentTerrain) {
                    final double baseDistance = distance(base, current, dimensions);
                    for (final Point neighbor : new SurroundingPointIterable(current,
                            dimensions, 1)) {
                        if (distance(base, neighbor, dimensions) >= baseDistance) {
                            queue.addLast(neighbor);
                        }
                    }
                }
            }
            considered.add(current);
        }
        return retval.stream().min(new DistanceComparator(base, dimensions)).orElse(null);
    }

    /**
     * Print a list of active towns within the given distance of the given
     * base that produce any resources, and what resources they produce.
     */
    private void suggestTrade(final Point base, final int distance) {
        final DistanceComparator comparator = new DistanceComparator(base, map.getDimensions());
        for (final Point location : new SurroundingPointIterable(base,
                map.getDimensions(), distance).stream().distinct()
                .sorted(comparator).toList()) { // TODO: can we combine loops?
            for (final ITownFixture town : map.getFixtures(location).stream()
                    .filter(ITownFixture.class::isInstance)
                    .map(ITownFixture.class::cast).toList()) {
				final CommunityStats population = town.getPopulation();
                if (TownStatus.Active == town.getStatus() &&
                        population != null &&
                        population.getYearlyProduction().isEmpty()) {
                    cli.print("At ", location.toString());
                    cli.print(comparator.distanceString(location, "base"), ": ");
                    cli.print(town.getName(), ", a ", town.getTownSize().toString(), " ");
                    if (town instanceof final Village v &&
                            !"human".equals(v.getRace())) {
                        cli.print(v.getRace(), " village");
                    } else {
                        cli.print(town.getKind());
                    }
                    if (town.owner().isIndependent()) {
                        cli.print(", independent");
                    } else if (!map.getCurrentPlayer().equals(town.owner())) {
                        cli.print(", allied to ", town.owner().toString());
                    }
                    cli.println(". Its yearly production:");
                    for (final IResourcePile resource :
                            population.getYearlyProduction()) {
                        cli.print("- ", resource.getKind(), ": ");
                        cli.print(resource.getQuantity().number().toString());
                        if (resource.getQuantity().units().isEmpty()) {
                            cli.print(" ");
                        } else if ("dozen".equals(resource.getQuantity().units())) {
                            cli.print(" dozen ");
                        } else {
                            cli.print(" ", resource.getQuantity().units(),
                                    " of ");
                        }
                        cli.println(resource.getContents());
                        if ("milk".equals(resource.getContents())) {
                            cli.println("- Corresponding livestock");
                        } else if ("eggs".equals(resource.getContents())) {
                            cli.println("- Corresponding poultry");
                        }
                    }
                }
            }
        }
    }

    private static final DecimalFormat ONE_PLACE_FORMAT = new DecimalFormat("#,###.0");

    private void findUnexploredCommand() {
        final Point base = cli.inputPoint("Starting point? ");
        if (base != null) {
            final Point unexplored = findUnexplored(base);
            if (unexplored == null) {
                cli.println("No unexplored tiles found.");
            } else {
                final double distanceTo = distance(base, unexplored, map.getDimensions());
                cli.println(String.format("Nearest unexplored tile is %s, %s tiles away",
                        unexplored, ONE_PLACE_FORMAT.format(distanceTo)));
            }
        }
    }

    private void tradeCommand() {
        final Point location = cli.inputPoint("Base location? ");
        if (location != null) {
            final Integer distance = cli.inputNumber("Within how many tiles? ");
            if (distance != null) {
                suggestTrade(location, distance);
            }
        }
    }

    private final AppletChooser<SimpleApplet> appletChooser;

    /**
     * Accept and respond to commands.
     */
    @Override
    public void startDriver() {
        while (true) {
            final Either<SimpleApplet, Boolean> selection = appletChooser.chooseApplet();
            if (selection == null || (selection.fromRight().isPresent() &&
                    selection.fromRight().get())) {
                continue;
            } else if (selection.fromRight().isPresent() &&
                    !selection.fromRight().get()) {
                break;
            } else if (selection.fromLeft().isPresent()) {
                selection.fromLeft().get().invoke();
            } else {
                throw new IllegalStateException("Impossible Either state");
            }
        }
    }
}
