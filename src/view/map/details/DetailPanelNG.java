package view.map.details;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import model.map.Point;
import util.PropertyChangeSource;
import view.map.key.KeyPanel;

/**
 * A panel to show the details of a tile, using a tree rather than subpanels
 * with chits for its fixtures.
 *
 * @author Jonathan Lovelace
 *
 */
public class DetailPanelNG extends JSplitPane {
	/**
	 * A label giving the header for the list of fixtures and saying what the
	 * current tile's coordinates are.
	 */
	private static class HeaderLabel extends JLabel implements PropertyChangeListener {
		/**
		 * Constructor.
		 * @param sources things to listen to for property changes
		 */
		HeaderLabel(final PropertyChangeSource... sources) {
			super(
					"<html><body><p>Contents of the tile at (-1, -1):</p></body></html>");
			for (final PropertyChangeSource source : sources) {
				source.addPropertyChangeListener(this);
			}
		}
		/**
		 * Handle a property change.
		 *
		 * @param evt the event to handle.
		 */
		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			if ("point".equalsIgnoreCase(evt.getPropertyName())
					&& evt.getNewValue() instanceof Point) {
				setText("<html><body><p>Contents of the tile at "
						+ ((Point) evt.getNewValue()).toString()
						+ ":</p></body></html>");
			}
		}

	}
	/**
	 * Constructor.
	 *
	 * @param version the (initial) map version
	 * @param sources Sources of PropertyChangeEvents we want to listen to.
	 */
	public DetailPanelNG(final int version,
			final PropertyChangeSource... sources) {
		super(HORIZONTAL_SPLIT, true);
		final JPanel panelOne = new JPanel(new BorderLayout());
		panelOne.add(new HeaderLabel(sources), BorderLayout.NORTH);
		panelOne.add(new JScrollPane(new FixtureList(this, sources)), BorderLayout.CENTER);
		setLeftComponent(panelOne);
		setRightComponent(new KeyPanel(version, sources));
		setDividerLocation(0.9);
		setResizeWeight(0.9);
	}
}
