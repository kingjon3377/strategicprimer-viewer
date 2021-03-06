import java.awt.event {
    MouseAdapter,
    MouseEvent
}

import strategicprimer.model.common.map {
    MapDimensions,
    Point,
    TileFixture,
    IMapNG
}
import strategicprimer.model.common.map.fixtures {
    TerrainFixture
}

import java.awt {
    Component
}

"An interface for the method to get the tool-tip message for the location the mouse
 cursor is over."
interface ToolTipSource {
    shared formal String? getToolTipText(MouseEvent event);
}

"A mouse listener for the map panel, to show the terrain-changing menu as needed."
class ComponentMouseListener(IViewerModel model, Boolean(TileFixture) zof,
            Comparison(TileFixture, TileFixture) comparator) extends MouseAdapter()
        satisfies ToolTipSource {
    TerrainChangingMenu menu = TerrainChangingMenu(model.mapDimensions.version, model);
    model.addSelectionChangeListener(menu);
    model.addVersionChangeListener(menu);

    String terrainFixturesAndTop(Point point) {
        IMapNG map = model.map;
        StringBuilder builder = StringBuilder();
        void accept(TileFixture fixture) {
            if (!builder.empty) {
                builder.append("<br />");
            }
            builder.append(fixture.shortDescription);
        }
//        {TileFixture*} stream = map.fixtures[point].filter(zof).sort(comparator); // TODO: syntax sugar once compiler bug fixed
        {TileFixture*} stream = map.fixtures.get(point).filter(zof).sort(comparator);
        if (exists top = stream.first) {
            accept(top);
        }
        stream.narrow<TerrainFixture>().each(accept);
        return builder.string;
    }

    Point pointFor(MouseEvent event) {
        value eventPoint = event.point;
        MapDimensions mapDimensions = model.mapDimensions;
        Integer tileSize = scaleZoom(model.zoomLevel, mapDimensions.version);
        VisibleDimensions visibleDimensions = model.visibleDimensions;
        return Point(((eventPoint.y / tileSize) + visibleDimensions.minimumRow).integer,
            ((eventPoint.x / tileSize) + visibleDimensions.minimumColumn).integer);
    }

    shared actual String? getToolTipText(MouseEvent event) {
        MapDimensions mapDimensions = model.mapDimensions;
        Point point = pointFor(event);
        if (point.valid, point.row < mapDimensions.rows,
                point.column < mapDimensions.columns) {
//            String mountainString = (model.map.mountainous[point]) // TODO: syntax sugar once compiler bug fixed
            String mountainString = (model.map.mountainous.get(point))
                        then ", mountainous" else "";
            return "<html><body>``point``: ``model.map
                    .baseTerrain[point] else "not visible"````mountainString``<br />``
                    terrainFixturesAndTop(point)``</body></html>";
        } else {
            return null;
        }
    }

    shared actual void mouseClicked(MouseEvent event) {
        event.component.requestFocusInWindow();
        MapDimensions mapDimensions = model.mapDimensions;
        Point point = pointFor(event);
        log.trace("User clicked on ``point``");
        if (point.valid, point.row < mapDimensions.rows,
                point.column < mapDimensions.columns) {
            if (event.popupTrigger) {
                model.interaction = point;
                menu.show(event.component, event.x, event.y);
            } else {
                model.selection = point;
            }
        }
    }

    shared actual void mousePressed(MouseEvent event) {
        if (event.popupTrigger) {
            model.interaction = pointFor(event);
            menu.show(event.component, event.x, event.y);
        }
    }

    shared actual void mouseReleased(MouseEvent event) {
        if (event.popupTrigger) {
            model.interaction  = pointFor(event);
            menu.show(event.component, event.x, event.y);
        }
    }

    [Integer, Integer] screenPointFor(Point point) {
        MapDimensions mapDimensions = model.mapDimensions;
        Integer tileSize = scaleZoom(model.zoomLevel, mapDimensions.version);
        VisibleDimensions visibleDimensions = model.visibleDimensions;
        return [(point.column - visibleDimensions.minimumColumn) * tileSize + tileSize / 2,
            (point.row - visibleDimensions.minimumRow) * tileSize + tileSize / 2];
    }

    shared void showMenuAtSelection(Component? parent) {
        model.interaction = model.selection;
        menu.show(parent, *screenPointFor(model.selection));
    }

//    shared actual void addSelectionChangeListener(SelectionChangeListener listener) =>
//            menu.addSelectionChangeListener(listener);
//    shared actual void removeSelectionChangeListener(SelectionChangeListener listener)
//            => menu.removeSelectionChangeListener(listener);
}
