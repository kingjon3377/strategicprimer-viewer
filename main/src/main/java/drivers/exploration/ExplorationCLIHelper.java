package drivers.exploration;

import common.map.Direction;
import common.map.HasExtent;
import common.map.HasOwner;
import common.map.HasPopulation;
import common.map.IFixture;
import common.map.IMapNG;
import common.map.Point;
import common.map.River;
import common.map.TileFixture;
import common.map.TileType;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.towns.Village;
import drivers.common.SelectionChangeListener;
import drivers.common.cli.AppletChooser;
import drivers.common.cli.ICLIHelper;
import drivers.common.cli.SimpleApplet;
import either.Either;
import exploration.common.HuntingModel;
import exploration.common.IExplorationModel;
import exploration.common.MovementCostListener;
import exploration.common.Pathfinder;
import exploration.common.PathfinderFactory;
import exploration.common.SimpleMovementModel;
import exploration.common.Speed;
import exploration.common.TraversalImpossibleException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import static lovelace.util.Decimalize.decimalize;

/**
 * The logic split out of {@link ExplorationCLI}, some also used in {@link drivers.turnrunning.TurnRunningCLI}
 */
public class ExplorationCLIHelper implements MovementCostListener, SelectionChangeListener {
	private static final Logger LOGGER = Logger.getLogger(ExplorationCLIHelper.class.getName());
	private final IExplorationModel model;
	private final ICLIHelper cli;
	private final HuntingModel huntingModel;
	public ExplorationCLIHelper(final IExplorationModel model, final ICLIHelper cli) {
		this.model = model;
		this.cli = cli;
		huntingModel = new HuntingModel(model.getMap());
		pather = PathfinderFactory.pathfinder(model.streamSubordinateMaps().findFirst().orElseGet(model::getMap));
		automationConfig = new ExplorationAutomationConfig(model.getMap()
			.getCurrentPlayer());
		appletChooser = new AppletChooser<>(this.cli,
			new SimpleApplet(model::swearVillages, "Swear any village here to the player", "swear"),
			new SimpleApplet(model::dig, "Dig to expose some ground here", "dig"));
	}

	/**
	 * The explorer's current movement speed.
	 */
	private Speed speed = Speed.Normal;

	/**
	 * Let the user change the explorer's speed
	 */
	private void changeSpeed() {
		final Speed temp = cli.chooseFromList(Arrays.asList(Speed.values()), "Possible Speeds:", "No speeds available", "Chosen Speed: ", ICLIHelper.ListChoiceBehavior.AUTO_CHOOSE_ONLY).getValue1();
		if (temp != null) {
			speed = temp;
		}
	}

	/**
	 * Copy the given fixture to subordinate maps and print it to the output stream.
	 */
	private void printAndTransferFixture(final Point destPoint, final @Nullable TileFixture fixture, final HasOwner mover,
	                                     final boolean automatic) {
		if (fixture != null) {
			if (automatic) {
				cli.print("- ");
				cli.print(fixture.toString());
				cli.println(" (automatically)");
			} else {
				cli.println(fixture.toString());
			}
			final IFixture.CopyBehavior zero;
			if (fixture instanceof HasOwner && (!((HasOwner) fixture).getOwner().equals(mover.getOwner())
					|| fixture instanceof Village)) {
				zero = IFixture.CopyBehavior.ZERO;
			} else if (fixture instanceof HasPopulation || fixture instanceof HasExtent)
				zero = IFixture.CopyBehavior.ZERO;
			else {
				zero = IFixture.CopyBehavior.KEEP;
			}
			model.copyToSubMaps(destPoint, fixture, zero);
		}
	}

	private int totalMP = 0;
	private BigDecimal runningTotal = BigDecimal.ZERO;

	public int getMovement() {
		return runningTotal.intValue();
	}

	@Override
	public void deduct(final Number cost) {
		runningTotal = runningTotal.subtract(decimalize(cost));
	}

	private final LinkedList<Point> proposedPath = new LinkedList<>();

	private ExplorationAutomationConfig automationConfig;

	private static final List<String> COMMANDS = List.of("Set Speed", "SW", "S", "SE", "W", "Linger", "E", "NW", "N", "NE", "Toward Point", "Quit");

	private final String usage = IntStream.range(0, COMMANDS.size())
		.mapToObj(i -> i + ": " + COMMANDS.get(i)).collect(Collectors.joining(", "));

	/**
	 * When the selected unit changes, print the unit's details and ask how many MP the unit has.
	 */
	@Override
	public void selectedUnitChanged(final @Nullable IUnit old, final @Nullable IUnit newSelection) {
		if (newSelection != null) { // TODO What if old == newSelection?
			cli.print("Details of the unit (apparently at ");
			cli.print(model.getSelectedUnitLocation().toString());
			cli.println("):");
			cli.println(newSelection.getVerbose());
			final Integer number = cli.inputNumber("MP the unit has: ");
			if (number != null) {
				totalMP = number;
				runningTotal = decimalize(number);
			}
			if (!automationConfig.getPlayer().equals(newSelection.getOwner())) {
				automationConfig = new ExplorationAutomationConfig(newSelection.getOwner());
			}
		}
	}

	private final AppletChooser<SimpleApplet> appletChooser;

	private final Pathfinder pather;

	/**
	 * If the unit has a proposed path, move one more tile along it;
	 * otherwise, ask the user for directions once and make that move, then
	 * return to the caller.
	 */
	// No need to set the 'modified' flag anywhere in this method, as
	// ExplorationModel.move() always sets it.
	public void moveOneStep() {
		final IUnit mover = model.getSelectedUnit();
		if (mover == null) {
			cli.println("No unit is selected");
		} else {
			final Point point = model.getSelectedUnitLocation();
			final Direction direction;
			final Point proposedDestination = proposedPath.pollFirst();
			if (proposedDestination == null) {
				cli.println(String.format("%d/%d MP remaining. Current speed: %s.",
						runningTotal.intValue(), totalMP, speed.getShortName()));
				cli.printlnAtInterval(usage);
				final int directionNum = Optional.ofNullable(cli.inputNumber("Direction to move: ")).orElse(-1);
				switch (directionNum) {
					case 0:
						changeSpeed();
						return;
					case 1:
						direction = Direction.Southwest;
						break;
					case 2:
						direction = Direction.South;
						break;
					case 3:
						direction = Direction.Southeast;
						break;
					case 4:
						direction = Direction.West;
						break;
					case 5:
						direction = Direction.Nowhere;
						break;
					case 6:
						direction = Direction.East;
						break;
					case 7:
						direction = Direction.Northwest;
						break;
					case 8:
						direction = Direction.North;
						break;
					case 9:
						direction = Direction.Northeast;
						break;
					case 10:
						final Point destination = cli.inputPoint("Location to move toward: ");
						if (destination == null) {
							// EOF
							runningTotal = BigDecimal.ZERO;
						} else {
							final Pair<Integer, Iterable<Point>> pair =
									pather.getTravelDistance(point, destination);
							final int cost = pair.getValue0();
							final Iterable<Point> path = pair.getValue1();
							if (path.iterator().hasNext()) {
								path.forEach(proposedPath::addLast);
							} else {
								cli.println(
										"S/he doesn't know how to get there from here.");
							}
						}
						return;
					default:
						runningTotal = BigDecimal.ZERO;
						return;
				}
			} else {
				direction = Stream.of(Direction.values())
						.filter(d -> proposedDestination.equals(model.getDestination(point, d)))
						.findAny().orElse(Direction.Nowhere);
				if (proposedDestination.equals(point)) {
					return;
				} else if (Direction.Nowhere == direction) {
					cli.println(String.format("Next step %s isn't adjacent to %s",
							proposedDestination, point));
					return;
				}
				cli.println(String.format("%d/%d MP remaining. Current speed: %s.",
						runningTotal.intValue(), totalMP, speed.getShortName()));
			}

			final Point destPoint = model.getDestination(point, direction);
			try {
				model.move(direction, speed);
			} catch (final TraversalImpossibleException except) {
				LOGGER.fine("Attempted movement to impossible destination");
				cli.println("That direction is impassable; we've made sure all maps show that at a cost of 1 MP");
				return;
			}

			final List<TileFixture> constants = new ArrayList<>();
			final IMapNG map = model.getMap();
			final List<TileFixture> allFixtures = new ArrayList<>();

			for (final TileFixture fixture : map.getFixtures(destPoint)) {
				if (SimpleMovementModel.shouldAlwaysNotice(mover, fixture)) {
					constants.add(fixture);
				} else if (SimpleMovementModel.shouldSometimesNotice(mover, speed, fixture)) {
					allFixtures.add(fixture);
				}
			}

			/*Animal|AnimalTracks|HuntingModel.NothingFound*/
			final TileFixture tracksAnimal;

			// Since not-visible terrain is impassable, by this point we know the tile is visible.
			final TileType terrain = map.getBaseTerrain(destPoint);
			if (TileType.Ocean == terrain) {
				tracksAnimal = huntingModel.fish(destPoint).iterator().next().getValue1();
			} else {
				tracksAnimal = huntingModel.hunt(destPoint).iterator().next().getValue1();
			}

			if (tracksAnimal instanceof Animal) {
				allFixtures.add(new AnimalTracks((((Animal) tracksAnimal).getKind())));
			} else if (tracksAnimal instanceof AnimalTracks) {
				allFixtures.add(tracksAnimal.copy(IFixture.CopyBehavior.KEEP));
			}

			if (Direction.Nowhere == direction) {
				while (true) {
					final Boolean response = cli.inputBooleanInSeries("Take an action here?");
					if (response == null) {
						// EOF
						runningTotal = BigDecimal.ZERO;
						return;
					} else if (!response) {
						break;
					}
					final Either<SimpleApplet, Boolean> choice = appletChooser.chooseApplet();
					assert choice != null;
					final SimpleApplet applet = choice.fromLeft().orElse(null);
					final Boolean bool = choice.fromRight().orElse(null);
					if (applet == null) {
						if (bool == null) {
							// EOF
							runningTotal = BigDecimal.ZERO;
							return;
						} else if (!bool) {
							break;
						}
					} else {
						applet.invoke();
					}
				}
			}

			final String mtn;
			if (map.isMountainous(destPoint)) {
				mtn = "mountainous ";
			} else {
				mtn = "";
			}
			model.copyTerrainToSubMaps(destPoint);

			cli.print(String.format("The explorer comes to %s, a %s%s tile", destPoint, mtn,
					Optional.ofNullable(map.getBaseTerrain(destPoint)).map(TileType::toString)
							.orElse("unknown-terrain")));
			final Collection<River> rivers = map.getRivers(destPoint);
			final boolean anyRivers;
			if (rivers.contains(River.Lake)) {
				anyRivers = true;
				if (rivers.stream().anyMatch(r -> River.Lake != r)) {
					cli.print(" with a lake and (a) river(s) flowing ");
				} else {
					cli.print(" with a lake");
				}
			} else if (!rivers.isEmpty()) {
				anyRivers = true;
				cli.print(" with (a) river(s) flowing ");
			} else {
				anyRivers = false;
			}
			cli.println(rivers.stream().filter(r -> River.Lake != r).map(River::toString)
					.collect(Collectors.joining(", ")));

			if (!map.getRoads(destPoint).isEmpty()) {
				if (anyRivers) {
					cli.print(". There are (a) road(s) to the ");
				} else {
					cli.print(" with (a) road(s) to the ");
				}
				cli.println(map.getRoads(destPoint).keySet().stream().map(Direction::toString)
						.collect(Collectors.joining(", "))); // TODO: Report on road quality
			}
			final Iterable<TileFixture> noticed = SimpleMovementModel.selectNoticed(allFixtures, Function.identity(),
					mover, speed);

			if (!constants.isEmpty() || noticed.iterator().hasNext()) {
				cli.println("The following were noticed:");
				for (final TileFixture fixture : constants) {
					printAndTransferFixture(destPoint, fixture, mover, true);
				}
				for (final TileFixture fixture : noticed) {
					printAndTransferFixture(destPoint, fixture, mover, false);
				}
			}

			if (!proposedPath.isEmpty() && automationConfig.stopAtPoint(cli,
					model.streamSubordinateMaps().findFirst().orElseGet(model::getMap),
					destPoint)) {
				proposedPath.clear();
			}
		}
	}

	@Override
	public void selectedPointChanged(final @Nullable Point previousSelection, final Point newSelection) {}

	@Override
	public void interactionPointChanged() {}

	@Override
	public void cursorPointChanged(final @Nullable Point previousCursor, final Point newCursor) {}
}
