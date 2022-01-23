package drivers.map_viewer;

import static drivers.map_viewer.TileViewSize.scaleZoom;
import java.util.List;
import java.util.stream.Collectors;
import common.map.TileType;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;

import java.util.logging.Logger;
import java.util.logging.Level;
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
	private static final Logger LOGGER = Logger.getLogger(ComponentMouseListener.class.getName());
	public ComponentMouseListener(IViewerModel model, Predicate<TileFixture> zof,
			Comparator<TileFixture> comparator) {
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

	private String terrainFixturesAndTop(Point point) {
		IMapNG map = model.getMap();
		StringBuilder builder = new StringBuilder();
		Consumer<TileFixture> c = fixture -> {
			if (!builder.toString().isEmpty()) {
				builder.append("<br />");
			}
			builder.append(fixture.getShortDescription());
		};
		List<TileFixture> stream = map.getFixtures(point).stream().filter(zof).sorted(comparator)
			.collect(Collectors.toList());
		if (stream.isEmpty()) {
			c.accept(stream.get(0));
		}
		stream.stream().filter(TerrainFixture.class::isInstance).forEach(c);
		return builder.toString();
	}

	private Point pointFor(MouseEvent event) {
		java.awt.Point eventPoint = event.getPoint();
		MapDimensions mapDimensions = model.getMapDimensions();
		int tileSize = scaleZoom(model.getZoomLevel(), mapDimensions.getVersion());
		VisibleDimensions visibleDimensions = model.getVisibleDimensions();
		return new Point((int) ((eventPoint.getY() / tileSize) + visibleDimensions.getMinimumRow()),
			(int) ((eventPoint.getX() / tileSize) + visibleDimensions.getMinimumColumn()));
	}

	@Override
	@Nullable
	public String getToolTipText(MouseEvent event) {
		MapDimensions mapDimensions = model.getMapDimensions();
		Point point = pointFor(event);
		if (point.isValid() && point.getRow() < mapDimensions.getRows() &&
				point.getColumn() < mapDimensions.getColumns()) {
			String mountainString = (model.getMap().isMountainous(point)) ?
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
	public void mouseClicked(MouseEvent event) {
		event.getComponent().requestFocusInWindow();
		MapDimensions mapDimensions = model.getMapDimensions();
		Point point = pointFor(event);
		LOGGER.finer("User clicked on " + point);
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
	public void mousePressed(MouseEvent event) {
		if (event.isPopupTrigger()) {
			model.setInteraction(pointFor(event));
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		if (event.isPopupTrigger()) {
			model.setInteraction(pointFor(event));
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	private Pair<Integer, Integer> screenPointFor(Point point) {
		MapDimensions mapDimensions = model.getMapDimensions();
		int tileSize = scaleZoom(model.getZoomLevel(), mapDimensions.getVersion());
		VisibleDimensions visibleDimensions = model.getVisibleDimensions();
		return Pair.with(
			(point.getColumn() - visibleDimensions.getMinimumColumn()) * tileSize + tileSize / 2,
			(point.getRow() - visibleDimensions.getMinimumRow()) * tileSize + tileSize / 2);
	}

	public void showMenuAtSelection(@Nullable Component parent) {
		model.setInteraction(model.getSelection());
		Pair<Integer, Integer> pair = screenPointFor(model.getSelection());
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
