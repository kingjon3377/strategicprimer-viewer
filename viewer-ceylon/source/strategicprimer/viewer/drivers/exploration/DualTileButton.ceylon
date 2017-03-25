import model.map {
    PointFactory,
    TileFixture,
    Point
}
import strategicprimer.viewer.model.map {
    IMapNG
}
import javax.swing {
    JButton
}
import strategicprimer.viewer.drivers.map_viewer {
    tileDrawHelperFactory,
    FixtureMatcher,
    TileDrawHelper
}
import java.awt {
    Graphics,
    Polygon
}
import ceylon.interop.java {
    createJavaIntArray
}
"A button (visually) representing a tile in two maps."
class DualTileButton(IMapNG master, IMapNG subordinate, {FixtureMatcher*} matchers)
        extends JButton() {
    Integer margin = 2;
    variable Point localPoint = PointFactory.invalidPoint;
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
