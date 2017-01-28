package view.map.main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
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
import model.viewer.IViewerModel;
import model.viewer.TileViewSize;
import model.viewer.VisibleDimensions;
import model.viewer.ZOrderFilter;
import org.eclipse.jdt.annotation.Nullable;
import util.ArraySet;

/**
 * A mouse listener for the MapComponent, to show the terrain-changing menu as needed.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ComponentMouseListener extends MouseAdapter implements
		SelectionChangeSource {
	/**
	 * The terrain-changing menu.
	 */
	private final TerrainChangingMenu menu;

	/**
	 * The map model we refer to.
	 */
	private final IViewerModel model;

	/**
	 * The filter to tell us what fixtures to ignore.
	 */
	private final ZOrderFilter zof;

	/**
	 * Constructor.
	 * @param mapModel the map model we'll refer to
	 * @param filter the filter to tell us what fixtures to ignore
	 */
	public ComponentMouseListener(final IViewerModel mapModel,
								  final ZOrderFilter filter) {
		model = mapModel;
		zof = filter;
		menu = new TerrainChangingMenu(model.getMapDimensions().version, model);
		model.addSelectionChangeListener(menu);
		model.addVersionChangeListener(menu);
	}

	/**
	 * Create the tool-tip message for the location the mouse cursor is near.
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
		final Point point = PointFactory.point((eventPoint.y / tileSize)
													   + dimensions.getMinimumRow(),
				(eventPoint.x / tileSize)
						+ dimensions.getMinimumCol());
		if ((point.getRow() < mapDim.getRows()) &&
					(point.getCol() < mapDim.getColumns())) {
			final String mtnString;
			if (model.getMap().isMountainous(point)) {
				mtnString = ", mountainous";
			} else {
				mtnString = "";
			}
			return String.format("<html><body>%s: %s%s<br />%s</br></body></html>",
					point.toString(), model.getMap().getBaseTerrain(point).toString(),
					mtnString, getTerrainFixturesAndTop(point));
		} else {
			return null;
		}
	}

	/**
	 * Create a description of the terrain and "top" fixture on the tile.
	 * TODO: sort by dynamic Z-value
	 * @param point a point
	 * @return a HTML-wrapped String (including final newline entity) representing the
	 * TerrainFixtures at that point, and the fixture the user can see as its top
	 * fixture.
	 */
	private String getTerrainFixturesAndTop(final Point point) {
		final IMapNG map = model.getMap();
		final Collection<TileFixture> fixes = new ArraySet<>();
		final Ground ground = map.getGround(point);
		if (ground != null && zof.shouldDisplay(ground)) {
			fixes.add(ground);
		}
		final Forest forest = map.getForest(point);
		if (forest != null && zof.shouldDisplay(forest)) {
			fixes.add(forest);
		}
		final Optional<TileFixture> first =
				map.streamOtherFixtures(point).filter(zof::shouldDisplay).findAny();
		first.ifPresent(fixes::add);
		map.streamOtherFixtures(point).filter(TerrainFixture.class::isInstance)
				.filter(zof::shouldDisplay).forEach(fixes::add);
		return fixes.stream().map(TileFixture::toString)
					   .collect(Collectors.joining("<br />"));
	}

	/**
	 * Handle mouse clicks.
	 *
	 * @param event the event to handle
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void mouseClicked(@Nullable final MouseEvent event) {
		if (event != null) {
			event.getComponent().requestFocusInWindow();
			final java.awt.Point eventPoint = event.getPoint();
			final VisibleDimensions dimensions = model.getDimensions();
			final MapDimensions mapDim = model.getMapDimensions();
			final int tileSize = TileViewSize.scaleZoom(model.getZoomLevel(),
					mapDim.getVersion());
			final Point point = PointFactory.point((eventPoint.y / tileSize)
														   + dimensions.getMinimumRow(),
					(eventPoint.x / tileSize)
							+ dimensions.getMinimumCol());
			if ((point.getRow() < mapDim.getRows()) &&
						(point.getCol() < mapDim.getColumns())) {
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
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void mousePressed(@Nullable final MouseEvent event) {
		if ((event != null) && event.isPopupTrigger()) {
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	/**
	 * Handle mouse releases.
	 *
	 * @param event the event to handle
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void mouseReleased(@Nullable final MouseEvent event) {
		if ((event != null) && event.isPopupTrigger()) {
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object.
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ComponentMouseListener";
	}

	/**
	 * Add a listener to notify when a tile's type changes.
	 * @param list Something to listen for changes to the tile type of the selected tile
	 */
	@Override
	public void addSelectionChangeListener(final SelectionChangeListener list) {
		menu.addSelectionChangeListener(list);
	}

	/**
	 * Remove a listener from the list that we notify.
	 * @param list something that no longer wants to listen for changes to the tile type
	 *             of the selected tile
	 */
	@Override
	public void removeSelectionChangeListener(final SelectionChangeListener list) {
		menu.removeSelectionChangeListener(list);
	}
}
