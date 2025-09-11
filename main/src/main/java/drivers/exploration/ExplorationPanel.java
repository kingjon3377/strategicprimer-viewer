package drivers.exploration;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import legacy.map.Direction;
import legacy.map.FakeFixture;
import legacy.map.HasExtent;
import legacy.map.HasOwner;
import legacy.map.HasPopulation;
import legacy.map.IFixture;
import legacy.map.ILegacyMap;
import legacy.map.Player;
import legacy.map.PlayerImpl;
import legacy.map.Point;
import legacy.map.River;
import legacy.map.TileFixture;
import legacy.map.TileType;
import lovelace.util.BorderedPanel;

import static lovelace.util.MenuUtils.createMenuItem;
import static lovelace.util.MenuUtils.createHotKey;

import lovelace.util.ListenedButton;
import lovelace.util.FormattedLabel;
import lovelace.util.FunctionalPopupMenu;
import goldberg.ImprovedComboBox;
import lovelace.util.FunctionalGroupLayout;

import static lovelace.util.FunctionalSplitPane.verticalSplit;

import drivers.common.SelectionChangeListener;
import drivers.common.FixtureMatcher;
import drivers.common.SelectionChangeSource;

import exploration.common.MovementCostListener;
import exploration.common.HuntingModel;
import exploration.common.Speed;
import exploration.common.TraversalImpossibleException;
import exploration.common.IExplorationModel;

import legacy.map.fixtures.towns.Village;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;

import lovelace.util.LovelaceLogger;
import lovelace.util.MenuUtils;
import org.javatuples.Pair;
import drivers.map_viewer.FixtureEditHelper;
import drivers.map_viewer.FixtureFilterTableModel;
import drivers.map_viewer.FixtureListModel;
import drivers.map_viewer.FixtureList;

import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.Animal;

import java.awt.Dimension;

import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

import javax.swing.ListModel;
import javax.swing.JScrollPane;
import javax.swing.JPopupMenu;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.SpinnerNumberModel;
import javax.swing.JPanel;
import javax.swing.ComboBoxModel;
import javax.swing.KeyStroke;

import legacy.idreg.IDRegistrar;
import legacy.idreg.IDFactoryFiller;

import org.jspecify.annotations.Nullable;
import worker.common.IFixtureEditHelper;

/**
 * TODO: try to split controller-functionality from presentation
 */
/* package */ final class ExplorationPanel extends BorderedPanel implements SelectionChangeListener {
	@Serial
	private static final long serialVersionUID = 1L;

	private static KeyStroke key(final int code) {
		return KeyStroke.getKeyStroke(code, 0);
	}

	private final IExplorationModel driverModel;
	private final HuntingModel huntingModel;
	private final SpinnerNumberModel mpModel;

	private void movementDeductionTracker(final Number cost) {
		mpModel.setValue(mpModel.getNumber().doubleValue() - cost.doubleValue());
	}

	public ExplorationPanel(final SpinnerNumberModel mpModel, final ComboBoxModel<Speed> speedModel,
							final JPanel headerPanel, final FunctionalGroupLayout headerLayout,
							final JPanel tilesPanel, final IExplorationModel driverModel,
							final Runnable explorerChangeButtonListener) {
		super(verticalSplit(headerPanel, tilesPanel));
		this.driverModel = driverModel;
		this.mpModel = mpModel;
		LovelaceLogger.trace("In ExplorationPanel initializer");
		final Map<Direction, KeyStroke> arrowKeys = Stream.of(
						Pair.with(Direction.North, key(KeyEvent.VK_UP)),
						Pair.with(Direction.South, key(KeyEvent.VK_DOWN)),
						Pair.with(Direction.West, key(KeyEvent.VK_LEFT)),
						Pair.with(Direction.East, key(KeyEvent.VK_RIGHT)))
				.collect(Collectors.toMap(Pair::getValue0, Pair::getValue1));

		final Map<Direction, KeyStroke> numKeys = Stream.of(
						Pair.with(Direction.North, key(KeyEvent.VK_NUMPAD8)),
						Pair.with(Direction.South, key(KeyEvent.VK_NUMPAD2)),
						Pair.with(Direction.West, key(KeyEvent.VK_NUMPAD4)),
						Pair.with(Direction.East, key(KeyEvent.VK_NUMPAD6)),
						Pair.with(Direction.Northeast, key(KeyEvent.VK_NUMPAD9)),
						Pair.with(Direction.Northwest, key(KeyEvent.VK_NUMPAD7)),
						Pair.with(Direction.Southeast, key(KeyEvent.VK_NUMPAD3)),
						Pair.with(Direction.Southwest, key(KeyEvent.VK_NUMPAD1)),
						Pair.with(Direction.Nowhere, key(KeyEvent.VK_NUMPAD5)))
				.collect(Collectors.toMap(Pair::getValue0, Pair::getValue1));
		driverModel.addMovementCostListener(this::movementDeductionTracker);
		final JButton explorerChangeButton = new ListenedButton("Select a different explorer",
				explorerChangeButtonListener);

		final JLabel remainingMPLabel = new JLabel("Remaining Movement Points:");
		final JSpinner mpField = new JSpinner(mpModel);
		mpField.setMaximumSize(new Dimension(Short.MAX_VALUE, mpField.getPreferredSize().height));

		final JLabel speedLabel = new JLabel("Current relative speed:");

		final Supplier<@Nullable Speed> speedSource = () -> (Speed) speedModel.getSelectedItem();
		final ImprovedComboBox<Speed> speedBox = new ImprovedComboBox<>(speedModel);

		headerPanel.add(explorerChangeButton);
		headerPanel.add(locLabel);
		headerPanel.add(remainingMPLabel);
		headerPanel.add(mpField);
		headerPanel.add(speedLabel);
		headerPanel.add(speedBox);

		LovelaceLogger.trace("ExplorationPanel: headerPanel contents added");

		headerLayout.setHorizontalGroup(
				headerLayout.sequentialGroupOf(explorerChangeButton, locLabel,
						remainingMPLabel, mpField, speedLabel, speedBox));
		headerLayout.setVerticalGroup(headerLayout.parallelGroupOf(explorerChangeButton,
				locLabel, remainingMPLabel, mpField, speedLabel, speedBox));

		LovelaceLogger.trace("ExplorationPanel: headerPanel layout adjusted");

		// TODO: Add 'secondMap' field (i.e. getter/setter, thinking
		// about extra logic needed in the setter) to IExplorationModel
		// (as IMap), to improve no-second-map to a-second-map
		// transition
		final ILegacyMap secondMap = driverModel.streamSubordinateMaps().findFirst().orElseGet(driverModel::getMap);

		final IDRegistrar idf = IDFactoryFiller.createIDFactory(
				driverModel.streamAllMaps().toArray(ILegacyMap[]::new));
		huntingModel = new HuntingModel(driverModel.getMap());

		LovelaceLogger.trace("ExplorationPanel: huntingModel created");

		final IFixtureEditHelper feh = new FixtureEditHelper(driverModel);
		final Function<Point, Collection<TileFixture>> getFixturesMain = driverModel.getMap()::getFixtures;
		final Function<Point, @Nullable TileType> getBaseTerrainMain = driverModel.getMap()::getBaseTerrain;
		final Function<Point, Collection<River>> getRiversMain = driverModel.getMap()::getRivers;
		final Predicate<Point> isMountainousMain = driverModel.getMap()::isMountainous;
		final Function<Point, Collection<TileFixture>> getFixturesSecond = secondMap::getFixtures;
		final Function<Point, @Nullable TileType> getBaseTerrainSecond = secondMap::getBaseTerrain;
		final Function<Point, Collection<River>> getRiversSecond = secondMap::getRivers;
		final Predicate<Point> isMountainousSecond = secondMap::isMountainous;

		for (final Direction direction : Direction.values()) {
			LovelaceLogger.trace("ExplorationPanel: Starting to initialize for %s", direction);
			final FixtureList mainList = new FixtureList(tilesPanel,
					new FixtureListModel(getFixturesMain,
							getBaseTerrainMain,
							getRiversMain, isMountainousMain,
							this::tracksCreator, null, null, null, null, null, null,
							Comparator.naturalOrder()), // TODO: Replace nulls with implementations?
					feh, idf, driverModel.getMap().getPlayers());
			tilesPanel.add(new JScrollPane(mainList));

			LovelaceLogger.trace("ExplorationPanel: main list set up for %s", direction);

			final Iterable<FixtureMatcher> matchers = new FixtureFilterTableModel();
			final DualTileButton dtb = new DualTileButton(driverModel.getMap(), secondMap, matchers);
			// At some point we tried wrapping the button in a JScrollPane.
			tilesPanel.add(dtb);
			LovelaceLogger.trace("ExplorationPanel: Added button for %s", direction);

			final ExplorationClickListener ecl = new ExplorationClickListener(driverModel, this,
					this::movementDeductionTracker, direction, mainList, speedSource);
			if (Direction.Nowhere == direction) {
				dtb.setComponentPopupMenu(ecl.getExplorerActionsMenu());
			}
			createHotKey(dtb, direction.toString(), ecl, MenuUtils.HotKeyCondition.WhenInFocusedWindow,
					Stream.of(arrowKeys.get(direction), numKeys.get(direction))
							.filter(Objects::nonNull).toArray(KeyStroke[]::new));
			dtb.addActionListener(ecl);

			final RandomDiscoverySelector ell = new RandomDiscoverySelector(driverModel,
					mainList, speedSource);

			// mainList.model.addListDataListener(ell);
			driverModel.addSelectionChangeListener(ell);
			ecl.addSelectionChangeListener(ell);

			LovelaceLogger.trace("ExplorationPanel: ell set up for %s", direction);

			final FixtureList secList = new FixtureList(tilesPanel,
					new FixtureListModel(getFixturesSecond, getBaseTerrainSecond,
							getRiversSecond, isMountainousSecond, ExplorationPanel::createNull,
							driverModel::setSubMapTerrain, driverModel::copyRiversToSubMaps,
							driverModel::setMountainousInSubMap, driverModel::copyToSubMaps,
							driverModel::removeRiversFromSubMaps,
							driverModel::removeFixtureFromSubMaps, Comparator.naturalOrder()),
					feh, idf, secondMap.getPlayers());
			tilesPanel.add(new JScrollPane(secList));

			LovelaceLogger.trace("ExploratonPanel: Second list set up for %s", direction);

			final SpeedChangeListener scl = new SpeedChangeListener(ell);
			speedModel.addListDataListener(scl);
			speedChangeListeners.put(direction, scl);

			mains.put(direction, mainList);
			buttons.put(direction, dtb);
			seconds.put(direction, secList);
			ell.selectedPointChanged(null, driverModel.getSelectedUnitLocation());
			LovelaceLogger.trace("ExplorationPanel: Done with %s", direction);
		}
		LovelaceLogger.trace("End of ExplorationPanel initializer");
	}

	// TODO: Cache selected unit here instead of always referring to it via the model?
	@Override
	public void selectedUnitChanged(final @Nullable IUnit old, final @Nullable IUnit newSelection) {
	}

	private final FormattedLabel locLabel = new FormattedLabel(
			"""
					<html><body>Currently exploring %s; click a tile to explore it. Selected fixtures in its left-hand \
					list will be 'discovered'.</body></html>""",
			Point.INVALID_POINT);

	private final Map<Direction, SelectionChangeListener> mains = new EnumMap<>(Direction.class);
	private final Map<Direction, SelectionChangeListener> seconds = new EnumMap<>(Direction.class);
	private final Map<Direction, DualTileButton> buttons = new EnumMap<>(Direction.class);

	private static final class SpeedChangeListener implements ListDataListener {
		public SpeedChangeListener(final SelectionChangeListener scs) {
			this.scs = scs;
		}

		private final SelectionChangeListener scs;

		private Point point = Point.INVALID_POINT;

		public Point getPoint() {
			return point;
		}

		public void setPoint(final Point point) {
			this.point = point;
		}

		private void apply() {
			scs.selectedPointChanged(null, point);
		}

		@Override
		public void contentsChanged(final ListDataEvent event) {
			apply();
		}

		@Override
		public void intervalAdded(final ListDataEvent event) {
			apply();
		}

		@Override
		public void intervalRemoved(final ListDataEvent event) {
			apply();
		}
	}

	private final Map<Direction, SpeedChangeListener> speedChangeListeners = new EnumMap<>(Direction.class);

	@Override
	public void selectedPointChanged(final @Nullable Point old, final Point newPoint) {
		if (newPoint.equals(old)) {
			return;
		}
		LovelaceLogger.trace("In ExplorationPanel.selectedPointChanged");
		for (final Direction direction : Direction.values()) {
			LovelaceLogger.trace("ExplorationPanel.selectedPointChanged: Beginning %s", direction);
			final Point point = driverModel.getDestination(newPoint, direction);
			final Point previous;
			if (speedChangeListeners.containsKey(direction)) {
				// TODO: Change SpeedChangeListener API so we can do this in one operation
				final SpeedChangeListener scl = speedChangeListeners.get(direction);
				previous = scl.getPoint();
				scl.setPoint(point);
			} else {
				previous = old;
			}
			final Consumer<SelectionChangeListener> c = l -> l.selectedPointChanged(previous, point);
			Optional.ofNullable(mains.get(direction)).ifPresent(c);
			Optional.ofNullable(seconds.get(direction)).ifPresent(c);
			Optional.ofNullable(buttons.get(direction)).ifPresent(b -> b.setPoint(point));
			LovelaceLogger.trace("ExplorationPanel.selectedPointChanged: Ending %s", direction);
		}
		locLabel.setArguments(newPoint);
	}

	// TODO: Move as many fields as possible into the constructor, to minimize class footprint

	private @Nullable AnimalTracks tracksCreator(final Point point) {
		final TileType terrain = driverModel.getMap().getBaseTerrain(point);
		if (Objects.isNull(terrain)) {
			return null;
		}
		LovelaceLogger.trace("In ExplorationPanel.tracksCreator");
		final Function<Point, Supplier<Pair<Point, ? extends TileFixture>>> source;
		if (TileType.Ocean == terrain) {
			source = huntingModel::fish;
		} else {
			source = huntingModel::hunt;
		}
		LovelaceLogger.trace("ExplorationPanel.tracksCreator: Determined which source to use");
		final TileFixture animal = source.apply(point).get().getValue1();
		LovelaceLogger.trace("ExplorationPanel.tracksCreator: Got first item from source");
		return switch (animal) {
			case final Animal a -> new AnimalTracks(a.getKind());
			case final AnimalTracks animalTracks -> animalTracks.copy(IFixture.CopyBehavior.ZERO);
			default -> null;
		};
	}

	private static final class ExplorationClickListener implements SelectionChangeSource, ActionListener {
		public ExplorationClickListener(final IExplorationModel driverModel, final SelectionChangeListener outer,
										final MovementCostListener movementDeductionTracker, final Direction direction,
										final FixtureList mainList, final Supplier<Speed> speedSource) {
			this.direction = direction;
			this.mainList = mainList;
			this.driverModel = driverModel;
			this.outer = outer;
			this.movementDeductionTracker = movementDeductionTracker;
			this.speedSource = speedSource;
			explorerActionsMenu = new FunctionalPopupMenu(
					createMenuItem("Swear any villages", KeyEvent.VK_V,
							"Swear any independent villages on this tile to the player's service",
							this::villageSwearingAction),
					createMenuItem("Dig to expose ground", KeyEvent.VK_D,
							"Dig to find what kind of ground is here", driverModel::dig),
					createMenuItem("Search again", KeyEvent.VK_S,
							"Search this tile, as if arriving on it again", this::searchCurrentTile));
		}

		private final IExplorationModel driverModel;
		private final Direction direction;
		private final FixtureList mainList;
		private final SelectionChangeListener outer;
		private final MovementCostListener movementDeductionTracker;
		private final Supplier<Speed> speedSource;

		private final Collection<SelectionChangeListener> selectionListeners = new ArrayList<>();

		@Override
		public void addSelectionChangeListener(final SelectionChangeListener listener) {
			selectionListeners.add(listener);
		}

		@Override
		public void removeSelectionChangeListener(final SelectionChangeListener listener) {
			selectionListeners.remove(listener);
		}

		private List<TileFixture> getSelectedValuesList() {
			final int[] selections = mainList.getSelectedIndices();
			final ListModel<TileFixture> listModel = mainList.getModel();
			final List<TileFixture> retval = new ArrayList<>();
			for (final int index : selections) {
				retval.add(listModel.getElementAt(index < listModel.getSize() ?
						index : listModel.getSize() - 1));
			}
			return retval;
		}

		private void villageSwearingAction() {
			driverModel.swearVillages();
			driverModel.getMap().getFixtures(driverModel.getSelectedUnitLocation())
					.stream().filter(Village.class::isInstance).map(Village.class::cast)
					.forEach(v -> getSelectedValuesList().add(v));
			// FIXME: Adding to that list doesn't actually do anything, if I read it correctly ...
		}

		/**
		 * Copy fixtures from the given list to subordinate maps.
		 */
		private void discoverFixtures(final Iterable<TileFixture> fixtures) {
			final Point destPoint = driverModel.getSelectedUnitLocation();
			final Player player = Optional.ofNullable(driverModel.getSelectedUnit())
					.map(IUnit::owner).orElse(new PlayerImpl(-1, "no-one"));

			driverModel.copyTerrainToSubMaps(destPoint);
			for (final TileFixture fixture : fixtures) {
				if (fixture instanceof FakeFixture) {
					// Skip it! It'll corrupt the output XML!
					continue;
				}
				final IFixture.CopyBehavior zero = switch (fixture) {
					case final Village village -> IFixture.CopyBehavior.ZERO;
					case final HasOwner owned when !player.equals(owned.owner()) -> IFixture.CopyBehavior.ZERO;
					case final HasPopulation<?> hasPopulation -> IFixture.CopyBehavior.ZERO;
					case final HasExtent<?> hasExtent -> IFixture.CopyBehavior.ZERO;
					default -> IFixture.CopyBehavior.KEEP;
				};
				driverModel.copyToSubMaps(destPoint, fixture, zero);
			}
		}

		/**
		 * The action of searching the current tile, since on moving
		 * 'nowhere' the listener now aborts its normal process.
		 */
		private void searchCurrentTile() {
			final List<TileFixture> fixtures = getSelectedValuesList();
			try {
				driverModel.move(Direction.Nowhere, speedSource.get());
			} catch (final TraversalImpossibleException except) {
				LovelaceLogger.error(except, "\"Traversal impossible\" going nowhere");
			}
			discoverFixtures(fixtures);
		}

		/**
		 * A menu of actions the explorer can take when moving 'nowhere'.
		 */
		public JPopupMenu getExplorerActionsMenu() {
			return explorerActionsMenu;
		}

		private final JPopupMenu explorerActionsMenu;

		private void actionPerformedImpl() {
			try {
				final List<TileFixture> fixtures = getSelectedValuesList();
				if (Direction.Nowhere == direction) {
					explorerActionsMenu.show(mainList, mainList.getWidth(), 0);
				} else {
					driverModel.move(direction, speedSource.get());
					discoverFixtures(fixtures);
				}
			} catch (final TraversalImpossibleException except) {
				LovelaceLogger.debug(except, "Attempted movement to impassable destination");
				final Point selection = driverModel.getSelectedUnitLocation();
				outer.selectedPointChanged(null, selection);
				for (final SelectionChangeListener listener : selectionListeners) {
					listener.selectedPointChanged(null, selection);
				}
				movementDeductionTracker.deduct(1);
			}
		}

		@Override
		public void actionPerformed(final ActionEvent event) {
			SwingUtilities.invokeLater(this::actionPerformedImpl);
		}
	}

	private static @Nullable AnimalTracks createNull(final Point point) {
		return null;
	}

	@Override
	public void interactionPointChanged() {
	}

	@Override
	public void cursorPointChanged(final @Nullable Point oldCursor, final Point newCursor) {
	}
}
