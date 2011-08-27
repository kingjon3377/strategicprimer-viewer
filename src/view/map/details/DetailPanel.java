package view.map.details;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.ScrollPane;
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
import view.map.main.SelectionListener;
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
	 * Preferred width of this panel, in pixels.
	 */
	public static final int DETAIL_PANEL_WIDTH = 250;
	/**
	 * Minimum width of this panel, in pixels.
	 */
	public static final int DETAIL_PAN_MIN_WID = 150;
	/**
	 * Maximum height of the chit panel and chit-detail label, in pixels.
	 */
	private static final int CHIT_PAN_MAX_HT = 200;
	/**
	 * Minimum height of the chit panel and chit-detail label, in pixels.
	 */
	private static final int CHIT_PAN_MIN_HT = 100;
	/**
	 * Preferred height of the chit panel and chit-detail label, in pixels.
	 */
	private static final int CHIT_PANEL_HEIGHT = 150;
	/**
	 * Minimum height of the results field and its label.
	 */
	private static final int RESULTS_MIN_HT = 150;
	/**
	 * Preferred height of the results field and its label.
	 */
	private static final int RESULTS_HEIGHT = 250;
	/**
	 * Maximum height of the results field and its label.
	 */
	private static final int RESULTS_MAX_HT = 350;
	/**
	 * Height of the results button as a ratio to its parent's height.
	 */
	private static final int RESULTS_BUTTON_HEIGHT = 20; // NOPMD
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
		setMaximumSize(new Dimension(DETAIL_PANEL_WIDTH, Integer.MAX_VALUE));
		setMinimumSize(new Dimension(DETAIL_PAN_MIN_WID, Integer.MAX_VALUE));
		setPreferredSize(new Dimension(DETAIL_PANEL_WIDTH, Integer.MAX_VALUE));
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		final JPanel typePanel = new JPanel(new BorderLayout());
		typePanel.add(new JLabel("<html>Coordinates:<br />Tile type:</html>"), BorderLayout.WEST);
		typePanel.add(typeLabel, BorderLayout.CENTER);
		add(typePanel);
		final JPanel viewPanel = new JPanel(new BorderLayout());
		final JPanel chitSuperPanel = new JPanel(new BorderLayout());
		final JScrollPane chitPane = new JScrollPane(chitPanel);
		chitPane.setMaximumSize(new Dimension(getMaximumSize().width / 2, CHIT_PAN_MAX_HT));
		chitPane.setMinimumSize(new Dimension(getMinimumSize().width / 2, CHIT_PAN_MIN_HT));
		chitPane.setPreferredSize(new Dimension(getPreferredSize().width / 2, CHIT_PANEL_HEIGHT));
		chitPane.setBorder(null);
		chitSuperPanel.add(chitPane, BorderLayout.WEST);
		
		chitDetail.setMaximumSize(new Dimension(getMaximumSize().width
				- chitPane.getMaximumSize().width,
				chitPane.getMaximumSize().height));
		chitDetail.setMinimumSize(new Dimension(getMinimumSize().width
				- chitPane.getMinimumSize().width,
				chitPane.getMinimumSize().height));
		chitDetail.setPreferredSize(new Dimension(getPreferredSize().width
				- chitPane.getPreferredSize().width, chitPane
				.getPreferredSize().height));
		chitSuperPanel.add(chitDetail, BorderLayout.EAST);

		viewPanel.add(chitSuperPanel, BorderLayout.NORTH);
		
		final JPanel resultsPanel = new JPanel(new BorderLayout());
		final JLabel resultsLabel = new JLabel(
				"<html>Exploration<br>results</html>");
		resultsLabel.setVerticalTextPosition(SwingConstants.CENTER);
		resultsLabel.setMinimumSize(new Dimension(getMinimumSize().width / 2, RESULTS_MIN_HT));
		resultsLabel.setPreferredSize(new Dimension(getPreferredSize().width / 2, RESULTS_HEIGHT));
		resultsLabel.setMaximumSize(new Dimension(getMaximumSize().width / 2, RESULTS_MAX_HT));
		resultsPanel.add(resultsLabel, BorderLayout.WEST);

		resultsField.setLineWrap(true);
		resultsField.setEditable(true);
		resultsField.setWrapStyleWord(true);
		final ScrollPane resultsWrapper = new ScrollPane();
		resultsWrapper.add(resultsField);
		resultsWrapper.setMinimumSize(new Dimension(getMinimumSize().width
				- resultsLabel.getMinimumSize().width, resultsLabel
				.getMinimumSize().height));
		resultsWrapper.setPreferredSize(new Dimension(getPreferredSize().width
				- resultsLabel.getPreferredSize().width, resultsLabel
				.getPreferredSize().height));
		resultsWrapper.setMaximumSize(new Dimension(getMaximumSize().width
				- resultsLabel.getMaximumSize().width, resultsLabel
				.getMaximumSize().height));
		resultsPanel.add(resultsWrapper, BorderLayout.CENTER);

		final JButton resultsButton = new JButton(RESULTS_SAVE_CMD);
		resultsButton.addActionListener(this);
		resultsButton.setMinimumSize(new Dimension(getMinimumSize().width, RESULTS_BUTTON_HEIGHT));
		resultsButton.setPreferredSize(new Dimension(getPreferredSize().width, RESULTS_BUTTON_HEIGHT));
		resultsButton.setMaximumSize(new Dimension(getMaximumSize().width, RESULTS_BUTTON_HEIGHT));
		resultsPanel.add(resultsButton, BorderLayout.SOUTH);
		viewPanel.add(resultsPanel, BorderLayout.SOUTH);
		add(viewPanel);
		addComponentListener(new SizeLimiter(viewPanel, 1.0, 0.8));
		runner.loadAllTables("tables");
		add(new KeyPanel());
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
		if (newTile.equals(tile)) {
			return;
		} else {
			tile = newTile;
		}
			typeLabel.setText("<html>(" + tile.getRow() + ", " + tile.getCol()
					+ ")<br />" + terrainText(tile.getType()) + "</html>");
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
	private final ChitPanel chitPanel = new ChitPanel(chitSelecter);
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
