package view.map.main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import model.exploration.ExplorationRunner;
import model.viewer.Fortress;
import model.viewer.Tile;
import model.viewer.TileFixture;
import model.viewer.TileType;
import model.viewer.Unit;
import model.viewer.events.AbstractEvent;
import view.util.SizeLimiter;

/**
 * A panel to show the details of a tile. FIXME: If the map includes the name of
 * a player, it should show that player's name in addition to his or her number.
 * 
 * @author Jonathan Lovelace
 */
public class DetailPanel extends JPanel implements ActionListener {
	/**
	 * Command to save changed results.
	 */
	private static final String RESULTS_SAVE_CMD = "<html><p>Save changed results</p></html>";
	/**
	 * Height of the chit panel as a ratio to its parent's height.
	 */
	private static final double CHIT_PANEL_HEIGHT = 0.6;
	/**
	 * Width of the chit panel as a ratio to its parent's width.
	 */
	private static final double CHIT_PANEL_WIDTH = 0.5;
	/**
	 * Height of the results button as a ratio to its parent's height.
	 */
	private static final double RESULTS_BUTTON_HEIGHT = 0.2; // NOPMD
	/**
	 * Width of the results field as a ratio to its parent's width.
	 */
	private static final double RESULTS_FIELD_WIDTH = 0.5; // NOPMD
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 3391860564156014799L;
	/**
	 * Label to show the tile's terrain type.
	 */
	private final JLabel typeLabel = new JLabel();
	/**
	 * Label to show the details of the selected chit.
	 */
	private final JLabel chitDetail = new JLabel();
	/**
	 * Field to show and edit exploration results.
	 */
	private final JTextArea resultsField = new JTextArea();
	/**
	 * Exploration runner to produce exploration results.
	 */
	private final ExplorationRunner runner = new ExplorationRunner();

	/**
	 * Constructor.
	 */
	public DetailPanel() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		final JPanel typePanel = new JPanel(new BorderLayout());
		typePanel.add(new JLabel("<html>Coordinates:<br />Tile type:</html>"), BorderLayout.WEST);
		typePanel.add(typeLabel, BorderLayout.CENTER);
		add(typePanel);
		addComponentListener(new SizeLimiter(typePanel, 1.0, 0.2));
		final JPanel viewPanel = new JPanel(new BorderLayout());
		final JPanel chitSuperPanel = new JPanel(new BorderLayout());
		chitSuperPanel.add(chitPanel, BorderLayout.WEST);
		chitSuperPanel.addComponentListener(new SizeLimiter(chitPanel,
				CHIT_PANEL_WIDTH, 1.0));
		chitSuperPanel.add(chitDetail, BorderLayout.EAST);
		chitSuperPanel.addComponentListener(new SizeLimiter(chitDetail,
				1.0 - CHIT_PANEL_WIDTH, 1.0));
		viewPanel.add(chitSuperPanel, BorderLayout.NORTH);
		viewPanel.addComponentListener(new SizeLimiter(chitSuperPanel, 1.0,
				CHIT_PANEL_HEIGHT));
		final JPanel resultsPanel = new JPanel(new BorderLayout());
		final JLabel resultsLabel = new JLabel(
				"<html>Exploration<br>results</html>");
		resultsPanel.add(resultsLabel, BorderLayout.WEST);
		resultsPanel.addComponentListener(new SizeLimiter(resultsLabel,
				1.0 - RESULTS_FIELD_WIDTH, 1.0 - RESULTS_BUTTON_HEIGHT));
		resultsField.setLineWrap(true);
		resultsField.setEditable(true);
		resultsField.setWrapStyleWord(true);
		resultsPanel.add(resultsField, BorderLayout.CENTER);
		resultsPanel.addComponentListener(new SizeLimiter(resultsField,
				RESULTS_FIELD_WIDTH, 1.0 - RESULTS_BUTTON_HEIGHT));
		final JButton resultsButton = new JButton(RESULTS_SAVE_CMD);
		resultsButton.addActionListener(this);
		resultsPanel.add(resultsButton, BorderLayout.SOUTH);
		resultsPanel.addComponentListener(new SizeLimiter(resultsButton, 1.0,
				RESULTS_BUTTON_HEIGHT));
		viewPanel.add(resultsPanel, BorderLayout.SOUTH);
		viewPanel.addComponentListener(new SizeLimiter(resultsPanel, 1.0,
				1.0 - CHIT_PANEL_HEIGHT));
		add(viewPanel);
		addComponentListener(new SizeLimiter(viewPanel, 1.0, 0.8));
		runner.loadAllTables("tables");
	}

	/**
	 * The tile we refer to.
	 */
	private Tile tile;
	/**
	 * To handle which chit is selected.
	 */
	private final transient SelectionListener chitSelecter = new ChitSelectionListener(
			chitDetail);

	/**
	 * @param newTile
	 *            the tile we should now refer to.
	 */
	public void setTile(final Tile newTile) {
		if (newTile == null) {
			tile = null;
			typeLabel.setText("");
			chitPanel.removeAll();
			resultsField.setText("");
		} else if (!newTile.equals(tile)) {
			tile = newTile;
			typeLabel.setText("<html>(" + tile.getRow() + ", " + tile.getCol()
					+ ")<br />" + terrainText(tile.getType()) + "</html>");
			chitPanel.removeAll();
			chitSelecter.clearSelection();
			for (final TileFixture fix : tile.getContents()) {
				if (fix instanceof Fortress) {
					chitPanel.add(new FortChit((Fortress) fix, chitSelecter)); // NOPMD
				} else if (fix instanceof Unit) {
					chitPanel.add(new UnitChit((Unit) fix, chitSelecter)); // NOPMD
				} else if (fix instanceof AbstractEvent) {
					chitPanel.add(new EventChit((AbstractEvent) fix, chitSelecter)); // NOPMD
				}
			}
			resultsField.setText(newTile.getTileText());
		}
		repaint();
	}

	/**
	 * Descriptions of the types.
	 */
	private static final Map<TileType, String> DESCRIPTIONS = new EnumMap<TileType, String>(
			TileType.class);
	/**
	 * Panel for chits.
	 */
	private final JPanel chitPanel = new JPanel(new FlowLayout());
	static {
		DESCRIPTIONS.put(TileType.BorealForest, "Boreal Forest");
		DESCRIPTIONS.put(TileType.Desert, "Desert");
		DESCRIPTIONS.put(TileType.Jungle, "Jungle");
		DESCRIPTIONS.put(TileType.Mountain, "Mountains");
		DESCRIPTIONS.put(TileType.NotVisible, "Unknown");
		DESCRIPTIONS.put(TileType.Ocean, "Ocean");
		DESCRIPTIONS.put(TileType.Plains, "Plains");
		DESCRIPTIONS.put(TileType.TemperateForest, "Temperate Forest");
		DESCRIPTIONS.put(TileType.Tundra, "Tundra");
	}

	/**
	 * @param type
	 *            a terrain type
	 * @return a String representation of that terrain type
	 */
	private static String terrainText(final TileType type) {
		if (DESCRIPTIONS.containsKey(type)) {
			return DESCRIPTIONS.get(type);
		} // else
		throw new IllegalArgumentException("Unknown terrain type");
	}

	/**
	 * Handle button presses.
	 * 
	 * @param event
	 *            the event to handle
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if (RESULTS_SAVE_CMD.equals(event.getActionCommand()) && tile != null) {
			tile.setTileText(resultsField.getText().trim());
		}
	}

	/**
	 * Run an encounter.
	 */
	public void runEncounter() {
		resultsField.setText(resultsField.getText() + '\n'
				+ runner.recursiveConsultTable("main", tile));
		actionPerformed(new ActionEvent(this, 0, RESULTS_SAVE_CMD));
	}
}
