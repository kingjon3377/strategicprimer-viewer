import ceylon.interop.java {
    createJavaIntArray
}
import ceylon.math.float {
    halfEven
}

import java.awt {
    Graphics,
    Color
}

import lovelace.util.common {
    todo
}

import model.map {
    River,
    Point
}

import strategicprimer.viewer.model.map {
    TileType,
    IMapNG,
    coordinateFactory
}

import view.util {
    Coordinate
}
"A [[TileDrawHelper]] for version-1 maps that draws directly instead of creating Shapes,
 which proves more efficent in practice."
todo("Convert to an object?")
class DirectTileDrawHelper() satisfies TileDrawHelper {
    void drawRiver(Graphics pen, River river, Integer xCoordinate,
            Integer yCoordinate, Integer width, Integer height) {
        // TODO: Add some small number to floats before .integer?
        switch (river)
        case (River.east) {
            pen.fillRect(
                halfEven(width * DrawingNumericConstants.riverLongDimension)
                    .integer + xCoordinate,
                halfEven(height * DrawingNumericConstants.riverShortStart)
                    .integer + yCoordinate,
                halfEven(width * DrawingNumericConstants.riverLongDimension)
                    .integer,
                halfEven(height * DrawingNumericConstants.riverShortDimension)
                    .integer);
        }
        case (River.lake) {
            pen.fillOval(
                halfEven(width * DrawingNumericConstants.lakeStart).integer + xCoordinate,
                halfEven(height * DrawingNumericConstants.lakeStart).integer
                + yCoordinate,
                halfEven(width * DrawingNumericConstants.riverLongDimension).integer,
                halfEven(height * DrawingNumericConstants.riverLongDimension).integer);
        }
        case (River.north) {
            pen.fillRect(
                halfEven(width * DrawingNumericConstants.riverShortStart).integer
                + xCoordinate, yCoordinate,
                halfEven(width * DrawingNumericConstants.riverShortDimension).integer,
                halfEven(height * DrawingNumericConstants.riverLongDimension).integer);
        }
        case (River.south) {
            pen.fillRect(
                halfEven(width * DrawingNumericConstants.riverShortStart).integer
                + xCoordinate,
                halfEven(height * DrawingNumericConstants.riverLongDimension).integer +
                yCoordinate,
                halfEven(width * DrawingNumericConstants.riverShortDimension).integer,
                halfEven(height * DrawingNumericConstants.riverLongDimension).integer);
        }
        case (River.west) {
            pen.fillRect(xCoordinate,
                halfEven(height * DrawingNumericConstants.riverShortStart).integer +
                yCoordinate,
                halfEven(width * DrawingNumericConstants.riverLongDimension).integer,
                halfEven(height * DrawingNumericConstants.riverShortDimension).integer);
        }
    }
    shared actual void drawTile(Graphics pen, IMapNG map, Point location,
            Coordinate coordinates, Coordinate dimensions) {
        Graphics context = pen.create();
        try {
            context.color = colorHelper.get(map.dimensions.version,
                map.getBaseTerrain(location));
            context.fillRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
            context.color = Color.black;
            context.drawRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
            if (TileType.notVisible == map.getBaseTerrain(location)) {
                return;
            }
            context.color = Color.\iBLUE;
            for (river in map.getRivers(location)) {
                drawRiver(context, river, coordinates.x, coordinates.y, dimensions.x,
                    dimensions.y);
            }
            if (hasAnyForts(map, location)) {
                context.color = fortColor;
                context.fillRect(
                    halfEven(dimensions.x * DrawingNumericConstants.fortStart - 1.0)
                        .integer + coordinates.x,
                    halfEven(dimensions.y * DrawingNumericConstants.fortStart - 1.0)
                        .integer + coordinates.y,
                    halfEven(dimensions.x * DrawingNumericConstants.fortSize).integer,
                    halfEven(dimensions.y * DrawingNumericConstants.fortSize).integer);
            }
            if (hasAnyUnits(map, location)) {
                context.color = unitColor;
                context.fillOval(
                    halfEven(dimensions.x * DrawingNumericConstants.unitSize).integer +
                    coordinates.x,
                    halfEven(dimensions.y * DrawingNumericConstants.unitSize).integer +
                    coordinates.y,
                    halfEven(dimensions.x * DrawingNumericConstants.unitSize).integer,
                    halfEven(dimensions.y * DrawingNumericConstants.unitSize).integer);
            } // Java version had else-if here, not just if
            if (hasEvent(map, location)) {
                context.color = eventColor;
                context.fillPolygon(
                    createJavaIntArray({
                        halfEven(dimensions.x *
                        DrawingNumericConstants.eventStart).integer + coordinates.x,
                        halfEven(dimensions.x *
                        DrawingNumericConstants.eventOther).integer + coordinates.x,
                        dimensions.x + coordinates.x}),
                    createJavaIntArray({coordinates.y,
                        halfEven(dimensions.y *
                        DrawingNumericConstants.eventOther).integer + coordinates.y,
                        halfEven(dimensions.y *
                        DrawingNumericConstants.eventOther).integer + coordinates.y}),
                    3);
            }
        } finally {
            context.dispose();
        }
    }
    shared actual void drawTileTranslated(Graphics pen, IMapNG map,
            Point location, Integer width, Integer height) =>
            drawTile(pen, map, location, coordinateFactory(0, 0),
                coordinateFactory(width, height));
}
