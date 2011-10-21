package view.map.details;

import java.awt.Dimension;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import model.exploration.ExplorationRunner;
import util.PropertyChangeSource;

/**
 * A panel to show the details of a tile.
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
	 * @param version the (initial) map version
	 * @param runner
	 *            an exploration runner
	 * @param tileEventSources
	 *            Sources of property-changing events we want sub-panels to
	 *            listen to.
	 */
	public DetailPanel(final int version, final ExplorationRunner runner,
			final PropertyChangeSource... tileEventSources) {
		super();
		setMaximumSize(new Dimension(Integer.MAX_VALUE, DETAIL_PAN_MAX_HT));
		setMinimumSize(new Dimension(Integer.MAX_VALUE, DETAIL_PAN_MIN_HT));
		setPreferredSize(new Dimension(Integer.MAX_VALUE, DETAIL_PANEL_HT));
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		addListener(new TileDetailPanel(), tileEventSources);
		addListener(new ChitAndDetailPanel(DETAIL_PAN_MAX_HT,
				DETAIL_PAN_MIN_HT, DETAIL_PANEL_HT, "tile"), tileEventSources);
		addListener(new ChitAndDetailPanel(DETAIL_PAN_MAX_HT,
				DETAIL_PAN_MIN_HT, DETAIL_PANEL_HT, "secondary-tile"),
				tileEventSources);
		addListener(new ResultsPanel(DETAIL_PAN_MIN_HT, DETAIL_PANEL_HT,
				DETAIL_PAN_MAX_HT, runner), tileEventSources);
		addListener(new KeyPanel(version), tileEventSources);
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
	private void addListener(final JPanel panel,
			final PropertyChangeSource... tileEventSources) {
		add(panel);
		if (panel instanceof PropertyChangeListener) {
			addPropertyChangeListener((PropertyChangeListener) panel);
			for (final PropertyChangeSource source : tileEventSources) {
				source.addPropertyChangeListener((PropertyChangeListener) panel);
			}
		}
	}

}
