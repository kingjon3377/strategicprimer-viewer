import ceylon.numeric.float {
    halfEven
}

import java.awt {
    Graphics,
    Color
}

import strategicprimer.model.impl.map {
    Point,
    River,
    IMapNG
}
import java.lang {
    IntArray
}

"A [[TileDrawHelper]] for version-1 maps that draws directly instead of creating Shapes,
 which proves more efficent in practice."
object directTileDrawHelper satisfies TileDrawHelper {
    Integer multiply(Integer one, Float two) =>
            (halfEven(one * two) + runtime.epsilon).integer;
    void drawRiver(Graphics pen, River river, Integer xCoordinate,
            Integer yCoordinate, Integer width, Integer height) {
        switch (river)
        case (River.east) {
            pen.fillRect(
                multiply(width, drawingNumericConstants.riverLongDimension) + xCoordinate,
                multiply(height, drawingNumericConstants.riverShortStart) + yCoordinate,
                multiply(width, drawingNumericConstants.riverLongDimension),
                multiply(height, drawingNumericConstants.riverShortDimension));
        }
        case (River.lake) {
            pen.fillOval(
                multiply(width, drawingNumericConstants.lakeStart) + xCoordinate,
                multiply(height, drawingNumericConstants.lakeStart) + yCoordinate,
                multiply(width, drawingNumericConstants.riverLongDimension),
                multiply(height, drawingNumericConstants.riverLongDimension));
        }
        case (River.north) {
            pen.fillRect(
                multiply(width, drawingNumericConstants.riverShortStart) + xCoordinate,
                yCoordinate,
                multiply(width, drawingNumericConstants.riverShortDimension),
                multiply(height, drawingNumericConstants.riverLongDimension));
        }
        case (River.south) {
            pen.fillRect(
                multiply(width, drawingNumericConstants.riverShortStart) + xCoordinate,
                multiply(height, drawingNumericConstants.riverLongDimension)
                    + yCoordinate,
                multiply(width, drawingNumericConstants.riverShortDimension),
                multiply(height, drawingNumericConstants.riverLongDimension));
        }
        case (River.west) {
            pen.fillRect(xCoordinate,
                multiply(height, drawingNumericConstants.riverShortStart) + yCoordinate,
                multiply(width, drawingNumericConstants.riverLongDimension),
                multiply(height, drawingNumericConstants.riverShortDimension));
        }
    }
    shared actual void drawTile(Graphics pen, IMapNG map, Point location,
            Coordinate coordinates, Coordinate dimensions) {
        Graphics context = pen.create();
        try {
            context.color = colorHelper.get(map.dimensions.version,
//                map.baseTerrain[location]); // TODO: syntax sugar once compiler bug fixed
                map.baseTerrain.get(location));
            context.fillRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
            context.color = Color.black;
            context.drawRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
            if (!map.baseTerrain[location] exists) {
                return;
            }
            context.color = Color.\iBLUE;
//            for (river in map.rivers[location]) {
            for (river in map.rivers.get(location)) {
                drawRiver(context, river, coordinates.x, coordinates.y, dimensions.x,
                    dimensions.y);
            }
            if (hasAnyForts(map, location)) {
                context.color = fortColor;
                context.fillRect(
                    multiply(dimensions.x, drawingNumericConstants.fortStart) - 1
                        + coordinates.x,
                    multiply(dimensions.y, drawingNumericConstants.fortStart) - 1
                        + coordinates.y,
                    multiply(dimensions.x, drawingNumericConstants.fortSize),
                    multiply(dimensions.y, drawingNumericConstants.fortSize));
            }
            if (hasAnyUnits(map, location)) {
                context.color = unitColor;
                context.fillOval(
                    multiply(dimensions.x, drawingNumericConstants.unitSize)
                        + coordinates.x,
                    multiply(dimensions.y, drawingNumericConstants.unitSize)
                        + coordinates.y,
                    multiply(dimensions.x, drawingNumericConstants.unitSize),
                    multiply(dimensions.y, drawingNumericConstants.unitSize));
            } // Java version had else-if here, not just if
            if (hasEvent(map, location)) {
                context.color = eventColor;
                context.fillPolygon(
                    IntArray.with([
                        multiply(dimensions.x,
                            drawingNumericConstants.eventStart) + coordinates.x,
                        multiply(dimensions.x,
                            drawingNumericConstants.eventOther) + coordinates.x,
                        dimensions.x + coordinates.x]),
                    IntArray.with([coordinates.y,
                        multiply(dimensions.y,
                            drawingNumericConstants.eventOther) + coordinates.y,
                        multiply(dimensions.y,
                            drawingNumericConstants.eventOther) + coordinates.y]),
                    3);
            }
        } finally {
            context.dispose();
        }
    }
    Coordinate origin = Coordinate(0, 0);
    shared actual void drawTileTranslated(Graphics pen, IMapNG map,
            Point location, Integer width, Integer height) =>
                drawTile(pen, map, location, origin, Coordinate(width, height));
}
