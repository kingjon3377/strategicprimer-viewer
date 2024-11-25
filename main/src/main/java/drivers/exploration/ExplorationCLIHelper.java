package drivers.exploration;

import legacy.map.Direction;
import legacy.map.HasExtent;
import legacy.map.HasOwner;
import legacy.map.HasPopulation;
import legacy.map.IFixture;
import legacy.map.ILegacyMap;
import legacy.map.Point;
import legacy.map.River;
import legacy.map.TileFixture;
import legacy.map.TileType;
import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.towns.Village;
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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lovelace.util.LovelaceLogger;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import static lovelace.util.Decimalize.decimalize;

/**
 * The logic split out of {@link ExplorationCLI}, some also used in {@link drivers.turnrunning.TurnRunningCLI}
 */
public final class ExplorationCLIHelper implements MovementCostListener, SelectionChangeListener {
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

	private static final List<Speed> SPEED_CHOICES = List.of(Speed.values());

	/**
	 * Let the user change the explorer's speed
	 */
	private void changeSpeed() {
		final Speed temp = cli.chooseFromList(SPEED_CHOICES, "Possible Speeds:", "No speeds available",
				"Chosen Speed: ", ICLIHelper.ListChoiceBehavior.AUTO_CHOOSE_ONLY).getValue1();
		if (Objects.nonNull(temp)) {
			speed = temp;
		}
	}

	/**
	 * Copy the given fixture to subordinate maps and print it to the output stream.
	 */
	private void printAndTransferFixture(final Point destPoint, final @Nullable TileFixture fixture,
	                                     final HasOwner mover, final boolean automatic) {
		if (Objects.nonNull(fixture)) {
			// TODO: Print a description of the form to be copied (omitting acreage, etc.) unless already in sub-map(s).
			cli.print("- ");
			if (automatic) {
				cli.print(fixture.getShortDescription());
				cli.println(" (automatically)");
			} else {
				cli.println(fixture.getShortDescription());
			}
			final IFixture.CopyBehavior zero = switch (fixture) {
				case final Village village -> IFixture.CopyBehavior.ZERO;
				case final HasOwner owned when !owned.owner().equals(mover.owner()) -> IFixture.CopyBehavior.ZERO;
				case final HasPopulation<?> hasPopulation -> IFixture.CopyBehavior.ZERO;
				case final HasExtent<?> hasExtent -> IFixture.CopyBehavior.ZERO;
				default -> IFixture.CopyBehavior.KEEP;
			};
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

	private static final List<String> COMMANDS = List.of("Set Speed", "SW", "S", "SE", "W", "Linger", "E", "NW", "N",
			"NE", "Toward Point", "Quit");

	private final String usage = IntStream.range(0, COMMANDS.size())
			.mapToObj(i -> i + ": " + COMMANDS.get(i)).collect(Collectors.joining(", "));

	/**
	 * When the selected unit changes, print the unit's details and ask how many MP the unit has.
	 */
	@Override
	public void selectedUnitChanged(final @Nullable IUnit old, final @Nullable IUnit newSelection) {
		if (Objects.nonNull(newSelection)) { // TODO What if old == newSelection?
			cli.print("Details of the unit (apparently at ");
			cli.print(model.getSelectedUnitLocation().toString());
			cli.println("):");
			cli.println(newSelection.getVerbose());
			final Integer number = cli.inputNumber("MP the unit has: ");
			if (Objects.nonNull(number)) {
				totalMP = number;
				runningTotal = decimalize(number);
			}
			if (!automationConfig.getPlayer().equals(newSelection.owner())) {
				automationConfig = new ExplorationAutomationConfig(newSelection.owner());
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
		if (Objects.isNull(mover)) {
			cli.println("No unit is selected");
		} else {
			final Point point = model.getSelectedUnitLocation();
			final Direction direction;
			final Point proposedDestination = proposedPath.pollFirst();
			if (Objects.isNull(proposedDestination)) {
				cli.printf("%d/%d MP remaining. Current speed: %s.%n",
						runningTotal.intValue(), totalMP, speed.getShortName());
				cli.printlnAtInterval(usage);
				final int directionNum = Optional.ofNullable(cli.inputNumber("Direction to move: ")).orElse(-1);
				//noinspection SwitchStatementWithTooManyBranches
				switch (directionNum) {
					case 0 -> {
						changeSpeed();
						return;
					}
					case 1 -> direction = Direction.Southwest;
					case 2 -> direction = Direction.South;
					case 3 -> direction = Direction.Southeast;
					case 4 -> direction = Direction.West;
					case 5 -> direction = Direction.Nowhere;
					case 6 -> direction = Direction.East;
					case 7 -> direction = Direction.Northwest;
					case 8 -> direction = Direction.North;
					case 9 -> direction = Direction.Northeast;
					case 10 -> {
						final Point destination = cli.inputPoint("Location to move toward: ");
						if (Objects.isNull(destination)) {
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
					}
					default -> {
						runningTotal = BigDecimal.ZERO;
						return;
					}
				}
			} else {
				direction = Stream.of(Direction.values())
						.filter(d -> proposedDestination.equals(model.getDestination(point, d)))
						.findAny().orElse(Direction.Nowhere);
				if (proposedDestination.equals(point)) {
					return;
				} else if (Direction.Nowhere == direction) {
					cli.printf("Next step %s isn't adjacent to %s%n", proposedDestination, point);
					return;
				}
				cli.printf("%d/%d MP remaining. Current speed: %s.%n",
						runningTotal.intValue(), totalMP, speed.getShortName());
			}

			final Point destPoint = model.getDestination(point, direction);
			try {
				model.move(direction, speed);
			} catch (final TraversalImpossibleException except) {
				LovelaceLogger.debug("Attempted movement to impossible destination");
				cli.println("That direction is impassable; we've made sure all maps show that at a cost of 1 MP");
				return;
			}

			final Collection<TileFixture> constants = new ArrayList<>();
			final ILegacyMap map = model.getMap();
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

			switch (tracksAnimal) {
				case final Animal a -> allFixtures.add(new AnimalTracks(a.getKind()));
				case final AnimalTracks tracks -> allFixtures.add(tracks.copy(IFixture.CopyBehavior.KEEP));
				case null, default -> {
				}
			}

			if (Direction.Nowhere == direction) {
				while (true) {
					final ICLIHelper.BooleanResponse response = cli.inputBooleanInSeries("Take an action here?");
					if (ICLIHelper.BooleanResponse.EOF == response) {
						// TODO: Somehoww propagate the EOF to callers
						runningTotal = BigDecimal.ZERO;
						return;
					} else if (ICLIHelper.BooleanResponse.QUIT == response) {
						runningTotal = BigDecimal.ZERO;
						return;
					} else if (ICLIHelper.BooleanResponse.NO == response) {
						break;
					}
					final Either<SimpleApplet, ICLIHelper.BooleanResponse> choice = appletChooser.chooseApplet();
					final SimpleApplet applet = choice.fromLeft().orElse(null);
					if (Objects.isNull(applet)) {
						final ICLIHelper.BooleanResponse condition = choice.fromRight().orElse(ICLIHelper.BooleanResponse.EOF);
						if (ICLIHelper.BooleanResponse.NO == condition) { // ambiguous/non-present
							break;
						}
						switch (condition) {
							case YES -> { // "--help", handled in chooseApplet()
							}
							case QUIT -> {
								runningTotal = BigDecimal.ZERO;
								return;
							}
							case EOF -> { // TODO: Somehow signal EOF to caller
								runningTotal = BigDecimal.ZERO;
								return;
							}
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

			cli.printf("The explorer comes to %s, a %s%s tile", destPoint, mtn,
					Optional.ofNullable(map.getBaseTerrain(destPoint)).map(TileType::toString)
							.orElse("unknown-terrain"));
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
	public void selectedPointChanged(final @Nullable Point previousSelection, final Point newSelection) {
	}

	@Override
	public void interactionPointChanged() {
	}

	@Override
	public void cursorPointChanged(final @Nullable Point previousCursor, final Point newCursor) {
	}
}
