package view.map.details;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import model.listeners.SelectionChangeListener;
import model.listeners.VersionChangeListener;
import model.map.IPlayerCollection;
import model.map.ITile;
import model.map.Point;

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
public class DetailPanelNG extends JSplitPane implements VersionChangeListener,
		SelectionChangeListener {
	/**
	 * The "weight" to give the divider. We want the 'key' to get very little of
	 * any extra space, but to get some.
	 */
	private static final double DIVIDER_LOCATION = 0.9;
	/**
	 * The 'key' panel, showing what each tile color represents.
	 */
	private final KeyPanel keyPanel;
	/**
	 * The list of fixtures on the current tile.
	 */
	private final FixtureList fixList;
	/**
	 * The 'header' label above the list.
	 */
	private final JLabel header = new JLabel(
			"<html><body><p>Contents of the tile at (-1, -1):</p></body></html>");
	/**
	 * Constructor.
	 *
	 * @param version the (initial) map version
	 * @param players the players in the map
	 */
	public DetailPanelNG(final int version, final IPlayerCollection players) {
		super(HORIZONTAL_SPLIT, true);

		fixList = new FixtureList(this, players);
		final BorderedPanel listPanel = new BorderedPanel(new JScrollPane(
				fixList), header, null, null, null);

		keyPanel = new KeyPanel(version);
		setLeftComponent(listPanel);
		setRightComponent(keyPanel);
		setResizeWeight(DIVIDER_LOCATION);
		setDividerLocation(DIVIDER_LOCATION);
	}
	/**
	 * @param old passed to key panel
	 * @param newVersion passed to key panel
	 */
	@Override
	public void changeVersion(final int old, final int newVersion) {
		keyPanel.changeVersion(old, newVersion);
	}
	/**
	 * @param old passed to fixture list
	 * @param newPoint passed to fixture list and shown on the header
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point old, final Point newPoint) {
		fixList.selectedPointChanged(old, newPoint);
		header.setText("<html><body><p>Contents of the tile at "
				+ newPoint.toString() + ":</p></body></html>");
	}
	/**
	 * @param old passed to fixture list
	 * @param newTile passed to fixture list
	 */
	@Override
	public void selectedTileChanged(@Nullable final ITile old, final ITile newTile) {
		fixList.selectedTileChanged(old, newTile);
	}
}
