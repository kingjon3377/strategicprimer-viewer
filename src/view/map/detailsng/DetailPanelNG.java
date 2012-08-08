package view.map.detailsng;

import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.PropertyChangeSource;
import view.map.details.TileDetailPanel;
import view.map.key.KeyPanel;

/**
 * A panel to show the details of a tile, using a tree rather than subpanels
 * with chits for its fixtures.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class DetailPanelNG extends JPanel {
	/**
	 * Constructor.
	 * 
	 * @param version the (initial) map version
	 * @param sources Sources of PropertyChangeEvents we want to listen to.
	 */
	public DetailPanelNG(final int version,
			final PropertyChangeSource... sources) {
		super();
		// Set dimensions?
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		addListener(new TileDetailPanel(), sources);
		add(new JLabel("Contents of the tile on the main map:"));
		add(new FixtureTree("tile", sources));
		add(new JLabel("Contents of the tile on the secondary map:"));
		add(new FixtureTree("secondary-tile", sources));
		addListener(new KeyPanel(version), sources);
	}

	/**
	 * Add a subpanel and make it a property-change listener, if it is one.
	 * 
	 * @param panel the panel to add
	 * 
	 * @param tileEventSources Sources of property-changing events we want
	 *        sub-panels to listen to.
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
