package view.map.main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import model.listeners.SelectionChangeListener;
import model.listeners.SelectionChangeSource;
import model.map.IMapNG;
import model.map.MapDimensions;
import model.map.Point;
import model.map.PointFactory;
import model.map.TerrainFixture;
import model.map.TileFixture;
import model.map.fixtures.Ground;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Mountain;
import model.viewer.FixtureComparator;
import model.viewer.IViewerModel;
import model.viewer.TileViewSize;
import model.viewer.VisibleDimensions;
import util.ArraySet;
import util.IteratorWrapper;
import util.NullCleaner;

/**
 * A mouse listener for the MapComponent, to show the terrain-changing menu as
 * needed.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ComponentMouseListener extends MouseAdapter implements
		SelectionChangeSource {
	/**
	 * Comparator to find which fixture is on top of a tile.
	 */
	private final FixtureComparator fixComp = new FixtureComparator();

	/**
	 * The terrain-changing menu.
	 */
	private final TerrainChangingMenu menu;

	/**
	 * The map model we refer to.
	 */
	private final IViewerModel model;

	/**
	 * @param mapModel the map model we'll refer to
	 */
	public ComponentMouseListener(final IViewerModel mapModel) {
		model = mapModel;
		menu = new TerrainChangingMenu(model.getMapDimensions().version, model);
		model.addSelectionChangeListener(menu);
		model.addVersionChangeListener(menu);
	}

	/**
	 * @param event an event representing the current mouse position
	 * @return a tool-tip message for the tile the mouse is currently over
	 */
	@Nullable
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
			return concat("<html><body>", point.toString(), ": ", model
					.getMap().getBaseTerrain(point).toString(), "<br />",
					getTerrainFixturesAndTop(point), "</body></html>");
		} else {
			return null;
		}
	}

	/**
	 * @param strings strings
	 * @return them concatenated together
	 */
	private static String concat(final String... strings) {
		int len = 0;
		for (final String string : strings) {
			len += string.length();
		}
		final StringBuilder build = new StringBuilder(len);
		for (final String string : strings) {
			build.append(string);
		}
		return NullCleaner.assertNotNull(build.toString());
	}

	/**
	 * @param point a point
	 * @return a HTML-ized String (including final newline entity) representing
	 *         the TerrainFixtures at that point, and the fixture the user can see as
	 *         its top fixture.
	 */
	private String getTerrainFixturesAndTop(final Point point) {
		final IMapNG map = model.getMap();
		final Set<TileFixture> fixes = new ArraySet<>();
		if (map.isMountainous(point)) {
			fixes.add(new Mountain());
		}
		Ground ground = map.getGround(point);
		if (ground != null) {
			fixes.add(ground);
		}
		Forest forest = map.getForest(point);
		if (forest != null) {
			fixes.add(forest);
		}
		final Iterable<TileFixture> iter = new IteratorWrapper<>(
				map.getOtherFixtures(point).iterator(), fixComp);
		final Iterator<TileFixture> iterat = iter.iterator();
		if (iterat.hasNext()) {
			fixes.add(iterat.next());
		}
		for (final TileFixture fix : iter) {
			if (fix instanceof TerrainFixture) {
				fixes.add(fix);
			}
		}
		final StringBuilder sbuild = new StringBuilder();
		for (final TileFixture fix : fixes) {
			sbuild.append(fix.toString());
			sbuild.append("<br />");
		}
		return NullCleaner.assertNotNull(sbuild.toString());
	}

	/**
	 * Handle mouse clicks.
	 *
	 * @param event the event to handle
	 */
	@Override
	public void mouseClicked(@Nullable final MouseEvent event) {
		if (event != null) {
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
	}

	/**
	 * Handle mouse presses.
	 *
	 * @param event the event to handle
	 */
	@Override
	public void mousePressed(@Nullable final MouseEvent event) {
		if (event != null && event.isPopupTrigger()) {
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	/**
	 * Handle mouse releases.
	 *
	 * @param event the event to handle
	 */
	@Override
	public void mouseReleased(@Nullable final MouseEvent event) {
		if (event != null && event.isPopupTrigger()) {
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

	/**
	 * @param list
	 *            Something to listen for changes to the tile type of the
	 *            selected tile
	 */
	@Override
	public void addSelectionChangeListener(final SelectionChangeListener list) {
		menu.addSelectionChangeListener(list);
	}

	/**
	 * @param list something that no longer wants to listen for changes to the
	 *        tile type of the selected tile
	 */
	@Override
	public void removeSelectionChangeListener(final SelectionChangeListener list) {
		menu.removeSelectionChangeListener(list);
	}
}
