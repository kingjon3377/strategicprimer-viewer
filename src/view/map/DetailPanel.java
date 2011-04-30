package view.map;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.EnumMap;
import java.util.List;

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
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 3391860564156014799L;
	private final JLabel typeLabel = new JLabel();
	private final JLabel eventLabel = new JLabel();
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
		viewPanel.add(chitPanel, BorderLayout.WEST);
		viewPanel.addComponentListener(new SizeLimiter(chitPanel, 0.5, 0.7));
		final JPanel resultsPanel = new JPanel(new BorderLayout());
		resultsPanel.add(new JLabel("<html>Exploration<br>results</html>"), BorderLayout.WEST);
		resultsPanel.add(resultsField, BorderLayout.CENTER);
		viewPanel.add(resultsPanel, BorderLayout.SOUTH);
		viewPanel.addComponentListener(new SizeLimiter(resultsPanel, 1.0, 0.3));
		add(viewPanel, BorderLayout.CENTER);
		addComponentListener(new SizeLimiter(viewPanel, 1.0, 0.75));
	}

	/**
	 * The tile we refer to
	 */
	private Tile tile = null;
	/**
	 * To handle which chit is selected.
	 */
	private final SelectionListener chitSelecter = new SelectionListener();
	/**
	 * @param newTile
	 *            the tile we should now refer to.
	 */
	public void setTile(final Tile newTile) {
		tile = newTile;
		if (tile != null) {
			typeLabel.setText(terrainText(tile.getType()));
			chitPanel.removeAll();
			for (Fortress fort : tile.getForts()) {
				chitPanel.add(new FortChit(fort, chitSelecter)); // FIXME: Make it an actual listener
			}
			for (Unit unit : tile.getUnits()) {
				chitPanel.add(new UnitChit(unit, chitSelecter)); // FIXME: Make it an actual listener
			}
			eventLabel.setText(Integer.toString(tile.getEvent()));
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

	/**
	 * @param forts
	 *            a list of forts
	 * @return a string representation of the list, to show the user
	 */
	private static String anyForts(final List<Fortress> forts) {
		final StringBuilder sbuild = new StringBuilder();
		for (Fortress fort : forts) {
			sbuild.append("Fortress ");
			if (fort.getName() != null) {
				sbuild.append(fort.getName());
				sbuild.append(", ");
			}
			sbuild.append("owned by player ");
			sbuild.append(fort.getOwner());
			sbuild.append('\n');
		}
		return sbuild.toString();
	}

	/**
	 * @param units
	 *            a list of units
	 * @return a string representation of the list, to show the user
	 */
	private static String anyUnits(final List<Unit> units) {
		final StringBuilder sbuild = new StringBuilder();
		for (Unit unit : units) {
			sbuild.append("Unit ");
			if (unit.getName() != null) {
				sbuild.append(unit.getName());
				sbuild.append(", ");
			}
			if (unit.getType() != null) {
				sbuild.append("of type ");
				sbuild.append(unit.getType());
				sbuild.append(", ");
			}
			sbuild.append("owned by player ");
			sbuild.append(unit.getOwner());
			sbuild.append('\n');
		}
		return sbuild.toString();
	}
}
