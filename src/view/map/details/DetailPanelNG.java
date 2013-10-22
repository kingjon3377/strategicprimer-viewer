package view.map.details;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import model.listeners.SelectionChangeListener;
import model.listeners.SelectionChangeSource;
import model.listeners.VersionChangeSource;
import model.map.PlayerCollection;
import model.map.Point;
import model.map.Tile;

import org.eclipse.jdt.annotation.Nullable;

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
	private static class HeaderLabel extends JLabel implements
			SelectionChangeListener {
		/**
		 * Constructor.
		 *
		 * @param sources things to listen to for property changes
		 */
		HeaderLabel(final SelectionChangeSource... sources) {
			super(
					"<html><body><p>Contents of the tile at (-1, -1):</p></body></html>");
			for (final SelectionChangeSource source : sources) {
				source.addSelectionChangeListener(this);
			}
		}

		/**
		 * @param old the formerly selected location
		 * @param newPoint the newly selected location
		 */
		@Override
		public void selectedPointChanged(@Nullable final Point old,
				final Point newPoint) {
			setText("<html><body><p>Contents of the tile at "
					+ newPoint.toString() + ":</p></body></html>");
		}

		/**
		 * @param old the formerly selected tile
		 * @param newTile the newly selected tile
		 */
		@Override
		public void selectedTileChanged(@Nullable final Tile old,
				final Tile newTile) {
			// Ignored; we only care about the *location*.
		}
	}

	/**
	 * The panel containing the list.
	 */
	private static final class ListPanel extends BorderedPanel {
		/**
		 * Constructor.
		 *
		 * @param players the players in the map
		 * @param sources PropertyChangeSources to pass to both members of the
		 *        panel.
		 */
		ListPanel(final PlayerCollection players,
				final SelectionChangeSource... sources) {
			// We can't use the multi-arg super() because the center component
			// references "this".
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
	 * @param sSources Sources of selection-change notifications we want to
	 *        listen to
	 * @param vSources Sources of PropertyChangeEvents we want to listen to.
	 */
	public DetailPanelNG(final int version, final PlayerCollection players,
			final SelectionChangeSource[] sSources,
			final VersionChangeSource[] vSources) {
		super(HORIZONTAL_SPLIT, true);
		setLeftComponent(new ListPanel(players, sSources));
		final KeyPanel keyPanel = new KeyPanel(version);
		for (final VersionChangeSource source : vSources) {
			source.addVersionChangeListener(keyPanel);
		}
		setRightComponent(keyPanel);
		setResizeWeight(DIVIDER_LOCATION);
		setDividerLocation(DIVIDER_LOCATION);
	}
}
