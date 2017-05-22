import ceylon.collection {
    MutableMap,
    HashMap
}
import java.awt.geom {
    Rectangle2D,
    Line2D,
    Ellipse2D
}
import strategicprimer.model.map {
    Point,
    River,
    TileType,
    IMapNG
}
import java.awt {
    Graphics,
    Graphics2D,
    Shape,
    Color,
    Polygon,
    Rectangle
}
import ceylon.interop.java {
    createJavaIntArray
}
import ceylon.math.float {
    halfEven
}
import lovelace.util.common {
    todo
}
"A class to do the drawing of a tile, whether on a component representing a single tile or
 a single-component map, using cached [[Shape]]s. Note that this is limited to version-1
 maps."
todo("Convert to an object?")
class CachingTileDrawHelper satisfies TileDrawHelper {
    static Float approximatelyZero = 0.000001;
    static Boolean areFloatsDifferent(Float first, Float second) =>
            (first - second).magnitude > approximatelyZero;
    shared new () {}
    "Shapes representing the rivers on the tile."
    MutableMap<River, Shape> rivers = HashMap<River, Shape>();
    "A cached copy of the background."
    variable Rectangle backgroundShape = Rectangle(0, 0, 1, 1);
    "Shape representing an event, or relative text, associated with the tile."
    variable Shape event = Line2D.Double();
    "Shape representing the fortress that might be on the tile."
    variable Shape fortress = event;
    "Shape representing the unit that might be on the tile."
    variable Shape unit = event;
    "Check, and possibly regenerate, the cache: regenerate if the width and height have
     changed."
    void updateCache(Integer width, Integer height) {
        if (areFloatsDifferent(backgroundShape.width, width.float) ||
        areFloatsDifferent(backgroundShape.height, height.float)) {
            backgroundShape = Rectangle(0, 0, width, height);
            rivers.clear();
            rivers.put(River.east, Rectangle2D.Double(
                width * drawingNumericConstants.riverLongDimension,
                height * drawingNumericConstants.riverShortStart,
                width * drawingNumericConstants.riverLongDimension,
                height * drawingNumericConstants.riverShortDimension));
            rivers.put(River.lake, Ellipse2D.Double(
                width * drawingNumericConstants.lakeStart,
                height * drawingNumericConstants.lakeStart,
                width * drawingNumericConstants.riverLongDimension,
                height * drawingNumericConstants.riverLongDimension));
            rivers.put(River.north, Rectangle2D.Double(
                width * drawingNumericConstants.riverShortStart, 0.0,
                width * drawingNumericConstants.riverShortDimension,
                height * drawingNumericConstants.riverLongDimension));
            rivers.put(River.south, Rectangle2D.Double(
                width * drawingNumericConstants.riverShortStart,
                height * drawingNumericConstants.riverLongDimension,
                width * drawingNumericConstants.riverShortDimension,
                height * drawingNumericConstants.riverLongDimension));
            rivers.put(River.west, Rectangle2D.Double(0.0,
                height * drawingNumericConstants.riverShortStart,
                width * drawingNumericConstants.riverLongDimension,
                height * drawingNumericConstants.riverShortDimension));
            fortress = Rectangle2D.Double(
                (width * drawingNumericConstants.fortStart) - 1.0,
                (height * drawingNumericConstants.fortStart) - 1.0,
                width * drawingNumericConstants.fortSize,
                height * drawingNumericConstants.fortSize);
            unit = Ellipse2D.Double(width * drawingNumericConstants.unitSize,
                height * drawingNumericConstants.unitSize,
                width * drawingNumericConstants.unitSize,
                height * drawingNumericConstants.unitSize);
            event = Polygon(
                createJavaIntArray({
                    halfEven(width * drawingNumericConstants.eventStart)
                        .plus(approximatelyZero).integer,
                    halfEven(width * drawingNumericConstants.eventOther)
                        .plus(approximatelyZero).integer, width}),
                createJavaIntArray({0,
                    halfEven(height * drawingNumericConstants.eventOther)
                        .plus(approximatelyZero).integer,
                    halfEven(height * drawingNumericConstants.eventOther)
                        .plus(approximatelyZero).integer}), 3);
        }
    }
    updateCache(2, 2);
    shared actual void drawTileTranslated(Graphics pen, IMapNG map, Point location,
            Integer width, Integer height) {
        assert (is Graphics2D pen);
//        TileType terrain = map.baseTerrain[location]; // TODO: syntax sugar once compiler bug fixed
        TileType terrain = map.baseTerrain.get(location);
        pen.color = colorHelper.get(map.dimensions.version, terrain);
        pen.fill(backgroundShape);
        pen.color = Color.black;
        pen.draw(backgroundShape);
        if (TileType.notVisible != terrain) {
            pen.color = Color.\iBLUE;
//            for (river in map.rivers[location]) {
            for (river in map.rivers.get(location)) {
                if (exists shape = rivers.get(river)) {
                    pen.fill(shape);
                }
                if (hasAnyForts(map, location)) {
                    pen.color = fortColor;
                    pen.fill(fortress);
                }
                if (hasAnyUnits(map, location)) {
                    pen.color = unitColor;
                    pen.fill(unit);
                }
                if (hasEvent(map, location)) {
                    pen.color = eventColor;
                    pen.fill(event);
                }
            }
        }
    }
    shared actual void drawTile(Graphics pen, IMapNG map, Point location,
            Coordinate coordinates, Coordinate dimensions) {
        Graphics context = pen.create(coordinates.x, coordinates.y, dimensions.x,
            dimensions.y);
        try {
            drawTileTranslated(context, map, location, dimensions.x, dimensions.y);
        } finally {
            context.dispose();
        }
    }
}
