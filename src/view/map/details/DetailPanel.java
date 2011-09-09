package view.map.details;

import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import model.viewer.TileType;
import view.util.PropertyChangeSource;

/**
 * A panel to show the details of a tile. FIXME: If the map includes the name of
 * a player, it should show that player's name in addition to his or her number.
 * 
 * @author Jonathan Lovelace
 */
public class DetailPanel extends JPanel {
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
	 * Constructor.
	 * 
	 * @param tileEventSources
	 *            Sources of property-changing events we want sub-panels to
	 *            listen to.
	 */
	public DetailPanel(final PropertyChangeSource... tileEventSources) {
		super();
		setMaximumSize(new Dimension(Integer.MAX_VALUE, DETAIL_PAN_MAX_HT));
		setMinimumSize(new Dimension(Integer.MAX_VALUE, DETAIL_PAN_MIN_HT));
		setPreferredSize(new Dimension(Integer.MAX_VALUE, DETAIL_PANEL_HT));
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		addListener(new TileDetailPanel(), tileEventSources);
		addListener(new ChitAndDetailPanel(
				DETAIL_PAN_MAX_HT, DETAIL_PAN_MIN_HT, DETAIL_PANEL_HT, "tile"), tileEventSources);
		addListener(new ChitAndDetailPanel(
				DETAIL_PAN_MAX_HT, DETAIL_PAN_MIN_HT, DETAIL_PANEL_HT, "secondary-tile"), tileEventSources);
		addListener(new ResultsPanel(DETAIL_PAN_MIN_HT,
				DETAIL_PANEL_HT, DETAIL_PAN_MAX_HT), tileEventSources);
		add(new KeyPanel());
	}
	
	/**
	 * Add a subpanel and make it a property-change listener, if it is one.
	 * 
	 * @param panel
	 *            the panel to add
	 * 
	 * @param tileEventSources
	 *            Sources of property-changing events we want sub-panels to
	 *            listen to.
	 */
	private void addListener(final JPanel panel, final PropertyChangeSource... tileEventSources) {
		add(panel);
		if (panel instanceof PropertyChangeListener) {
			addPropertyChangeListener((PropertyChangeListener) panel);
			for (PropertyChangeSource source : tileEventSources) {
				source.addPropertyChangeListener((PropertyChangeListener) panel);
			}
		}
	}
	/**
	 * Descriptions of the types.
	 */
	private static final Map<TileType, String> DESCRIPTIONS = new EnumMap<TileType, String>(
			TileType.class);
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
}
