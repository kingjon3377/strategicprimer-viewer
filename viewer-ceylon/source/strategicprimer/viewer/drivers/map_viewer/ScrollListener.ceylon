import java.awt.event {
    AdjustmentEvent
}
import java.awt {
    BorderLayout,
    Adjustable
}
import model.map {
    MapDimensions,
    Point
}
import lovelace.util.common {
    todo
}
import lovelace.util.jvm {
    BorderedPanel
}
import model.listeners {
    MapChangeListener,
    GraphicalParamsListener,
    SelectionChangeListener
}
import javax.swing {
    JScrollBar,
    JComponent,
    InputVerifier
}
import ceylon.interop.java {
    javaString
}
import model.viewer {
    VisibleDimensions
}
"A class to change the visible area of the map based on the user's use of the scrollbars."
todo("Maybe keep track of visible dimensions and selected point directly instaed of
      through the model, so we can drop the reference to the model.")
class ScrollListener satisfies MapChangeListener&SelectionChangeListener&
        GraphicalParamsListener {
    static class LocalInputVerifier extends InputVerifier {
        Integer() mapDimension;
        Integer() visibleDimension;
        shared new horizontal(MapDimensions() mapDimsSource,
                VisibleDimensions() visibleDimsSource) extends InputVerifier() {
            mapDimension = () => mapDimsSource().columns;
            visibleDimension = () => visibleDimsSource().width;
        }
        shared new vertical(MapDimensions() mapDimsSource,
                VisibleDimensions() visibleDimsSource) extends InputVerifier() {
            mapDimension = () => mapDimsSource().rows;
            visibleDimension = () => visibleDimsSource().height;
        }
        "A scrollbar is valid if its value is between 0 and the size of the map minus the
         visible size of the map (that subtraction is to prevent scrolling so far that
         empty tiles show to the right of or below the map)."
        shared actual Boolean verify(JComponent input) {
            if (is JScrollBar input) {
                return (0..(mapDimension() - visibleDimension())).contains(input.\ivalue);
            } else {
                return false;
            }
        }
    }
    IViewerModel model;
    JScrollBar horizontalBar;
    JScrollBar verticalBar;
    variable MapDimensions mapDimensions;
    variable VisibleDimensions visibleDimensions;
    shared new (IViewerModel mapModel, JScrollBar horizontal, JScrollBar vertical) {
        model = mapModel;
        visibleDimensions = mapModel.dimensions;
        mapDimensions = mapModel.mapDimensions;
        Point selectedPoint = mapModel.selection;
        horizontalBar = horizontal;
        horizontal.model.setRangeProperties(largest(selectedPoint.col, 0), 1, 0,
            mapDimensions.columns - visibleDimensions.width, false);
        horizontal.inputVerifier = LocalInputVerifier.horizontal(() => mapDimensions,
                    () => visibleDimensions);
        verticalBar = vertical;
        vertical.model.setRangeProperties(largest(selectedPoint.row, 0), 1, 0,
            mapDimensions.rows - visibleDimensions.height, false);
        vertical.inputVerifier = LocalInputVerifier.vertical(() => mapDimensions,
                    () => visibleDimensions);
        void adjustmentListener(AdjustmentEvent event) =>
                model.dimensions = VisibleDimensions(verticalBar.\ivalue,
                    verticalBar.\ivalue + visibleDimensions.height, horizontalBar.\ivalue,
                    horizontalBar.\ivalue + visibleDimensions.width);
        horizontalBar.addAdjustmentListener(adjustmentListener);
        verticalBar.addAdjustmentListener(adjustmentListener);
    }
    "Alternate constructor that adds new scroll-bars to an existing component. This only
     works if that component is laid out using a [[BorderLayout]] and doesn't already have
      members at page-end and line-end."
    shared new createScrollBars(IViewerModel mapModel, BorderedPanel component)
            extends ScrollListener(mapModel, JScrollBar(Adjustable.horizontal),
        JScrollBar(Adjustable.vertical)) {
        component.add(horizontalBar, javaString(BorderLayout.pageEnd));
        component.add(verticalBar, javaString(BorderLayout.lineEnd));
    }
    "Handle a change in visible dimensions."
    shared actual void dimensionsChanged(VisibleDimensions oldDimensions,
            VisibleDimensions newDimensions) {
        visibleDimensions = newDimensions;
        horizontalBar.model.setRangeProperties(largest(model.selection.col, 0), 1, 0,
            mapDimensions.columns - newDimensions.width, false);
        verticalBar.model.setRangeProperties(largest(model.selection.row, 0), 1, 0,
            mapDimensions.rows - newDimensions.height, false);
    }
    "Ignored."
    todo("Should we really ignore this?")
    shared actual void tileSizeChanged(Integer oldSize, Integer newSize) { }
    "Handle a change to the selected location in the map. The property-change based
     version this replaced went to the model for the selected point rather than looking
     at the reported new value; since it's typesafe here, and probably faster, this
     switched to using the new value it was passed."
    shared actual void selectedPointChanged(Point? old, Point newPoint) {
        VisibleDimensions temp = model.dimensions;
        if (!((temp.minimumCol)..(temp.maximumCol + 1)).contains(newPoint.col)) {
            horizontalBar.model.\ivalue = largest(newPoint.col, 0);
        }
        if (!((temp.minimumRow)..(temp.maximumRow + 1)).contains(newPoint.row)) {
            verticalBar.model.\ivalue = largest(newPoint.row, 0);
        }
    }
    "Handle notification that a new map was loaded."
    shared actual void mapChanged() {
        mapDimensions = model.mapDimensions;
        visibleDimensions = model.dimensions;
        horizontalBar.model.setRangeProperties(0, 1, 0,
            mapDimensions.columns - visibleDimensions.width, false);
        verticalBar.model.setRangeProperties(0, 1, 0,
            mapDimensions.rows - visibleDimensions.height, false);
    }
}
