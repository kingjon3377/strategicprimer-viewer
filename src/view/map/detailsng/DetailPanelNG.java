package view.map.detailsng;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
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
		setMaximumSize(new Dimension(Integer.MAX_VALUE, DETAIL_PAN_MAX_HT));
		setMinimumSize(new Dimension(Integer.MAX_VALUE, DETAIL_PAN_MIN_HT));
		setPreferredSize(new Dimension(Integer.MAX_VALUE, DETAIL_PANEL_HT));
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		addListener(new TileDetailPanel(), sources);
		final JPanel panelOne = new JPanel(new BorderLayout());
		final JLabel labelOne = new JLabel("Contents of the tile on the main map:");
		labelOne.setMaximumSize(new Dimension(LIST_MAX_WIDTH, LABEL_MAX_HT));
		labelOne.setMinimumSize(new Dimension(LIST_MIN_WIDTH, LABEL_MIN_HT));
		labelOne.setPreferredSize(new Dimension(LIST_WIDTH, LABEL_HEIGHT));
		panelOne.add(labelOne, BorderLayout.NORTH);
		final JScrollPane listOne = new JScrollPane(new FixtureList("tile", sources));
		listOne.setMaximumSize(new Dimension(LIST_MAX_WIDTH, DETAIL_PAN_MAX_HT));
		listOne.setMinimumSize(new Dimension(LIST_MIN_WIDTH, DETAIL_PAN_MIN_HT));
		listOne.setPreferredSize(new Dimension(LIST_WIDTH, DETAIL_PANEL_HT));
		panelOne.add(listOne, BorderLayout.CENTER);
		add(panelOne);
		final JPanel panelTwo = new JPanel(new BorderLayout());
		final JLabel labelTwo = new JLabel("On the secondary map:");
		labelTwo.setMaximumSize(new Dimension(LIST_MAX_WIDTH, LABEL_MAX_HT));
		labelTwo.setMinimumSize(new Dimension(LIST_MIN_WIDTH, LABEL_MIN_HT));
		labelTwo.setPreferredSize(new Dimension(LIST_WIDTH, LABEL_HEIGHT));
		panelTwo.add(labelTwo, BorderLayout.NORTH);
		final JScrollPane listTwo = new JScrollPane(new FixtureList("secondary-tile", sources));
		listTwo.setMaximumSize(new Dimension(LIST_MAX_WIDTH, DETAIL_PAN_MAX_HT));
		listTwo.setMinimumSize(new Dimension(LIST_MIN_WIDTH, DETAIL_PAN_MIN_HT));
		listTwo.setPreferredSize(new Dimension(LIST_WIDTH, DETAIL_PANEL_HT));
		panelTwo.add(listTwo, BorderLayout.CENTER);
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
}
