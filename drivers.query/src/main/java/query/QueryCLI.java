package query;

import common.map.fixtures.IResourcePile;
import java.util.Optional;
import common.map.fixtures.FixtureIterable;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
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
import exploration.common.HuntingModel;

import common.DistanceComparator;

import java.util.logging.Logger;

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
	 * A logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(QueryCLI.class.getName());

	/**
	 * Count the workers in an Iterable belonging to a player.
	 */
	private static int countWorkersInIterable(Player player, Iterable<? extends IFixture> fixtures) {
		int retval = 0;
		for (IFixture fixture : fixtures) {
			if (fixture instanceof IWorker && fixtures instanceof HasOwner &&
					player.equals(((HasOwner) fixtures).getOwner())) {
				retval++;
			} else if (fixture instanceof FixtureIterable) {
				retval += countWorkersInIterable(player, (FixtureIterable) fixture);
			}
		}
		return retval;
	}

	/**
	 * The distance between two points in a map with the given dimensions.
	 */
	private static double distance(Point base, Point destination, MapDimensions dimensions) {
		return dimensions.distance(base, destination);
	}

	private final IDriverModel model;

	@Override
	public IDriverModel getModel() {
		return model;
	}

	private final ICLIHelper cli;
	private final IMapNG map;
	private final HuntingModel huntModel;

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	public QueryCLI(ICLIHelper cli, IDriverModel model) {
		this.cli = cli;
		this.model = model;
		map = model.getMap();
		huntModel = new HuntingModel(map);
		pather = PathfinderFactory.pathfinder(map);
		appletChooser = new AppletChooser(cli,
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
			new SimpleApplet(this::tradeCommand, "Suggest possible trading partners.",
				"trade"));
	}

	private final Pathfinder pather;

	/**
	 * Count the workers belonging to a player.
	 */
	private void countWorkers(List<Player> players) {
		Player player = cli.chooseFromList(players, "Players in the map:", "Map contains no players",
			"Owner of workers to count: ", true).getValue1();
		if (player != null) {
			int count = countWorkersInIterable(player, map.streamLocations()
					.flatMap(l -> map.getFixtures(l).stream())
					.collect(Collectors.toList()));
			cli.println(String.format("%s has %d workers", player.getName(), count));
		}
	}

	/**
	 * Report the distance between two points.
	 *
	 * FIXME: abort after EOF at any point
	 */
	private void printDistance() {
		Point start = cli.inputPoint("Starting point:\t");
		Point end = cli.inputPoint("Destination:\t");
		Boolean groundTravel = cli.inputBoolean("Compute ground travel distance?");
		if (start != null && end != null && groundTravel != null) {
			if (groundTravel) {
				cli.print("Distance (on the ground, in MP cost):\t");
				cli.println(Integer.toString(pather.getTravelDistance(start, end)
					.getValue0()));
			} else {
				cli.print("Distance (as the crow files, in tiles):\t");
				cli.println(String.format("%0.0f", distance(start, end,
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
	private void fortressInfo(@Nullable Point location) {
		if (location != null) {
			cli.println(String.format("Terrain is %s",
				Optional.ofNullable(map.getBaseTerrain(location))
					.map(TileType::toString).orElse("unknown")));
			List<Ground> ground = map.getFixtures(location).stream()
				.filter(Ground.class::isInstance).map(Ground.class::cast)
				.collect(Collectors.toList());
			List<Forest> forests = map.getFixtures(location).stream()
				.filter(Forest.class::isInstance).map(Forest.class::cast)
				.collect(Collectors.toList());
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
	@Nullable
	private Point findUnexplored(Point base) {
		Deque<Point> queue = new LinkedList<>();
		queue.addLast(base);
		MapDimensions dimensions = map.getDimensions();
		Set<Point> considered = new HashSet<>();
		List<Point> retval = new ArrayList<>();
		while (!queue.isEmpty()) {
			Point current = queue.removeFirst();
			TileType currentTerrain = map.getBaseTerrain(current);
			if (considered.contains(current)) {
				continue;
			} else if (currentTerrain != null) { // TODO: invert
				if (!TileType.Ocean.equals(currentTerrain)) {
					double baseDistance = distance(base, current, dimensions);
					for (Point neighbor : new SurroundingPointIterable(current,
							dimensions, 1)) {
						if (distance(base, neighbor, dimensions) >= baseDistance) {
							queue.addLast(neighbor);
						}
					}
				}
			} else {
				retval.add(current);
			}
			considered.add(current);
		}
		return retval.stream().min(new DistanceComparator(base, dimensions)).orElse(null);
	}

	/**
	 * Print a list of active towns within the given distance of the given
	 * base that produce any resources, and what resources they produce.
	 */
	private void suggestTrade(Point base, int distance) {
		DistanceComparator comparator = new DistanceComparator(base, map.getDimensions());
		for (Point location : StreamSupport.stream(new SurroundingPointIterable(base,
				map.getDimensions(), distance).spliterator(), true).distinct()
				.sorted(comparator).collect(Collectors.toList())) { // TODO: can we combine loops?
			for (ITownFixture town : map.getFixtures(location).stream()
					.filter(ITownFixture.class::isInstance)
					.map(ITownFixture.class::cast).collect(Collectors.toList())) {
				if (TownStatus.Active.equals(town.getStatus()) &&
						town.getPopulation() != null &&
						!town.getPopulation().getYearlyProduction().isEmpty()) {
					cli.print("At ", location.toString());
					cli.print(comparator.distanceString(location, "base"), ": ");
					cli.print(town.getName(), ", a ", town.getTownSize().toString(), " ");
					if (town instanceof Village &&
							!"human".equals(((Village) town).getRace())) {
						cli.print(((Village) town).getRace(), " village");
					} else {
						cli.print(town.getKind());
					}
					if (town.getOwner().isIndependent()) {
						cli.print(", independent");
					} else if (!map.getCurrentPlayer().equals(town.getOwner())) {
						cli.print(", allied to ", town.getOwner().toString());
					}
					cli.println(". Its yearly production:");
					for (IResourcePile resource :
							town.getPopulation().getYearlyProduction()) {
						cli.print("- ", resource.getKind(), ": ");
						cli.print(resource.getQuantity().getNumber().toString());
						if (resource.getQuantity().getUnits().isEmpty()) {
							cli.print(" ");
						} else if ("dozen".equals(resource.getQuantity().getUnits())) {
							cli.print(" dozen ");
						} else {
							cli.print(" ", resource.getQuantity().getUnits(),
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
		Point base = cli.inputPoint("Starting point? ");
		if (base != null) {
			Point unexplored = findUnexplored(base);
			if (unexplored != null) { // TODO: invert
				double distanceTo = distance(base, unexplored, map.getDimensions());
				cli.println(String.format("Nearest unexplored tile is %s, %s tiles away",
					unexplored, ONE_PLACE_FORMAT.format(distanceTo)));
			} else {
				cli.println("No unexplored tiles found.");
			}
		}
	}

	private void tradeCommand() {
		Point location = cli.inputPoint("Base location? ");
		if (location != null) {
			Integer distance = cli.inputNumber("Within how many tiles? ");
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
			Either<SimpleApplet, Boolean> selection = appletChooser.chooseApplet();
			if (selection == null || (selection.fromRight().isPresent() &&
					selection.fromRight().get().booleanValue())) {
				continue;
			} else if (selection.fromRight().isPresent() &&
					!selection.fromRight().get().booleanValue()) {
				break;
			} else if (selection.fromLeft().isPresent()) {
				selection.fromLeft().get().invoke();
			} else {
				throw new IllegalStateException("Impossible Either state");
			}
		}
	}
}
