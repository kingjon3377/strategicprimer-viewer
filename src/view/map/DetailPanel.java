package view.map;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.EnumMap;

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
public class DetailPanel extends JPanel {
	private static final double CHIT_PANEL_HEIGHT = 0.7;
	private static final double CHIT_PANEL_WIDTH = 0.5;
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
		addComponentListener(new SizeLimiter(typePanel, 1.0, 0.25));
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
		resultsPanel.add(new JLabel("<html>Exploration<br>results</html>"), BorderLayout.WEST);
		resultsPanel.add(resultsField, BorderLayout.CENTER);
		viewPanel.add(resultsPanel, BorderLayout.SOUTH);
		viewPanel.addComponentListener(new SizeLimiter(resultsPanel, 1.0, 1.0 - CHIT_PANEL_HEIGHT));
		add(viewPanel, BorderLayout.CENTER);
		addComponentListener(new SizeLimiter(viewPanel, 1.0, 0.75));
	}

	/**
	 * To handle which chit is selected.
	 */
	private final transient SelectionListener chitSelecter = new ChitSelectionListener(chitDetail);
	/**
	 * @param newTile
	 *            the tile we should now refer to.
	 */
	public void setTile(final Tile newTile) {
		if (newTile == null) {
			typeLabel.setText("");
			chitPanel.removeAll();
			eventLabel.setText("");
			resultsField.setText("");
		} else {
			typeLabel.setText(terrainText(newTile.getType()));
			chitPanel.removeAll();
			chitSelecter.clearSelection();
			for (Fortress fort : newTile.getForts()) {
				chitPanel.add(new FortChit(fort, chitSelecter)); // NOPMD
			}
			for (Unit unit : newTile.getUnits()) {
				chitPanel.add(new UnitChit(unit, chitSelecter)); // NOPMD
			}
			eventLabel.setText(Integer.toString(newTile.getEvent()));
			resultsField.setText(""); // FIXME: Implement
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
}
