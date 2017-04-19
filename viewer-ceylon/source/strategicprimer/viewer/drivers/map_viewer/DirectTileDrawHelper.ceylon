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

import strategicprimer.model.map {
    Point,
    River,
    TileType,
    IMapNG
}

"A [[TileDrawHelper]] for version-1 maps that draws directly instead of creating Shapes,
 which proves more efficent in practice."
object directTileDrawHelper satisfies TileDrawHelper {
    void drawRiver(Graphics pen, River river, Integer xCoordinate,
            Integer yCoordinate, Integer width, Integer height) {
        // TODO: Add some small number (`runtime.epsilon`?) to floats before .integer?
        switch (river)
        case (River.east) {
            pen.fillRect(
                halfEven(width * drawingNumericConstants.riverLongDimension)
                    .integer + xCoordinate,
                halfEven(height * drawingNumericConstants.riverShortStart)
                    .integer + yCoordinate,
                halfEven(width * drawingNumericConstants.riverLongDimension)
                    .integer,
                halfEven(height * drawingNumericConstants.riverShortDimension)
                    .integer);
        }
        case (River.lake) {
            pen.fillOval(
                halfEven(width * drawingNumericConstants.lakeStart).integer + xCoordinate,
                halfEven(height * drawingNumericConstants.lakeStart).integer
                + yCoordinate,
                halfEven(width * drawingNumericConstants.riverLongDimension).integer,
                halfEven(height * drawingNumericConstants.riverLongDimension).integer);
        }
        case (River.north) {
            pen.fillRect(
                halfEven(width * drawingNumericConstants.riverShortStart).integer
                + xCoordinate, yCoordinate,
                halfEven(width * drawingNumericConstants.riverShortDimension).integer,
                halfEven(height * drawingNumericConstants.riverLongDimension).integer);
        }
        case (River.south) {
            pen.fillRect(
                halfEven(width * drawingNumericConstants.riverShortStart).integer
                + xCoordinate,
                halfEven(height * drawingNumericConstants.riverLongDimension).integer +
                yCoordinate,
                halfEven(width * drawingNumericConstants.riverShortDimension).integer,
                halfEven(height * drawingNumericConstants.riverLongDimension).integer);
        }
        case (River.west) {
            pen.fillRect(xCoordinate,
                halfEven(height * drawingNumericConstants.riverShortStart).integer +
                yCoordinate,
                halfEven(width * drawingNumericConstants.riverLongDimension).integer,
                halfEven(height * drawingNumericConstants.riverShortDimension).integer);
        }
    }
    shared actual void drawTile(Graphics pen, IMapNG map, Point location,
            Coordinate coordinates, Coordinate dimensions) {
        Graphics context = pen.create();
        try {
            context.color = colorHelper.get(map.dimensions.version,
                map.baseTerrain(location));
            context.fillRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
            context.color = Color.black;
            context.drawRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
            if (TileType.notVisible == map.baseTerrain(location)) {
                return;
            }
            context.color = Color.\iBLUE;
            for (river in map.rivers(location)) {
                drawRiver(context, river, coordinates.x, coordinates.y, dimensions.x,
                    dimensions.y);
            }
            if (hasAnyForts(map, location)) {
                context.color = fortColor;
                context.fillRect(
                    halfEven(dimensions.x * drawingNumericConstants.fortStart - 1.0)
                        .integer + coordinates.x,
                    halfEven(dimensions.y * drawingNumericConstants.fortStart - 1.0)
                        .integer + coordinates.y,
                    halfEven(dimensions.x * drawingNumericConstants.fortSize).integer,
                    halfEven(dimensions.y * drawingNumericConstants.fortSize).integer);
            }
            if (hasAnyUnits(map, location)) {
                context.color = unitColor;
                context.fillOval(
                    halfEven(dimensions.x * drawingNumericConstants.unitSize).integer +
                    coordinates.x,
                    halfEven(dimensions.y * drawingNumericConstants.unitSize).integer +
                    coordinates.y,
                    halfEven(dimensions.x * drawingNumericConstants.unitSize).integer,
                    halfEven(dimensions.y * drawingNumericConstants.unitSize).integer);
            } // Java version had else-if here, not just if
            if (hasEvent(map, location)) {
                context.color = eventColor;
                context.fillPolygon(
                    createJavaIntArray({
                        halfEven(dimensions.x *
                        drawingNumericConstants.eventStart).integer + coordinates.x,
                        halfEven(dimensions.x *
                        drawingNumericConstants.eventOther).integer + coordinates.x,
                        dimensions.x + coordinates.x}),
                    createJavaIntArray({coordinates.y,
                        halfEven(dimensions.y *
                        drawingNumericConstants.eventOther).integer + coordinates.y,
                        halfEven(dimensions.y *
                        drawingNumericConstants.eventOther).integer + coordinates.y}),
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
