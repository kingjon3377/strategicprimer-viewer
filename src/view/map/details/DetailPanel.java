package view.map.details;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import model.exploration.ExplorationRunner;
import model.viewer.Tile;
import model.viewer.TileFixture;
import model.viewer.TileType;

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
	 * Maximum height of this panel, in pixels.
	 */
	public static final int DETAIL_PAN_MAX_HT = 175;
	/**
	 * Preferred width of this panel, in pixels.
	 */
	public static final int DETAIL_PANEL_HT = 125;
	/**
	 * Minimum width of this panel, in pixels.
	 */
	public static final int DETAIL_PAN_MIN_HT = 50;
	/**
	 * Maximum height of the chit panel and chit-detail label, in pixels.
	 */
	private static final int CHIT_PAN_MAX_WD = 200;
	/**
	 * Minimum height of the chit panel and chit-detail label, in pixels.
	 */
	private static final int CHIT_PAN_MIN_WD = 100;
	/**
	 * Preferred height of the chit panel and chit-detail label, in pixels.
	 */
	private static final int CHIT_PANEL_WIDTH = 150;
	/**
	 * Minimum height of the results field and its label.
	 */
	private static final int RESULTS_MIN_WD = 200;
	/**
	 * Preferred height of the results field and its label.
	 */
	private static final int RESULTS_PREF_WD = 250;
	/**
	 * Maximum height of the results field and its label.
	 */
	private static final int RESULTS_MAX_WD = 300;
	/**
	 * Minimum height of the results label.
	 */
	private static final int LABEL_MIN_HT = 10; // NOPMD
	/**
	 * Preferred height of the results label.
	 */
	private static final int LABEL_PREF_HT = 15;
	/**
	 * Maximum height for the results label.
	 */
	private static final int LABEL_MAX_HT = 20;
	/**
	 * Minimum height of the results button.
	 */
	private static final int BUTTON_MIN_HT = 15;
	/**
	 * Preferred height of the results button.
	 */
	private static final int BUTTON_PREF_HT = 20;
	/**
	 * Maximum height of the results button.
	 */
	private static final int BUTTON_MAX_HT = 25;
	/**
	 * Panel to show the tile's coordinates and terrain type.
	 */
	private final TileDetailPanel typePanel = new TileDetailPanel();
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
		setMaximumSize(new Dimension(Integer.MAX_VALUE, DETAIL_PAN_MAX_HT));
		setMinimumSize(new Dimension(Integer.MAX_VALUE, DETAIL_PAN_MIN_HT));
		setPreferredSize(new Dimension(Integer.MAX_VALUE, DETAIL_PANEL_HT));
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		typePanel.updateText(new Tile(-1, -1, TileType.NotVisible));
		add(typePanel);
		final JScrollPane chitPane = new JScrollPane(chitPanel);
		chitPane.setMaximumSize(new Dimension(CHIT_PAN_MAX_WD, getMaximumSize().height));
		chitPane.setMinimumSize(new Dimension(CHIT_PAN_MIN_WD, getMinimumSize().height));
		chitPane.setPreferredSize(new Dimension(CHIT_PANEL_WIDTH, getPreferredSize().height));
		chitPane.setBorder(null);
		add(chitPane);
		
		chitDetail.setMaximumSize(new Dimension(
				chitPane.getMaximumSize().width, getMaximumSize().height));
		chitDetail.setMinimumSize(new Dimension(
				chitPane.getMinimumSize().width, getMinimumSize().height));
		chitDetail.setPreferredSize(new Dimension(
				chitPane.getPreferredSize().width, getPreferredSize().height));
		add(chitDetail);

		final JPanel resultsPanel = new JPanel(new BorderLayout());
		final JLabel resultsLabel = new JLabel(
				"Exploration results");
		resultsLabel.setAlignmentY(SwingConstants.CENTER);
		resultsLabel.setMinimumSize(new Dimension(RESULTS_MIN_WD, LABEL_MIN_HT));
		resultsLabel.setPreferredSize(new Dimension(RESULTS_PREF_WD, LABEL_PREF_HT));
		resultsLabel.setMaximumSize(new Dimension(RESULTS_MAX_WD, LABEL_MAX_HT));
		resultsPanel.add(resultsLabel, BorderLayout.NORTH);

		resultsField.setLineWrap(true);
		resultsField.setEditable(true);
		resultsField.setWrapStyleWord(true);
		final JScrollPane resultsWrapper = new JScrollPane(resultsField);
		resultsWrapper.setMinimumSize(new Dimension(RESULTS_MIN_WD,
				getMinimumSize().height - LABEL_MIN_HT - BUTTON_MIN_HT));
		resultsWrapper.setPreferredSize(new Dimension(RESULTS_PREF_WD,
				getPreferredSize().height - LABEL_PREF_HT - BUTTON_PREF_HT));
		resultsWrapper.setMaximumSize(new Dimension(RESULTS_MAX_WD,
				getMaximumSize().height - LABEL_MAX_HT - BUTTON_MAX_HT));
		resultsPanel.add(resultsWrapper, BorderLayout.CENTER);

		final JButton resultsButton = new JButton(RESULTS_SAVE_CMD);
		resultsButton.addActionListener(this);
		resultsButton.setMinimumSize(new Dimension(RESULTS_MIN_WD, BUTTON_MIN_HT));
		resultsButton.setPreferredSize(new Dimension(RESULTS_PREF_WD, BUTTON_PREF_HT));
		resultsButton.setMaximumSize(new Dimension(RESULTS_MAX_WD, BUTTON_MAX_HT));
		resultsPanel.add(resultsButton, BorderLayout.SOUTH);
		add(resultsPanel);
		runner.loadAllTables("tables");
		add(new KeyPanel());
	}

	/**
	 * The tile we refer to.
	 */
	private Tile tile;
	
	/**
	 * @param newTile
	 *            the tile we should now refer to.
	 */
	public void setTile(final Tile newTile) {
		if (newTile.equals(tile)) {
			return;
		} else {
			tile = newTile;
		}
		typePanel.updateText(tile);
			chitPanel.clear();
			for (final TileFixture fix : tile.getContents()) {
				chitPanel.add(fix);
			}
			resultsField.setText(tile.getTileText());
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
	private final ChitPanel chitPanel = new ChitPanel(new ChitSelectionListener(
			chitDetail));
	static {
		DESCRIPTIONS.put(TileType.BorealForest, "<html><p>Boreal Forest</p></html>");
		DESCRIPTIONS.put(TileType.Desert, "<html><p>Desert</p></html>");
		DESCRIPTIONS.put(TileType.Jungle, "<html><p>Jungle</p></html>");
		DESCRIPTIONS.put(TileType.Mountain, "<html><p>Mountains</p></html>");
		DESCRIPTIONS.put(TileType.NotVisible, "<html><p>Unknown</p></html>");
		DESCRIPTIONS.put(TileType.Ocean, "<html><p>Ocean</p></html>");
		DESCRIPTIONS.put(TileType.Plains, "<html><p>Plains</p></html>");
		DESCRIPTIONS.put(TileType.TemperateForest, "<html><p>Temperate Forest</p></html>");
		DESCRIPTIONS.put(TileType.Tundra, "<html><p>Tundra</p></html>");
	}

	/**
	 * @param type
	 *            a terrain type
	 * @return a String representation of that terrain type
	 */
	static String terrainText(final TileType type) { // NOPMD
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
		if (RESULTS_SAVE_CMD.equals(event.getActionCommand())) {
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
