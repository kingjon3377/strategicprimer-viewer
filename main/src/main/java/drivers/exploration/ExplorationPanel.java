package drivers.exploration;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lovelace.util.BorderedPanel;
import static lovelace.util.MenuUtils.createMenuItem;
import static lovelace.util.MenuUtils.createHotKey;
import lovelace.util.ListenedButton;
import lovelace.util.InterpolatedLabel;
import lovelace.util.FormattedLabel;
import lovelace.util.FunctionalPopupMenu;
import lovelace.util.ImprovedComboBox;
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

import common.map.fixtures.towns.Village;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;

import org.javatuples.Pair;
import drivers.map_viewer.FixtureEditHelper;
import drivers.map_viewer.FixtureFilterTableModel;
import drivers.map_viewer.FixtureListModel;
import drivers.map_viewer.FixtureList;

import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.Animal;

import java.awt.Dimension;

import common.map.FakeFixture;
import common.map.HasExtent;
import common.map.IMapNG;
import common.map.PlayerImpl;
import common.map.Player;
import common.map.TileFixture;
import common.map.HasOwner;
import common.map.TileType;
import common.map.Point;
import common.map.Direction;
import common.map.HasPopulation;

import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

import javax.swing.ListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JPopupMenu;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.SpinnerNumberModel;
import javax.swing.JPanel;
import javax.swing.ComboBoxModel;
import javax.swing.KeyStroke;

import common.idreg.IDRegistrar;
import common.idreg.IDFactoryFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.jetbrains.annotations.Nullable;
import worker.common.IFixtureEditHelper;

/**
 * TODO: try to split controller-functionality from presentation
 */
/* package */ class ExplorationPanel extends BorderedPanel implements SelectionChangeListener {
	private static final Logger LOGGER = Logger.getLogger(ExplorationPanel.class.getName());
	private static KeyStroke key(final int code) {
		return KeyStroke.getKeyStroke(code, 0);
	}
	private final IExplorationModel driverModel;
	private final HuntingModel huntingModel;
	private final Supplier<Speed> speedSource;
	private final SpinnerNumberModel mpModel;

	private void movementDeductionTracker(final int cost) {
		mpModel.setValue(mpModel.getNumber().intValue() - cost);
	}

	public ExplorationPanel(final SpinnerNumberModel mpModel, final ComboBoxModel<Speed> speedModel,
	                        final JPanel headerPanel, final FunctionalGroupLayout headerLayout,
	                        final JPanel tilesPanel, final IExplorationModel driverModel,
	                        final Runnable explorerChangeButtonListener) {
		super(verticalSplit(headerPanel, tilesPanel));
		this.driverModel = driverModel;
		this.mpModel = mpModel;
		LOGGER.finer("In ExplorationPanel initializer");
		Map<Direction, KeyStroke> arrowKeys = Stream.of(
				Pair.with(Direction.North, key(KeyEvent.VK_UP)),
				Pair.with(Direction.South, key(KeyEvent.VK_DOWN)),
				Pair.with(Direction.West, key(KeyEvent.VK_LEFT)),
				Pair.with(Direction.East, key(KeyEvent.VK_RIGHT)))
			.collect(Collectors.toMap(Pair::getValue0, Pair::getValue1));

		Map<Direction, KeyStroke> numKeys = Stream.of(
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
		JButton explorerChangeButton = new ListenedButton("Select a different explorer",
			ignored -> explorerChangeButtonListener.run());

		JLabel remainingMPLabel = new JLabel("Remaining Movement Points:");
		JSpinner mpField = new JSpinner(mpModel);
		mpField.setMaximumSize(new Dimension(Short.MAX_VALUE,
			(int) mpField.getPreferredSize().getHeight()));

		JLabel speedLabel = new JLabel("Current relative speed:");

		speedSource = () -> (Speed) speedModel.getSelectedItem();
		ImprovedComboBox<Speed> speedBox = new ImprovedComboBox<>(speedModel);

		headerPanel.add(explorerChangeButton);
		headerPanel.add(locLabel);
		headerPanel.add(remainingMPLabel);
		headerPanel.add(mpField);
		headerPanel.add(speedLabel);
		headerPanel.add(speedBox);

		LOGGER.finer("ExplorationPanel: headerPanel contents added");

		headerLayout.setHorizontalGroup(
			headerLayout.sequentialGroupOf(explorerChangeButton, locLabel,
				remainingMPLabel, mpField, speedLabel, speedBox));
			headerLayout.setVerticalGroup(headerLayout.parallelGroupOf(explorerChangeButton,
				locLabel, remainingMPLabel, mpField, speedLabel, speedBox));

		LOGGER.finer("ExplorationPanel: headerPanel layout adjusted");

		// TODO: Add 'secondMap' field (i.e. getter/setter, thinking
		// about extra logic needed in the setter) to IExplorationModel
		// (as IMap), to improve no-second-map to a-second-map
		// transition
		IMapNG secondMap = StreamSupport.stream(driverModel.getSubordinateMaps().spliterator(),
			false).findFirst().orElseGet(driverModel::getMap);

		IDRegistrar idf = new IDFactoryFiller().createIDFactory(StreamSupport.stream(
			driverModel.getAllMaps().spliterator(), false).toArray(IMapNG[]::new));
		huntingModel = new HuntingModel(driverModel.getMap());

		LOGGER.finer("ExplorationPanel: huntingModel created");

		final IFixtureEditHelper feh = new FixtureEditHelper(driverModel);

		for (Direction direction : Direction.values()) {
			LOGGER.finer("ExplorationPanel: Starting to initialize for " + direction);
			FixtureList mainList = new FixtureList(tilesPanel,
				new FixtureListModel(driverModel.getMap()::getFixtures,
					driverModel.getMap()::getBaseTerrain,
					driverModel.getMap()::getRivers, driverModel.getMap()::isMountainous,
					this::tracksCreator, null, null, null, null, null, null,
					Comparator.naturalOrder()), // TODO: Replace nulls with implementations?
				feh, idf, driverModel.getMap().getPlayers());
			tilesPanel.add(new JScrollPane(mainList));

			LOGGER.finer("ExplorationPanel: main list set up for " + direction);

			DualTileButton dtb = new DualTileButton(driverModel.getMap(), secondMap, matchers);
			// At some point we tried wrapping the button in a JScrollPane.
			tilesPanel.add(dtb);
			LOGGER.finer("ExplorationPanel: Added button for " + direction);

			ExplorationClickListener ecl = new ExplorationClickListener(driverModel, this, this::movementDeductionTracker,
					direction, mainList);
			if (Direction.Nowhere.equals(direction)) {
				dtb.setComponentPopupMenu(ecl.getExplorerActionsMenu());
			}
			createHotKey(dtb, direction.toString(), ecl, JComponent.WHEN_IN_FOCUSED_WINDOW,
				Stream.of(arrowKeys.get(direction), numKeys.get(direction))
					.filter(k -> k != null).toArray(KeyStroke[]::new));
			dtb.addActionListener(ecl);

			RandomDiscoverySelector ell = new RandomDiscoverySelector(driverModel,
				mainList, speedSource);

			// mainList.model.addListDataListener(ell);
			driverModel.addSelectionChangeListener(ell);
			ecl.addSelectionChangeListener(ell);

			LOGGER.finer("ExplorationPanel: ell set up for " + direction);

			FixtureList secList = new FixtureList(tilesPanel,
				new FixtureListModel(secondMap::getFixtures, secondMap::getBaseTerrain,
					secondMap::getRivers, secondMap::isMountainous, this::createNull,
					driverModel::setSubMapTerrain, driverModel::copyRiversToSubMaps,
					driverModel::setMountainousInSubMap, driverModel::copyToSubMaps,
					driverModel::removeRiversFromSubMaps,
					driverModel::removeFixtureFromSubMaps, Comparator.naturalOrder()),
				feh, idf, secondMap.getPlayers());
			tilesPanel.add(new JScrollPane(secList));

			LOGGER.finer("ExploratonPanel: Second list set up for " + direction);

			SpeedChangeListener scl = new SpeedChangeListener(ell);
			speedModel.addListDataListener(scl);
			speedChangeListeners.put(direction, scl);

			mains.put(direction, mainList);
			buttons.put(direction, dtb);
			seconds.put(direction, secList);
			ell.selectedPointChanged(null, driverModel.getSelectedUnitLocation());
			LOGGER.finer("ExplorationPanel: Done with " + direction);
		}
		LOGGER.finer("End of ExplorationPanel initializer");
	}

	// TODO: Cache selected unit here instead of always referring to it via the model?
	@Override
	public void selectedUnitChanged(@Nullable final IUnit old, @Nullable final IUnit newSelection) {}

	private final FormattedLabel locLabel = new FormattedLabel(
		"<html><body>Currently exploring %s; click a tile to explore it. Selected fixtures in its left-hand list will be 'discovered'.</body></html>", Point.INVALID_POINT);

	private final Map<Direction, SelectionChangeListener> mains = new HashMap<>();
	private final Map<Direction, SelectionChangeListener> seconds = new HashMap<>();
	private final Map<Direction, DualTileButton> buttons = new HashMap<>();

	private final Iterable<FixtureMatcher> matchers = new FixtureFilterTableModel();

	// TODO: What do we need to do to make this static?
	private class SpeedChangeListener implements ListDataListener {
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

	private final Map<Direction, SpeedChangeListener> speedChangeListeners = new HashMap<>();

	@Override
	public void selectedPointChanged(@Nullable final Point old, final Point newPoint) {
		if (old != null && old.equals(newPoint)) {
			return;
		}
		LOGGER.finer("In ExplorationPanel.selectedPointChanged");
		for (Direction direction : Direction.values()) {
			LOGGER.finer("ExplorationPanel.selectedPointChanged: Beginning " + direction);
			Point point = driverModel.getDestination(newPoint, direction);
			@Nullable Point previous;
			if (speedChangeListeners.containsKey(direction)) {
				// TODO: Change SpeedChangeListener API so we can do this in one operation
				SpeedChangeListener scl = speedChangeListeners.get(direction);
				previous = scl.getPoint();
				scl.setPoint(point);
			} else {
				previous = old;
			}
			Consumer<SelectionChangeListener> c = l -> l.selectedPointChanged(previous, point);
			Optional.ofNullable(mains.get(direction)).ifPresent(c);
			Optional.ofNullable(seconds.get(direction)).ifPresent(c);
			Optional.ofNullable(buttons.get(direction)).ifPresent(b -> b.setPoint(point));
			LOGGER.finer("ExplorationPanel.selectedPointChanged: Ending " + direction);
		}
		locLabel.setArguments(newPoint);
	}

	// TODO: Move as many fields as possible into the constructor, to minimize class footprint

	@Nullable
	private AnimalTracks tracksCreator(final Point point) {
		TileType terrain = driverModel.getMap().getBaseTerrain(point);
		if (terrain != null) { // TODO: invert
			LOGGER.finer("In ExplorationPanel.tracksCreator");
			Function<Point, Iterable<Pair<Point, TileFixture>>> source;
			if (TileType.Ocean.equals(terrain)) {
				source = huntingModel::fish;
			} else {
				source = huntingModel::hunt;
			}
			LOGGER.finer("ExplorationPanel.tracksCreator: Determined which source to use");
			TileFixture animal = source.apply(point).iterator().next().getValue1();
			LOGGER.finer("ExplorationPanel.tracksCreator: Got first item from source");
			if (animal instanceof Animal) {
				return new AnimalTracks(((Animal) animal).getKind());
			} else if (animal instanceof AnimalTracks) {
				return ((AnimalTracks) animal).copy(true);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	// TODO: Try to make this static
	private class ExplorationClickListener implements SelectionChangeSource, ActionListener {
		public ExplorationClickListener(final IExplorationModel driverModel, final SelectionChangeListener outer,
		                                final MovementCostListener movementDeductionTracker, final Direction direction,
		                                final FixtureList mainList) {
			this.direction = direction;
			this.mainList = mainList;
			this.driverModel = driverModel;
			this.outer = outer;
			this.movementDeductionTracker = movementDeductionTracker;
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

		private final List<SelectionChangeListener> selectionListeners = new ArrayList<>();

		@Override
		public void addSelectionChangeListener(final SelectionChangeListener listener) {
			selectionListeners.add(listener);
		}

		@Override
		public void removeSelectionChangeListener(final SelectionChangeListener listener) {
			selectionListeners.remove(listener);
		}

		private List<TileFixture> getSelectedValuesList() {
			int[] selections = mainList.getSelectedIndices();
			ListModel<TileFixture> listModel = mainList.getModel();
			List<TileFixture> retval = new ArrayList<>();
			for (int index : selections) {
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
			Point destPoint = driverModel.getSelectedUnitLocation();
			Player player = Optional.ofNullable(driverModel.getSelectedUnit())
				.map(IUnit::getOwner).orElse(new PlayerImpl(- 1, "no-one"));

			driverModel.copyTerrainToSubMaps(destPoint);
			for (TileFixture fixture : fixtures) {
				if (fixture instanceof FakeFixture) {
					// Skip it! It'll corrupt the output XML!
					continue;
				} else {
					boolean zero;
					if (fixture instanceof HasOwner &&
							(!player.equals(((HasOwner) fixture).getOwner()) ||
								fixture instanceof Village)) {
						zero = true;
					} else {
						zero = fixture instanceof HasPopulation ||
							fixture instanceof HasExtent;
					}
					driverModel.copyToSubMaps(destPoint, fixture, zero);
				}
			}
		}

		/**
		 * The action of searching the current tile, since on moving
		 * 'nowhere' the listener now aborts its normal process.
		 */
		private void searchCurrentTile() {
			List<TileFixture> fixtures = getSelectedValuesList();
			try {
				driverModel.move(Direction.Nowhere, speedSource.get());
			} catch (final TraversalImpossibleException except) {
				LOGGER.log(Level.SEVERE, "\"Traversal impossible\" going nowhere", except);
			}
			discoverFixtures(fixtures);
		}

		/**
		 * A menu of actions the explorer can take when moving 'nowhere'."
		 */
		public JPopupMenu getExplorerActionsMenu() {
			return explorerActionsMenu;
		}

		private final JPopupMenu explorerActionsMenu;

		private void actionPerformedImpl() {
			try {
				List<TileFixture> fixtures = getSelectedValuesList();
				if (Direction.Nowhere.equals(direction)) {
					explorerActionsMenu.show(mainList, mainList.getWidth(), 0);
				} else {
					driverModel.move(direction, speedSource.get());
					discoverFixtures(fixtures);
				}
			} catch (final TraversalImpossibleException except) {
				LOGGER.log(Level.FINE, "Attempted movement to impassable destination",
					except);
				Point selection = driverModel.getSelectedUnitLocation();
				outer.selectedPointChanged(null, selection);
				for (SelectionChangeListener listener : selectionListeners) {
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

	@Nullable
	private AnimalTracks createNull(final Point point) {
		return null;
	}

	@Override
	public void interactionPointChanged() {}
	@Override
	public void cursorPointChanged(@Nullable final Point oldCursor, final Point newCursor) {}
}
