import strategicprimer.model.common.map {
    Point,
    IMapNG
}
import java.awt {
    Graphics
}

"An interface for helpers that do the drawing of tiles in various components."
shared interface TileDrawHelper {
    "Draw a tile, at the given coordinates."
    shared formal void drawTile(
            "The graphics context"
            Graphics pen,
            "The map to draw a tile from"
            IMapNG map,
            "The (location of the) tile to draw"
            Point location,
            "The coordinates of the tile's upper-left corner."
            Coordinate coordinates,
            "The width ('x') and height ('y') to draw the tile within."
            Coordinate dimensions);
}
