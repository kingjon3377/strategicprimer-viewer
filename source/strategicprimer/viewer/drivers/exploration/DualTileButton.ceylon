import java.awt {
    Graphics,
    Polygon
}

import javax.swing {
    JButton
}

import strategicprimer.viewer.drivers.map_viewer {
	tileDrawHelperFactory,
	TileDrawHelper
}
import strategicprimer.model.map {
    Point,
    TileFixture,
    IMapNG,
    invalidPoint
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
        pen.clip = Polygon(IntArray.with([width - margin, margin, margin]),
            IntArray.with([margin, height - margin, margin]), 3);
        helper.drawTileTranslated(pen, master, point, width, height);
        pen.clip = Polygon(IntArray.with([width - margin, width - margin, margin]),
            IntArray.with([margin, height - margin, height - margin]), 3);
        helper.drawTileTranslated(pen, subordinate, point, width, height);
    }
}
