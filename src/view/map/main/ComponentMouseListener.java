package view.map.main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Set;

import model.map.MapDimensions;
import model.map.Point;
import model.map.PointFactory;
import model.map.TerrainFixture;
import model.map.Tile;
import model.map.TileFixture;
import model.viewer.FixtureComparator;
import model.viewer.IViewerModel;
import model.viewer.TileViewSize;
import model.viewer.VisibleDimensions;
import util.ArraySet;
import util.IteratorWrapper;

/**
 * A mouse listener for the MapComponent, to show the terrain-changing menu as
 * needed.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ComponentMouseListener extends MouseAdapter {
	/**
	 * The map model we refer to.
	 */
	private final IViewerModel model;
	/**
	 * @param mapModel the map model we'll refer to
	 * @param list a listener to send encounter events to
	 */
	public ComponentMouseListener(final IViewerModel mapModel,
			final PropertyChangeListener list) {
		super();
		model = mapModel;
		menu = new TerrainChangingMenu(model.getMapDimensions().version,
				model.getMap().getTile(model.getSelectedPoint()), list, model);
	}
	/**
	 * @param event an event representing the current mouse position
	 * @return a tool-tip message for the tile the mouse is currently over
	 */
	public String getToolTipText(final MouseEvent event) {
		final java.awt.Point eventPoint = event.getPoint();
		final MapDimensions mapDim = model.getMapDimensions();
		final int tileSize = TileViewSize.scaleZoom(model.getZoomLevel(),
				mapDim.getVersion());
		final VisibleDimensions dimensions = model.getDimensions();
		final Point point = PointFactory.point(eventPoint.y / tileSize
				+ dimensions.getMinimumRow(), eventPoint.x / tileSize
				+ dimensions.getMinimumCol());
		if (point.row < mapDim.getRows() && point.col < mapDim.getColumns()) {
			final Tile tile = model.getTile(point);
			return new StringBuilder("<html><body>")// NOPMD
					.append(point.toString()).append(": ")
					.append(tile.getTerrain()).append("<br />")
					.append(getTerrainFixturesAndTop(tile))
					.append("</body></html>").toString();
		} else {
			return null;
		}
	}
	/**
	 * Comparator to find which fixture is on top of a tile.
	 */
	private final FixtureComparator fixComp = new FixtureComparator();

	/**
	 * @param tile a tile
	 * @return a HTML-ized String (including final newline entity) representing the
	 *         TerrainFixtures on it, and the fixture the user can see as its
	 *         top fixture.
	 */
	private String getTerrainFixturesAndTop(final Tile tile) {
		final Set<TileFixture> fixes = new ArraySet<TileFixture>();
		final Iterable<TileFixture> iter = new IteratorWrapper<TileFixture>(
				tile.iterator(), fixComp);
		final Iterator<TileFixture> iterat = iter.iterator();
		if (iterat.hasNext()) {
			fixes.add(iterat.next());
		}
		for (TileFixture fix : iter) {
			if (fix instanceof TerrainFixture) {
				fixes.add(fix);
			}
		}
		final StringBuilder sbuild = new StringBuilder();
		for (TileFixture fix : fixes) {
			sbuild.append(fix.toString());
			sbuild.append("<br />");
		}
		return sbuild.toString();
	}
	/**
	 * The terrain-changing menu.
	 */
	private final TerrainChangingMenu menu;
	/**
	 * Handle mouse clicks.
	 *
	 * @param event the event to handle
	 */
	@Override
	public void mouseClicked(final MouseEvent event) {
		event.getComponent().requestFocusInWindow();
		final java.awt.Point eventPoint = event.getPoint();
		final VisibleDimensions dimensions = model.getDimensions();
		final MapDimensions mapDim = model.getMapDimensions();
		final int tileSize = TileViewSize.scaleZoom(model.getZoomLevel(),
				mapDim.getVersion());
		final Point point = PointFactory.point(eventPoint.y / tileSize
				+ dimensions.getMinimumRow(), eventPoint.x / tileSize
				+ dimensions.getMinimumCol());
		if (point.row < mapDim.getRows() && point.col < mapDim.getColumns()) {
			model.setSelection(point);
			if (event.isPopupTrigger()) {
				menu.show(event.getComponent(), event.getX(), event.getY());
			}
		}
	}

	/**
	 * Handle mouse presses.
	 *
	 * @param event the event to handle
	 */
	@Override
	public void mousePressed(final MouseEvent event) {
		if (event.isPopupTrigger()) {
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	/**
	 * Handle mouse releases.
	 *
	 * @param event the event to handle
	 */
	@Override
	public void mouseReleased(final MouseEvent event) {
		if (event.isPopupTrigger()) {
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	/**
	 *
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "ComponentMouseListener";
	}
}
