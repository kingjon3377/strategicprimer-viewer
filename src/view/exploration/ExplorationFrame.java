package view.exploration;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.exploration.ExplorationModel;
import model.exploration.ExplorationUnitListModel;
import model.exploration.IExplorationModel.Direction;
import model.exploration.PlayerListModel;
import model.map.IMap;
import model.map.Player;
import model.map.Point;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import model.map.fixtures.mobile.Unit;
import util.IsNumeric;
import util.Pair;
import util.PropertyChangeSource;
import util.PropertyChangeSupportSource;
import view.map.details.FixtureList;

/**
 * The main window for the exploration GUI.
 *
 * FIXME: Too many methods; move some of the inline anonymous classes to their
 * own files.
 *
 * @author Jonathan Lovelace
 */
public class ExplorationFrame extends JFrame implements PropertyChangeSource,
		ListSelectionListener, PropertyChangeListener {
	/**
	 * A list-data-listener to select a random but suitable set of fixtures to be 'discovered' if the tile is explored.
	 * @author Jonathan Lovelace
	 *
	 */
	private final class ExplorationListListener implements ListDataListener {
		/**
		 * The list this is attached to.
		 */
		private final FixtureList list;
		/**
		 * Constructor.
		 * @param mainList the list this is attached to
		 */
		ExplorationListListener(final FixtureList mainList) {
			list = mainList;
		}
		/**
		 * @param evt an event indicating items were removed from the list
		 */
		@Override
		public void intervalRemoved(final ListDataEvent evt) {
			randomizeSelection();
		}
		/**
		 * @param evt an event indicating items were added to the list
		 */
		@Override
		public void intervalAdded(final ListDataEvent evt) {
			randomizeSelection();
		}
		/**
		 * @param evt an event indicating items were changed in the list
		 */
		@Override
		public void contentsChanged(final ListDataEvent evt) {
			randomizeSelection();
		}
		/**
		 * Select a suitable but randomized selection of fixtures.
		 */
		private void randomizeSelection() {
			list.clearSelection();
			final List<Pair<Integer, TileFixture>> constants = new ArrayList<Pair<Integer, TileFixture>>();
			final List<Pair<Integer, TileFixture>> possibles = new ArrayList<Pair<Integer, TileFixture>>();
			for (int i = 0; i < list.getModel().getSize(); i++) {
				final TileFixture fix = list.getModel().getElementAt(i);
				if (ExplorationCLI.shouldAlwaysNotice(
						model.getSelectedUnit(), fix)) {
					constants.add(Pair.of(Integer.valueOf(i), fix));
				} else if (ExplorationCLI.mightNotice(
						model.getSelectedUnit(), fix)) {
					possibles.add(Pair.of(Integer.valueOf(i), fix));
				}
			}
			Collections.shuffle(possibles);
			if (!possibles.isEmpty()) {
				constants.add(possibles.get(0));
			}
			final int[] indices = new int[constants.size()];
			for (int i = 0; i < constants.size(); i++) {
				indices[i] = constants.get(i).first().intValue();
			}
			list.setSelectedIndices(indices);
		}
	}
	/**
	 * The listener for clicks on tile buttons indicating movement.
	 * @author Jonathan Lovelace
	 *
	 */
	private final class TileClickListener implements ActionListener {
		/**
		 * The direction this button is from the currently selected tile.
		 */
		private final Direction direction;
		/**
		 * The list of fixtures on this tile in the main map.
		 */
		private final FixtureList list;
		/**
		 * Constructor.
		 * @param direct what direction this button is from the center.
		 * @param mainList the list of fixtures on this tile in the main map.
		 */
		TileClickListener(final Direction direct, final FixtureList mainList) {
			direction = direct;
			list = mainList;
		}
		/**
		 * @param evt the event to handle.
		 *
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(final ActionEvent evt) {
			try {
				final List<TileFixture> fixtures = list
						.getSelectedValuesList();
				model.move(direction);
				for (final Pair<IMap, String> pair : model
						.getSubordinateMaps()) {
					final IMap map = pair.first();
					final Tile tile = map.getTile(model
							.getSelectedUnitLocation());
					for (final TileFixture fix : fixtures) {
						tile.addFixture(fix);
					}
				}
			} catch (TraversalImpossibleException except) {
				propertyChange(new PropertyChangeEvent(this, "point", null,
						model.getSelectedUnitLocation()));
				propertyChange(new PropertyChangeEvent(this, "cost",
						Integer.valueOf(0), Integer.valueOf(1)));
			}
		}
	}
	/**
	 * The list of players.
	 */
	private final JList<Player> playerList;
	/**
	 * The exploration model.
	 */
	protected final ExplorationModel model;

	/**
	 * Constructor.
	 *
	 * @param emodel the exploration model
	 */
	public ExplorationFrame(final ExplorationModel emodel) {
		super("Strategic Primer Exploration");
		model = emodel;
		setMinimumSize(new Dimension(768, 480));
		setPreferredSize(new Dimension(1024, 640));
		final Container outer = getContentPane();
		final CardLayout layout = new CardLayout();
		setLayout(layout);
		final JPanel uspFirst = new JPanel(new BorderLayout());
		uspFirst.add(new JLabel("Players in all maps:"), BorderLayout.NORTH);
		playerList = new JList<Player>(new PlayerListModel(emodel));
		playerList.addListSelectionListener(this);
		uspFirst.add(playerList, BorderLayout.CENTER);
		final JPanel uspSecond = new JPanel(new BorderLayout());
		uspSecond
				.add(new JLabel(
						"<html><body><p>Units belonging to that player:</p>"
								+ "<p>(Selected unit will be used for exploration.)</p></body></html>"),
						BorderLayout.NORTH);
		final JList<Unit> unitList = new JList<Unit>(
				new ExplorationUnitListModel(emodel, this));
		uspSecond.add(unitList, BorderLayout.CENTER);
		final JPanel mpPanel = new JPanel(new BorderLayout());
		mpPanel.add(new JLabel("Unit's Movement Points: "), BorderLayout.WEST);
		mpPanel.add(mpField, BorderLayout.EAST);
		final JButton explButton = new JButton("Start exploring!");
		final JSplitPane explorationPanel = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT);
		explButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				if (!unitList.isSelectionEmpty()) {
					layout.next(outer);
					explorationPanel.validate();
					emodel.selectUnit(unitList.getSelectedValue());
				}
			}
		});
		mpPanel.add(explButton, BorderLayout.SOUTH);
		uspSecond.add(mpPanel, BorderLayout.SOUTH);
		final JSplitPane unitSelPanel = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, uspFirst, uspSecond);
		unitSelPanel.setDividerLocation(0.5);
		unitSelPanel.setResizeWeight(0.5);
		add(unitSelPanel);

		final JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.LINE_AXIS));
		final JButton backButton = new JButton("Select a different explorer");
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				unitSelPanel.validate();
				layout.first(outer);
			}
		});
		headerPanel.add(backButton);
		headerPanel.add(locLabel);
		headerPanel.add(new JLabel("Remaining Movement Points: "));
		headerPanel.add(new JTextField(mpField.getDocument(), null, 5));
		explorationPanel.setTopComponent(headerPanel);
		final JPanel tilePanel = new JPanel(new GridLayout(3, 12, 2, 2));
		addTileGUI(tilePanel, Direction.Northwest);
		addTileGUI(tilePanel, Direction.North);
		addTileGUI(tilePanel, Direction.Northeast);
		addTileGUI(tilePanel, Direction.West);
		addTileGUI(tilePanel, Direction.Nowhere);
		addTileGUI(tilePanel, Direction.East);
		addTileGUI(tilePanel, Direction.Southwest);
		addTileGUI(tilePanel, Direction.South);
		addTileGUI(tilePanel, Direction.Southeast);
		explorationPanel.setBottomComponent(tilePanel);
		add(explorationPanel);
		emodel.addPropertyChangeListener(this);
		pack();
	}

	/**
	 * The collection of proxies for main-map tile-fixture-lists.
	 */
	private final EnumMap<Direction, PropertyChangeSupportSource> mains = new EnumMap<Direction, PropertyChangeSupportSource>(
			Direction.class);
	/**
	 * The collection of proxies for secondary-map tile-fixture lists.
	 */
	private final EnumMap<Direction, PropertyChangeSupportSource> seconds = new EnumMap<Direction, PropertyChangeSupportSource>(
			Direction.class);
	/**
	 * The collection of dual-tile-buttons.
	 */
	private final EnumMap<Direction, DualTileButton> buttons = new EnumMap<Direction, DualTileButton>(
			Direction.class);

	/**
	 * Set up the GUI representation of a tile---a list of its contents in the
	 * main map, a visual representation, and a list of its contents in a
	 * secondary map.
	 *
	 * @param panel the panel to add them to
	 * @param direction which direction from the currently selected tile this
	 *        GUI represents.
	 */
	private void addTileGUI(final JPanel panel, final Direction direction) {
		final PropertyChangeSupportSource mainPCS = new PropertyChangeSupportSource(
				this);
		final FixtureList mainList = new FixtureList(panel, mainPCS);
		panel.add(new JScrollPane(mainList));
		final DualTileButton dtb = new DualTileButton();
		// panel.add(new JScrollPane(dtb));
		panel.add(dtb);
		dtb.addActionListener(new TileClickListener(direction, mainList));
		mainList.getModel().addListDataListener(new ExplorationListListener(mainList));
		final PropertyChangeSupportSource secPCS = new PropertyChangeSupportSource(
				this);
		panel.add(new JScrollPane(new FixtureList(panel, secPCS)));
		mains.put(direction, mainPCS);
		buttons.put(direction, dtb);
		seconds.put(direction, secPCS);
	}

	/**
	 * Handle the user selecting a different player.
	 *
	 * @param evt event
	 */
	@Override
	public void valueChanged(final ListSelectionEvent evt) {
		firePropertyChange("player", null, playerList.getSelectedValue());
	}

	/**
	 * Handle change in selected location.
	 *
	 * @param evt the event to handle
	 */
	@Override
	public final void propertyChange(final PropertyChangeEvent evt) {
		if ("point".equalsIgnoreCase(evt.getPropertyName())) {
			final Point selPoint = model.getSelectedUnitLocation();
			for (final Direction dir : Direction.values()) {
				final Point point = model.getDestination(selPoint, dir);
				final Tile tileOne = model.getMap().getTile(point);
				final Iterator<Pair<IMap, String>> subs = model
						.getSubordinateMaps().iterator();
				// ESCA-JAVA0177:
				final Tile tileTwo; // NOPMD
				if (subs.hasNext()) {
					tileTwo = subs.next().first().getTile(point);
				} else {
					tileTwo = new Tile(TileType.NotVisible); // NOPMD
				}
				mains.get(dir).firePropertyChange("tile", null, tileOne);
				seconds.get(dir).firePropertyChange("tile", null, tileTwo);
				buttons.get(dir).setTiles(tileOne, tileTwo);

			}
			locLabel.setText("<html><body>Currently exploring "
					+ model.getSelectedUnitLocation()
					+ "; click a tile to explore it. "
					+ "Selected fixtures in its left-hand list will be 'discovered'.</body></html>");
		} else if ("cost".equalsIgnoreCase(evt.getPropertyName())
				&& evt.getNewValue() instanceof Integer) {
			final int cost = ((Integer) evt.getNewValue()).intValue();
			if (IsNumeric.isNumeric(mpField.getText().trim())) {
				int mpoints = Integer.parseInt(mpField.getText().trim());
				mpoints -= cost;
				mpField.setText(Integer.toString(mpoints));
			}
		}
	}
	/**
	 * The text-field containing the running MP total.
	 */
	private final JTextField mpField = new JTextField(5);
	/**
	 * The label showing the current location of the explorer.
	 */
	private final JLabel locLabel = new JLabel(
			"<html><body>Currently exploring (-1, -1); click a tile to explore it. "
					+ "Selected fixtures in its left-hand list will be 'discovered'.</body></html>");
}
