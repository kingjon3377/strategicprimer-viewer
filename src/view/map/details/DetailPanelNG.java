package view.map.details;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.eclipse.jdt.annotation.Nullable;

import model.map.PlayerCollection;
import model.map.Point;
import util.PropertyChangeSource;
import view.map.key.KeyPanel;
import view.util.BorderedPanel;

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
		public void propertyChange(@Nullable final PropertyChangeEvent evt) {
			if (evt != null && "point".equalsIgnoreCase(evt.getPropertyName())
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
	private static final class ListPanel extends BorderedPanel {
		/**
		 * Constructor.
		 * @param players the players in the map
		 * @param sources PropertyChangeSources to pass to both members of the panel.
		 */
		ListPanel(final PlayerCollection players, final PropertyChangeSource... sources) {
			// We can't use the multi-arg super() because the center component references "this".
			setNorth(new HeaderLabel(sources));
			setCenter(new JScrollPane(new FixtureList(this, players, sources)));
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
		 * @param players the players in the map
	 * @param sources Sources of PropertyChangeEvents we want to listen to.
	 */
	public DetailPanelNG(final int version, final PlayerCollection players,
			final PropertyChangeSource... sources) {
		super(HORIZONTAL_SPLIT, true, new ListPanel(players, sources), new KeyPanel(version, sources));
		setResizeWeight(DIVIDER_LOCATION);
		setDividerLocation(DIVIDER_LOCATION);
	}
}
