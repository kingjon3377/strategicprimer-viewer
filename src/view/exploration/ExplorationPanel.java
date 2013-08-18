package view.exploration;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.text.Document;

import model.exploration.IExplorationModel;
import model.exploration.IExplorationModel.Direction;
import model.map.IMap;
import model.map.Point;
import model.map.Tile;
import model.map.TileType;
import util.IsNumeric;
import util.Pair;
import util.PropertyChangeSupportSource;
import view.map.details.FixtureList;
import view.util.BorderedPanel;
import view.util.BoxPanel;
import view.util.ListenedButton;
/**
 * A panel to let the user explore using a unit.
 * @author Jonathan Lovelace
 */
public class ExplorationPanel extends BorderedPanel implements ActionListener, PropertyChangeListener {
	/**
	 * The exploration model.
	 */
	private final IExplorationModel model;
	/**
	 * The text for the 'back' button.
	 */
	private static final String BACK_TEXT = "Select a different explorer";
	/**
	 * Constructor.
	 * @param emodel the exploration model.
	 * @param mpDoc the model underlying the remaining-MP text boxes.
	 */
	public ExplorationPanel(final IExplorationModel emodel, final Document mpDoc) {
		super();
		model = emodel;
		final BoxPanel headerPanel = new BoxPanel(true);
		headerPanel.add(new ListenedButton(BACK_TEXT, this));
		headerPanel.add(locLabel);
		headerPanel.add(new JLabel("Remaining Movement Points: "));
		mpField = new JTextField(mpDoc, null, 5);
		// TODO: store the reference to the document, not the text field, in the class body.
		headerPanel.add(mpField);
		/**
		 * TODO: Make the tilePanel and its logic a separate class.
		 */
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
		setCenter(new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				headerPanel, tilePanel));
		emodel.addPropertyChangeListener(this);
	}
	/**
	 * Handle a button press.
	 * @param evt the event to handle.
	 */
	@Override
	public void actionPerformed(final ActionEvent evt) {
		if (BACK_TEXT.equalsIgnoreCase(evt.getActionCommand())) {
			firePropertyChange("switch", false, true);
		}
	}
	/**
	 * The label showing the current location of the explorer.
	 */
	private final JLabel locLabel = new JLabel(
			"<html><body>Currently exploring (-1, -1); click a tile to explore it. "
					+ "Selected fixtures in its left-hand list will be 'discovered'.</body></html>");
	/**
	 * The text-field containing the running MP total.
	 */
	private final JTextField mpField;
	/**
	 * The collection of proxies for main-map tile-fixture-lists.
	 */
	private final EnumMap<Direction, PropertyChangeSupportSource> mains = new EnumMap<>(
			Direction.class);
	/**
	 * The collection of proxies for secondary-map tile-fixture lists.
	 */
	private final EnumMap<Direction, PropertyChangeSupportSource> seconds = new EnumMap<>(
			Direction.class);
	/**
	 * The collection of dual-tile-buttons.
	 */
	private final EnumMap<Direction, DualTileButton> buttons = new EnumMap<>(
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
		final FixtureList mainList = new FixtureList(panel, model.getMap()
				.getPlayers(), mainPCS);
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
		panel.add(new JScrollPane(new FixtureList(panel, model
				.getSubordinateMaps().iterator().next().first().getPlayers(),
				secPCS)));
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
				buttons.get(dir).repaint();
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
}
