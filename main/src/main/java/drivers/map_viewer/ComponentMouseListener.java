package drivers.map_viewer;

import static drivers.map_viewer.TileViewSize.scaleZoom;
import java.util.List;
import java.util.stream.Collectors;
import common.map.TileType;
import java.util.Optional;
import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import common.map.MapDimensions;
import common.map.Point;
import common.map.TileFixture;
import common.map.IMapNG;
import common.map.fixtures.TerrainFixture;

import java.awt.Component;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.function.Consumer;

/**
 * A mouse listener for the map panel, to show the terrain-changing menu as needed.
 */
/* package */ class ComponentMouseListener extends MouseAdapter implements ToolTipSource {
	public ComponentMouseListener(final IViewerModel model, final Predicate<TileFixture> zof,
	                              final Comparator<TileFixture> comparator) {
		this.model = model;
		this.zof = zof;
		this.comparator = comparator;
		menu = new TerrainChangingMenu(model.getMapDimensions().getVersion(), model);
		model.addSelectionChangeListener(menu);
		model.addVersionChangeListener(menu);
	}

	private final IViewerModel model;
	private final Predicate<TileFixture> zof;
	private final Comparator<TileFixture> comparator;

	private final TerrainChangingMenu menu;

	private String terrainFixturesAndTop(final Point point) {
		final IMapNG map = model.getMap();
		final StringBuilder builder = new StringBuilder();
		final Consumer<TileFixture> c = fixture -> {
			if (!builder.toString().isEmpty()) {
				builder.append("<br />");
			}
			builder.append(fixture.getShortDescription());
		};
		final List<TileFixture> stream = map.getFixtures(point).stream().filter(zof).sorted(comparator)
			.collect(Collectors.toList());
		if (!stream.isEmpty()) {
			c.accept(stream.get(0));
		}
		stream.stream().filter(TerrainFixture.class::isInstance).forEach(c);
		return builder.toString();
	}

	private Point pointFor(final MouseEvent event) {
		final java.awt.Point eventPoint = event.getPoint();
		final MapDimensions mapDimensions = model.getMapDimensions();
		final int tileSize = scaleZoom(model.getZoomLevel(), mapDimensions.getVersion());
		final VisibleDimensions visibleDimensions = model.getVisibleDimensions();
		return new Point((int) ((eventPoint.getY() / tileSize) + visibleDimensions.getMinimumRow()),
			(int) ((eventPoint.getX() / tileSize) + visibleDimensions.getMinimumColumn()));
	}

	@Override
	public @Nullable String getToolTipText(final MouseEvent event) {
		final MapDimensions mapDimensions = model.getMapDimensions();
		final Point point = pointFor(event);
		if (point.isValid() && point.getRow() < mapDimensions.getRows() &&
				point.getColumn() < mapDimensions.getColumns()) {
			final String mountainString = (model.getMap().isMountainous(point)) ?
				", mountainous" : "";
			return String.format("<html><body>%s: %s%s<br />%s</body></html>", point,
				Optional.ofNullable(model.getMap().getBaseTerrain(point))
					.map(TileType::toString).orElse("not visible"),
				mountainString, terrainFixturesAndTop(point));
		} else {
			return null;
		}
	}

	@Override
	public void mouseClicked(final MouseEvent event) {
		event.getComponent().requestFocusInWindow();
		final MapDimensions mapDimensions = model.getMapDimensions();
		final Point point = pointFor(event);
		LovelaceLogger.trace("User clicked on %s", point);
		if (point.isValid() && point.getRow() < mapDimensions.getRows() &&
				point.getColumn() < mapDimensions.getColumns()) {
			if (event.isPopupTrigger()) {
				model.setInteraction(point);
				menu.show(event.getComponent(), event.getX(), event.getY());
			} else {
				model.setSelection(point);
			}
		}
	}

	@Override
	public void mousePressed(final MouseEvent event) {
		if (event.isPopupTrigger()) {
			model.setInteraction(pointFor(event));
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	@Override
	public void mouseReleased(final MouseEvent event) {
		if (event.isPopupTrigger()) {
			model.setInteraction(pointFor(event));
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	private Pair<Integer, Integer> screenPointFor(final Point point) {
		final MapDimensions mapDimensions = model.getMapDimensions();
		final int tileSize = scaleZoom(model.getZoomLevel(), mapDimensions.getVersion());
		final VisibleDimensions visibleDimensions = model.getVisibleDimensions();
		return Pair.with(
			(point.getColumn() - visibleDimensions.getMinimumColumn()) * tileSize + tileSize / 2,
			(point.getRow() - visibleDimensions.getMinimumRow()) * tileSize + tileSize / 2);
	}

	public void showMenuAtSelection(final @Nullable Component parent) {
		model.setInteraction(model.getSelection());
		final Pair<Integer, Integer> pair = screenPointFor(model.getSelection());
		menu.show(parent, pair.getValue0(), pair.getValue1());
	}

//	@Override
//	public void addSelectionChangeListener(SelectionChangeListener listener) {
//		menu.addSelectionChangeListener(listener);
//	}
//	@Override
//	public void removeSelectionChangeListener(SelectionChangeListener listener) {
//		menu.removeSelectionChangeListener(listener);
//	}
}
