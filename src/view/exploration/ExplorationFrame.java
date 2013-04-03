package view.exploration;

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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import model.exploration.ExplorationModel;
import model.exploration.IExplorationModel.Direction;
import model.map.IMap;
import model.map.Point;
import model.map.Tile;
import model.map.TileType;
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
		PropertyChangeListener {
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
		final ExplorerSelectingPanel esp = new ExplorerSelectingPanel(emodel);
		final JSplitPane explorationPanel = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT);
		esp.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				if ("switch".equalsIgnoreCase(evt.getPropertyName())) {
					explorationPanel.validate();
					layout.next(outer);
				}
			}
		});
		add(esp);

		final JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.LINE_AXIS));
		final JButton backButton = new JButton("Select a different explorer");
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				esp.validate();
				layout.first(outer);
			}
		});
		headerPanel.add(backButton);
		headerPanel.add(locLabel);
		headerPanel.add(new JLabel("Remaining Movement Points: "));
		mpField = new JTextField(esp.getMPDocument(), null, 5);
		headerPanel.add(mpField);
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
		final ExplorationClickListener ecl = new ExplorationClickListener(model, direction, mainList);
		dtb.addActionListener(ecl);
		ecl.addPropertyChangeListener(this);
		mainList.getModel().addListDataListener(new ExplorationListListener(model, mainList));
		final PropertyChangeSupportSource secPCS = new PropertyChangeSupportSource(
				this);
		panel.add(new JScrollPane(new FixtureList(panel, secPCS)));
		mains.put(direction, mainPCS);
		buttons.put(direction, dtb);
		seconds.put(direction, secPCS);
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
	private final JTextField mpField;
	/**
	 * The label showing the current location of the explorer.
	 */
	private final JLabel locLabel = new JLabel(
			"<html><body>Currently exploring (-1, -1); click a tile to explore it. "
					+ "Selected fixtures in its left-hand list will be 'discovered'.</body></html>");
}
