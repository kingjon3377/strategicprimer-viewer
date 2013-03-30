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
import java.util.EnumMap;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.exploration.ExplorationModel;
import model.exploration.ExplorationUnitListModel;
import model.exploration.IExplorationModel;
import model.exploration.IExplorationModel.Direction;
import model.exploration.PlayerListModel;
import model.map.IMap;
import model.map.Player;
import model.map.Point;
import model.map.Tile;
import model.map.TileType;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import model.map.fixtures.mobile.Unit;
import util.Pair;
import util.PropertyChangeSource;
import util.PropertyChangeSupportSource;
import view.map.details.FixtureList;
/**
 * The main window for the exploration GUI.
 * @author Jonathan Lovelace
 */
public class ExplorationFrame extends JFrame implements PropertyChangeSource,
		ListSelectionListener, PropertyChangeListener {
	/**
	 * The list of players.
	 */
	private final JList<Player> playerList;
	/**
	 * The exploration  model.
	 */
	private final ExplorationModel model;
	/**
	 * Constructor.
	 * @param emodel the exploration model
	 */
	public ExplorationFrame(final ExplorationModel emodel) {
		model = emodel;
		setMinimumSize(new Dimension(640, 480));
		final Container outer = getContentPane();
		final CardLayout layout = new CardLayout();
		setLayout(layout);
		final JPanel unitSelPanel = new JPanel();
		unitSelPanel.setLayout(new BoxLayout(unitSelPanel, BoxLayout.LINE_AXIS));
		final JPanel uspFirst = new JPanel();
		uspFirst.setLayout(new BoxLayout(uspFirst, BoxLayout.PAGE_AXIS));
		uspFirst.add(new JLabel("Players in all maps:"));
		playerList = new JList<Player>(new PlayerListModel(emodel));
		playerList.addListSelectionListener(this);
		uspFirst.add(playerList);
		unitSelPanel.add(uspFirst);
		final JPanel uspSecond = new JPanel();
		uspSecond.setLayout(new BoxLayout(uspSecond, BoxLayout.PAGE_AXIS));
		uspSecond.add(new JLabel("Units belonging to that player:"));
		uspSecond.add(new JLabel("(Selected unit will be used for exploration.)"));
		final JList<Unit> unitList = new JList<Unit>(new ExplorationUnitListModel(emodel, this));
		uspSecond.add(unitList);
		final JPanel mpPanel = new JPanel();
		mpPanel.setLayout(new BoxLayout(mpPanel, BoxLayout.LINE_AXIS));
		mpPanel.add(new JLabel("Unit's Movement Points: "));
		/**
		 * The field storing the unit's available movement points.
		 */
		final JTextField mpField = new JTextField(5);
		mpPanel.add(mpField);
		uspSecond.add(mpPanel);
		final JButton explButton = new JButton("Start exploring!");
		explButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				if (!unitList.isSelectionEmpty()) {
					layout.next(outer);
					emodel.selectUnit(unitList.getSelectedValue());
				}
			}
		});
		uspSecond.add(explButton);
		unitSelPanel.add(uspSecond);
		add(unitSelPanel);

		final JPanel explorationPanel = new JPanel(new BorderLayout());
		final JPanel headerPanel = new JPanel();
		final JButton backButton = new JButton("Select a different explorer");
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				layout.first(outer);
			}
		});
		headerPanel.add(backButton);
		final IExplorationModel labelModel = emodel;
		headerPanel.add(new JLabel("text") {
			@Override
			public String getText() {
				return "<html><body>Currently exploring "
						+ labelModel.getSelectedUnitLocation()
						+ "; click a tile to explore it.</body></html>";
		}
		});
		final JTextField mpField2 = new JTextField(mpField.getDocument(), null, 5);
		final JPanel mpPanel2 = new JPanel();
		headerPanel.add(new JLabel("Remaining Movement Points: "));
		headerPanel.add(mpField2);
		explorationPanel.add(mpPanel2, BorderLayout.NORTH);
		final JPanel tilePanel = new JPanel(new GridLayout(3, 12, 2, 2));
		addTileGUI(tilePanel, emodel, Direction.Northwest);
		addTileGUI(tilePanel, emodel, Direction.North);
		addTileGUI(tilePanel, emodel, Direction.Northeast);
		addTileGUI(tilePanel, emodel, Direction.West);
		addTileGUI(tilePanel, emodel, Direction.Nowhere);
		addTileGUI(tilePanel, emodel, Direction.East);
		addTileGUI(tilePanel, emodel, Direction.Southwest);
		addTileGUI(tilePanel, emodel, Direction.South);
		addTileGUI(tilePanel, emodel, Direction.Southeast);
		explorationPanel.add(tilePanel, BorderLayout.SOUTH);
		add(explorationPanel);
		emodel.addPropertyChangeListener(this);
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
	private final EnumMap<Direction, DualTileButton> buttons = new EnumMap<Direction, DualTileButton>(Direction.class);
	/**
	 * Set up the GUI representation of a tile---a list of its contents in the
	 * main map, a visual representation, and a list of its contents in a
	 * secondary map.
	 *
	 * @param panel the panel to add them to
	 * @param emodel the exploration model to refer to
	 * @param direction which direction from the currently selected tile this
	 *        GUI represents.
	 */
	private void addTileGUI(final JPanel panel, final ExplorationModel emodel,
			final Direction direction) {
		final PropertyChangeSupportSource mainPCS = new PropertyChangeSupportSource(this);
		panel.add(new JScrollPane(new FixtureList(panel, mainPCS)));
		final DualTileButton dtb = new DualTileButton();
		panel.add(new JScrollPane(dtb));
		dtb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				try {
					emodel.move(direction);
				} catch (TraversalImpossibleException except) {
					propertyChange(new PropertyChangeEvent(this, "point", null, emodel.getSelectedUnitLocation()));
				}
			}
		});
		final PropertyChangeSupportSource secPCS = new PropertyChangeSupportSource(this);
		panel.add(new JScrollPane(new FixtureList(panel, secPCS)));
		mains.put(direction, mainPCS);
		buttons.put(direction, dtb);
		seconds.put(direction, secPCS);
	}
	/**
	 * Handle the user selecting a different player.
	 * @param evt event
	 */
	@Override
	public void valueChanged(final ListSelectionEvent evt) {
		firePropertyChange("player", null, playerList.getSelectedValue());
	}
	/**
	 * Handle change in selected location.
	 * @param evt the event to handle
	 */
	@Override
	public final void propertyChange(final PropertyChangeEvent evt) {
		if ("point".equalsIgnoreCase(evt.getPropertyName())) {
			final Point selPoint = model.getSelectedUnitLocation();
			for (final Direction dir : Direction.values()) {
				final Point point = model.getDestination(selPoint, dir);
				final Tile tileOne = model.getMap().getTile(point);
				final Iterator<Pair<IMap, String>> subs = model.getSubordinateMaps().iterator();
				// ESCA-JAVA0177:
				final Tile tileTwo; // NOPMD
				if (subs.hasNext()) {
					tileTwo = subs.next().first().getTile(point);
				} else {
					tileTwo = new Tile(TileType.NotVisible);
				}
				mains.get(dir).firePropertyChange("tile", null, tileOne);
				seconds.get(dir).firePropertyChange("tile", null, tileTwo);
				buttons.get(dir).setTiles(tileOne, tileTwo);

			}
		}
	}
}
