package view.map.main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.viewer.Fortress;
import model.viewer.Tile;
import model.viewer.TileType;
import model.viewer.Unit;
import view.util.IsAdmin;
import view.util.SizeLimiter;

/**
 * A panel to show the details of a tile. FIXME: If the map includes the name of
 * a player, it should show that player's name in addition to his or her number.
 * 
 * @author Jonathan Lovelace
 */
public class DetailPanel extends JPanel implements ActionListener {
	private static final String RESULTS_SAVE_CMD = "<html><p>Save changed results</p></html>";
	private static final double CHIT_PANEL_HEIGHT = 0.6;
	private static final double CHIT_PANEL_WIDTH = 0.5;
	private static final double RESULTS_BUTTON_HEIGHT = 0.2; // NOPMD
	private static final double RESULTS_FIELD_WIDTH = 0.5; // NOPMD
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 3391860564156014799L;
	private final JLabel typeLabel = new JLabel();
	private final JLabel eventLabel = new JLabel();
	private final JLabel chitDetail = new JLabel();
	private final JTextField resultsField = new JTextField();
	/**
	 * Constructor.
	 */
	public DetailPanel() {
		super(new BorderLayout());
		final JPanel typePanel = new JPanel(new BorderLayout());
		typePanel.add(new JLabel("Tile type:"), BorderLayout.WEST);
		typePanel.add(typeLabel, BorderLayout.CENTER);
		add(typePanel, BorderLayout.NORTH);
		addComponentListener(new SizeLimiter(typePanel, 1.0, 0.2));
		if (IsAdmin.IS_ADMIN) {
			final JPanel eventPanel = new JPanel(new BorderLayout());
			eventPanel.add(new JLabel("Legacy Event:"), BorderLayout.WEST);
			eventPanel.add(eventLabel, BorderLayout.CENTER);
			add(eventPanel, BorderLayout.SOUTH);
			addComponentListener(new SizeLimiter(eventPanel, 1.0, 0.1));
		}
		final JPanel viewPanel = new JPanel(new BorderLayout());
		final JPanel chitSuperPanel = new JPanel(new BorderLayout());
		chitSuperPanel.add(chitPanel, BorderLayout.WEST);
		chitSuperPanel.addComponentListener(new SizeLimiter(chitPanel, CHIT_PANEL_WIDTH, 1.0));
		chitSuperPanel.add(chitDetail, BorderLayout.EAST);
		chitSuperPanel.addComponentListener(new SizeLimiter(chitDetail, 1.0 - CHIT_PANEL_WIDTH, 1.0));
		viewPanel.add(chitSuperPanel, BorderLayout.NORTH);
		viewPanel.addComponentListener(new SizeLimiter(chitSuperPanel, 1.0, CHIT_PANEL_HEIGHT));
		final JPanel resultsPanel = new JPanel(new BorderLayout());
		final JLabel resultsLabel = new JLabel("<html>Exploration<br>results</html>");
		resultsPanel.add(resultsLabel, BorderLayout.WEST);
		resultsPanel.addComponentListener(new SizeLimiter(resultsLabel, 1.0 - RESULTS_FIELD_WIDTH, 1.0 - RESULTS_BUTTON_HEIGHT));
		resultsPanel.add(resultsField, BorderLayout.CENTER);
		resultsPanel.addComponentListener(new SizeLimiter(resultsField, RESULTS_FIELD_WIDTH, 1.0 - RESULTS_BUTTON_HEIGHT));
		final JButton resultsButton = new JButton(RESULTS_SAVE_CMD);
		resultsButton.addActionListener(this);
		resultsPanel.add(resultsButton, BorderLayout.SOUTH);
		resultsPanel.addComponentListener(new SizeLimiter(resultsButton, 1.0, RESULTS_BUTTON_HEIGHT));
		viewPanel.add(resultsPanel, BorderLayout.SOUTH);
		viewPanel.addComponentListener(new SizeLimiter(resultsPanel, 1.0, 1.0 - CHIT_PANEL_HEIGHT));
		add(viewPanel, BorderLayout.CENTER);
		addComponentListener(new SizeLimiter(viewPanel, 1.0, 0.8));
	}

	/**
	 * The tile we refer to
	 */
	private Tile tile = null;
	/**
	 * To handle which chit is selected.
	 */
	private final transient SelectionListener chitSelecter = new ChitSelectionListener(chitDetail);
	/**
	 * @param newTile
	 *            the tile we should now refer to.
	 */
	public void setTile(final Tile newTile) {
		tile = newTile;
		if (tile == null) {
			typeLabel.setText("");
			chitPanel.removeAll();
			eventLabel.setText("");
			resultsField.setText("");
		} else {
			typeLabel.setText(terrainText(tile.getType()));
			chitPanel.removeAll();
			chitSelecter.clearSelection();
			for (Fortress fort : tile.getForts()) {
				chitPanel.add(new FortChit(fort, chitSelecter)); // NOPMD
			}
			for (Unit unit : tile.getUnits()) {
				chitPanel.add(new UnitChit(unit, chitSelecter)); // NOPMD
			}
			eventLabel.setText(Integer.toString(tile.getEvent()));
			resultsField.setText(newTile.getTileText());
		}
		repaint();
	}

	/**
	 * Descriptions of the types.
	 */
	private static final EnumMap<TileType, String> DESCRIPTIONS = new EnumMap<TileType, String>(
			TileType.class);
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
	 * Handle button presses
	 * @param event the event to handle
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if (RESULTS_SAVE_CMD.equals(event.getActionCommand()) && tile != null) {
			tile.setTileText(resultsField.getText().trim());
		}
	}
}
