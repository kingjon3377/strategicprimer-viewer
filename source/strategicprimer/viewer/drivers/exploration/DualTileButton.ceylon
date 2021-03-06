import java.awt {
    Graphics,
    Polygon
}

import javax.swing {
    JButton
}

import strategicprimer.viewer.drivers.map_viewer {
    TileDrawHelper,
    Ver2TileDrawHelper,
    Coordinate
}
import strategicprimer.model.common.map {
    Point,
    TileFixture,
    IMapNG
}
import strategicprimer.drivers.common {
    FixtureMatcher
}
import java.lang {
    IntArray
}

"A button (visually) representing a tile in two maps."
class DualTileButton(IMapNG master, IMapNG subordinate, {FixtureMatcher*} matchers)
        extends JButton() {
    Integer margin = 2;

    variable Point localPoint = Point.invalidPoint;
    shared Point point => localPoint;
    assign point {
        localPoint = point;
        repaint();
    }

    TileDrawHelper helper = Ver2TileDrawHelper(super.imageUpdate,
                (TileFixture fix) => true, matchers);

    shared actual void paintComponent(Graphics pen) {
        super.paintComponent(pen);
        Coordinate origin = Coordinate(0, 0);
        Coordinate dimensions = Coordinate(width, height);
        pen.clip = Polygon(IntArray.with([width - margin, margin, margin]),
            IntArray.with([margin, height - margin, margin]), 3);
        helper.drawTile(pen, master, point, origin, dimensions);
        pen.clip = Polygon(IntArray.with([width - margin, width - margin, margin]),
            IntArray.with([margin, height - margin, height - margin]), 3);
        helper.drawTile(pen, subordinate, point, origin, dimensions);
    }
}
