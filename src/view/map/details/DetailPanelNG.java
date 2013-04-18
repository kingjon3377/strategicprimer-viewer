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
	 * The panel containing the list.
	 */
	private static final class ListPanel extends JPanel {
		/**
		 * Constructor.
		 * @param sources PropertyChangeSources to pass to both members of the panel.
		 */
		ListPanel(final PropertyChangeSource... sources) {
			super(new BorderLayout());
			add(new HeaderLabel(sources), BorderLayout.NORTH);
			add(new JScrollPane(new FixtureList(this, sources)), BorderLayout.CENTER);
		}
	}
	/**
	 * The "weight" to give the divider. We want the 'key' to get very little of
	 * any extra space, but to get some.
	 */
	private static final double DIVIDER_LOCATION = 0.9;
	/**
	 * Constructor.
	 *
	 * @param version the (initial) map version
	 * @param sources Sources of PropertyChangeEvents we want to listen to.
	 */
	public DetailPanelNG(final int version,
			final PropertyChangeSource... sources) {
		super(HORIZONTAL_SPLIT, true, new ListPanel(sources), new KeyPanel(version, sources));
		setResizeWeight(DIVIDER_LOCATION);
		setDividerLocation(DIVIDER_LOCATION);
	}
}
