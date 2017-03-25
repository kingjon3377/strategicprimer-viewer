import javax.swing {
    JPopupMenu
}
import strategicprimer.viewer.model.map.fixtures.terrain {
    Forest
}
import model.map.fixtures {
    Ground
}
import model.map {
    PointFactory,
    TerrainFixture,
    TileFixture,
    MapDimensions,
    Point
}
import strategicprimer.viewer.model.map {
    IMapNG
}
import java.awt.event {
    MouseAdapter,
    MouseListener,
    MouseEvent
}
import model.listeners {
    VersionChangeListener,
    SelectionChangeListener,
    SelectionChangeSource
}
import ceylon.math.float {
    halfEven
}
"An interface for the method to get the tool-tip message for the location the mouse
 cursor is over."
interface ToolTipSource {
    shared formal String? getToolTipText(MouseEvent event);
}
"A mouse listener for the map panel, to show the terrain-changing menu as needed."
MouseListener&ToolTipSource&SelectionChangeSource componentMouseListener(
        IViewerModel model, Boolean(TileFixture) zof,
        Comparison(TileFixture, TileFixture) comparator) {
    JPopupMenu&VersionChangeListener&SelectionChangeSource&SelectionChangeListener menu =
            terrainChangingMenu(model.mapDimensions.version, model);
    model.addSelectionChangeListener(menu);
    model.addVersionChangeListener(menu);
    String terrainFixturesAndTop(Point point) {
        IMapNG map = model.map;
        StringBuilder builder = StringBuilder();
        void accept(TileFixture fixture) {
            if (!builder.empty) {
                builder.append("<br />");
            }
            builder.append(fixture.string);
        }
        Ground? ground = map.getGround(point);
        Forest? forest = map.getForest(point);
        {TileFixture*} stream = {ground, forest, *map.getOtherFixtures(point)}
            .coalesced.filter(zof).sort(comparator);
        if (exists top = stream.first) {
            accept(top);
        }
        for (fixture in stream) {
            if (is TerrainFixture fixture) {
                accept(fixture);
            }
        }
        return builder.string;
    }
    object retval extends MouseAdapter() satisfies SelectionChangeSource&ToolTipSource {
        shared actual String? getToolTipText(MouseEvent event) {
            value eventPoint = event.point;
            MapDimensions mapDimensions = model.mapDimensions;
            Integer tileSize = scaleZoom(model.zoomLevel, mapDimensions.version);
            VisibleDimensions visibleDimensions = model.dimensions;
            Point point = PointFactory.point(
                halfEven((eventPoint.y / tileSize) + visibleDimensions.minimumRow)
                    .plus(0.1).integer,
                halfEven((eventPoint.x / tileSize) + visibleDimensions.minimumColumn)
                    .plus(0.1).integer);
            if (point.valid, point.row < mapDimensions.rows,
                point.col < mapDimensions.columns) {
                String mountainString = (model.map.isMountainous(point))
                then ", mountainous" else "";
                return "<html><body>``point``: ``model.map
                    .getBaseTerrain(point)````mountainString``<br />``
                terrainFixturesAndTop(point)``</body></html>";
            } else {
                return null;
            }
        }
        shared actual void mouseClicked(MouseEvent event) {
            event.component.requestFocusInWindow();
            value eventPoint = event.point;
            VisibleDimensions visibleDimensions = model.dimensions;
            MapDimensions mapDimensions = model.mapDimensions;
            Integer tileSize = scaleZoom(model.zoomLevel, mapDimensions.version);
            Point point = PointFactory.point(
                halfEven((eventPoint.y / tileSize) + visibleDimensions.minimumRow)
                    .plus(0.1).integer,
                halfEven((eventPoint.x / tileSize) + visibleDimensions.minimumColumn)
                    .plus(0.1).integer);
            if (point.valid, point.row < mapDimensions.rows,
                point.col < mapDimensions.columns) {
                model.selection = point;
                if (event.popupTrigger) {
                    menu.show(event.component, event.x, event.y);
                }
            }
        }
        shared actual void mousePressed(MouseEvent event) {
            if (event.popupTrigger) {
                menu.show(event.component, event.x, event.y);
            }
        }
        shared actual void mouseReleased(MouseEvent event) {
            if (event.popupTrigger) {
                menu.show(event.component, event.x, event.y);
            }
        }
        shared actual void addSelectionChangeListener(SelectionChangeListener listener) =>
                menu.addSelectionChangeListener(listener);
        shared actual void removeSelectionChangeListener(SelectionChangeListener listener)
                => menu.removeSelectionChangeListener(listener);
    }
    return retval;
}
