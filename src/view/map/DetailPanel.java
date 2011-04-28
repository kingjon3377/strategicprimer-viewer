package view.map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
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
	private final JLabel typeLabel = new ShortLabel();
	private final JLabel fortsLabel = new JLabel();
	private final JLabel unitsLabel = new JLabel();
	private final JLabel eventLabel = new ShortLabel();
	private final JTextField resultsField = new JTextField();

	/**
	 * Constructor.
	 */
	public DetailPanel() {
		super(new BorderLayout());
		JPanel typePanel = new JPanel(new BorderLayout());
		typePanel.add(new JLabel("Tile type:"), BorderLayout.WEST);
		typePanel.add(typeLabel, BorderLayout.CENTER);
		add(typePanel, BorderLayout.NORTH);
		if (IsAdmin.IS_ADMIN) {
			JPanel eventPanel = new JPanel(new BorderLayout());
			eventPanel.add(new JLabel("Legacy Event:"), BorderLayout.WEST);
			eventPanel.add(eventLabel, BorderLayout.CENTER);
			add(eventPanel, BorderLayout.SOUTH);
		}
		final JPanel viewPanel = new JPanel(new BorderLayout());
		viewPanel.add(new JPanel(), BorderLayout.WEST);
		final JPanel resultsPanel = new JPanel(new BorderLayout());
		resultsPanel.add(new ShortLabel("Exploration\nresults"), BorderLayout.NORTH);
		resultsPanel.add(resultsField, BorderLayout.CENTER);
		viewPanel.add(resultsPanel, BorderLayout.EAST);
		add(viewPanel, BorderLayout.CENTER);
	}

	/**
	 * The tile we refer to
	 */
	private Tile tile = null;

	/**
	 * @param newTile
	 *            the tile we should now refer to.
	 */
	public void setTile(final Tile newTile) {
		tile = newTile;
		if (tile != null) {
			typeLabel.setText(terrainText(tile.getType()));
			fortsLabel.setText(anyForts(tile.getForts()));
			unitsLabel.setText(anyUnits(tile.getUnits()));
			eventLabel.setText(Integer.toString(tile.getEvent()));
			resultsField.setText(""); // FIXME: Implement
		}
	}

	/**
	 * Descriptions of the types.
	 */
	private static final EnumMap<TileType, String> DESCRIPTIONS = new EnumMap<TileType, String>(
			TileType.class);
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
	public static class ShortLabel extends JLabel {
		@Override
		public Dimension getMaximumSize() {
			return new Dimension(super.getMaximumSize().width,
					getMinimumSize().height);
		}
		@Override
		public Dimension getPreferredSize() {
			return getMaximumSize();
		}
		public ShortLabel(String string) {
			super(string);
		}
		public ShortLabel() {
			super();
		}
		public String getText() {
			return "<html><pre>" + super.getText() + "</pre></html>";
		}
	}
}
