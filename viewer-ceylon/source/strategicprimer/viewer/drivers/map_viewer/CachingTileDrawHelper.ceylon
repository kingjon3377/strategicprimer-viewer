import ceylon.collection {
    MutableMap,
    HashMap
}
import java.awt.geom {
    Rectangle2D,
    Line2D,
    Ellipse2D
}
import view.util {
    Coordinate
}
import model.map {
    River,
    TileType,
    Point,
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
                width * DrawingNumericConstants.riverLongDimension,
                height * DrawingNumericConstants.riverShortStart,
                width * DrawingNumericConstants.riverLongDimension,
                height * DrawingNumericConstants.riverShortDimension));
            rivers.put(River.lake, Ellipse2D.Double(
                width * DrawingNumericConstants.lakeStart,
                height * DrawingNumericConstants.lakeStart,
                width * DrawingNumericConstants.riverLongDimension,
                height * DrawingNumericConstants.riverLongDimension));
            rivers.put(River.north, Rectangle2D.Double(
                width * DrawingNumericConstants.riverShortStart, 0.0,
                width * DrawingNumericConstants.riverShortDimension,
                height * DrawingNumericConstants.riverLongDimension));
            rivers.put(River.south, Rectangle2D.Double(
                width * DrawingNumericConstants.riverShortStart,
                height * DrawingNumericConstants.riverLongDimension,
                width * DrawingNumericConstants.riverShortDimension,
                height * DrawingNumericConstants.riverLongDimension));
            rivers.put(River.west, Rectangle2D.Double(0.0,
                height * DrawingNumericConstants.riverShortStart,
                width * DrawingNumericConstants.riverLongDimension,
                height * DrawingNumericConstants.riverShortDimension));
            fortress = Rectangle2D.Double(
                (width * DrawingNumericConstants.fortStart) - 1.0,
                (height * DrawingNumericConstants.fortStart) - 1.0,
                width * DrawingNumericConstants.fortSize,
                height * DrawingNumericConstants.fortSize);
            unit = Ellipse2D.Double(width * DrawingNumericConstants.unitSize,
                height * DrawingNumericConstants.unitSize,
                width * DrawingNumericConstants.unitSize,
                height * DrawingNumericConstants.unitSize);
            event = Polygon(
                createJavaIntArray({
                    halfEven(width * DrawingNumericConstants.eventStart)
                        .plus(approximatelyZero).integer,
                    halfEven(width * DrawingNumericConstants.eventOther)
                        .plus(approximatelyZero).integer, width}),
                createJavaIntArray({0,
                    halfEven(height * DrawingNumericConstants.eventOther)
                        .plus(approximatelyZero).integer,
                    halfEven(height * DrawingNumericConstants.eventOther)
                        .plus(approximatelyZero).integer}), 3);
        }
    }
    updateCache(2, 2);
    shared actual void drawTileTranslated(Graphics pen, IMapNG map, Point location,
            Integer width, Integer height) {
        assert (is Graphics2D pen);
        TileType terrain = map.getBaseTerrain(location);
        pen.color = colorHelper.get(map.dimensions().version, terrain);
        pen.fill(backgroundShape);
        pen.color = Color.black;
        pen.draw(backgroundShape);
        if (TileType.notVisible != terrain) {
            pen.color = Color.\iBLUE;
            for (river in map.getRivers(location)) {
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
