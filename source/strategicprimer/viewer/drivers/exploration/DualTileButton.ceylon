import ceylon.interop.java {
    createJavaIntArray
}

import java.awt {
    Graphics,
    Polygon
}

import javax.swing {
    JButton
}

import strategicprimer.viewer.drivers.map_viewer {
    tileDrawHelperFactory,
    FixtureMatcher,
    TileDrawHelper
}
import strategicprimer.model.map {
    Point,
    TileFixture,
    IMap,
    invalidPoint
}
"A button (visually) representing a tile in two maps."
class DualTileButton(IMap master, IMap subordinate, {FixtureMatcher*} matchers)
        extends JButton() {
    Integer margin = 2;
    variable Point localPoint = invalidPoint;
    shared Point point => localPoint;
    assign point {
        localPoint = point;
        repaint();
    }
    TileDrawHelper helper = tileDrawHelperFactory(2, super.imageUpdate,
                (TileFixture fix) => true, matchers);
    shared actual void paintComponent(Graphics pen) {
        super.paintComponent(pen);
        pen.clip = Polygon(createJavaIntArray({width - margin, margin, margin}),
            createJavaIntArray({margin, height - margin, margin}), 3);
        helper.drawTileTranslated(pen, master, point, width, height);
        pen.clip = Polygon(createJavaIntArray({width - margin, width - margin, margin}),
            createJavaIntArray({margin, height - margin, height - margin}), 3);
        helper.drawTileTranslated(pen, subordinate, point, width, height);
    }
}
