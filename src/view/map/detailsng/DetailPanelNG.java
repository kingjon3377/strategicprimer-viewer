package view.map.detailsng;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
	 * The maximum width of a list.
	 */
	private static final int LIST_MAX_WIDTH = 300;
	/**
	 * The minimum width of a list.
	 */
	private static final int LIST_MIN_WIDTH = 180;
	/**
	 * The preferred width of a list.
	 */
	private static final int LIST_WIDTH = 240;
	/**
	 * The maximum height of the title labels.
	 */
	private static final int LABEL_MAX_HT = 30;
	/**
	 * The minimum height of the title labels.
	 */
	private static final int LABEL_MIN_HT = 10;
	/**
	 * The preferred height of the title labels.
	 */
	private static final int LABEL_HEIGHT = 20;
	/**
	 * Constructor.
	 *
	 * @param version the (initial) map version
	 * @param sources Sources of PropertyChangeEvents we want to listen to.
	 */
	public DetailPanelNG(final int version,
			final PropertyChangeSource... sources) {
		super();
		setComponentSizes(this, new Dimension(Integer.MAX_VALUE,
				DETAIL_PAN_MIN_HT), new Dimension(Integer.MAX_VALUE,
				DETAIL_PANEL_HT), new Dimension(Integer.MAX_VALUE,
				DETAIL_PAN_MAX_HT));
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		addListener(new TileDetailPanel(), sources);
		final JPanel panelOne = new JPanel(new BorderLayout());
		panelOne.add(createLabel("Contents of the tile on the main map:"),
				BorderLayout.NORTH);
		panelOne.add(createList("tile", sources), BorderLayout.CENTER);
		add(panelOne);
		final JPanel panelTwo = new JPanel(new BorderLayout());
		panelTwo.add(createLabel("On the secondary map:"), BorderLayout.NORTH);
		panelTwo.add(createList("secondary-tile", sources), BorderLayout.CENTER);
		add(panelTwo);
		addListener(new KeyPanel(version), sources);
		// TODO: Drag-and-drop between lists ...
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
	/**
	 * Set a component's sizes.
	 * @param <T> the type of the component
	 * @param component the component
	 * @param min the minimum size
	 * @param pref the preferred size
	 * @param max the maximum size
	 * @return the component
	 */
	private static <T extends JComponent> T setComponentSizes(
			final T component, final Dimension min, final Dimension pref,
			final Dimension max) {
		component.setMinimumSize(min);
		component.setPreferredSize(pref);
		component.setMaximumSize(max);
		return component;
	}
	/**
	 * Create one of the labels. The static constants are effectively parameters.
	 * @param text the text it should have.
	 * @return the constructed label
	 */
	private static JLabel createLabel(final String text) {
		return setComponentSizes(new JLabel(text), new Dimension(
				LIST_MIN_WIDTH, LABEL_MIN_HT), new Dimension(LIST_WIDTH,
				LABEL_HEIGHT), new Dimension(LIST_MAX_WIDTH, LABEL_MAX_HT));
	}
	/**
	 * Create one of the lists/trees. The static constants are effectively parameters.
	 * @param property the property it should be listening to for changes
	 * @param sources PropertyChangeSources it should be listening on
	 * @return a scroll pane containing the list or tree.
	 */
	private static JScrollPane createList(final String property, final PropertyChangeSource... sources) {
		return setComponentSizes(new JScrollPane(
				new FixtureTree(property, sources)), new Dimension(
				LIST_MIN_WIDTH, DETAIL_PAN_MIN_HT), new Dimension(LIST_WIDTH,
				DETAIL_PANEL_HT), new Dimension(LIST_MAX_WIDTH,
				DETAIL_PAN_MAX_HT));
	}
}
