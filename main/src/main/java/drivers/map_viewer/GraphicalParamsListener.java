package drivers.map_viewer;

import java.util.EventListener;

/**
 * An interface for objects that want to keep abreast of visible dimensions and zoom level.
 *
 * TODO: Take a polymorphic Event object instead of specifying two methods?
 */
public interface GraphicalParamsListener extends EventListener {
    /**
     * Handle a change in map dimensions.
     *
     * @param oldDimensions The previous dimensions
     * @param newDimensions The new dimensions
     */
    void dimensionsChanged(VisibleDimensions oldDimensions, VisibleDimensions newDimensions);

    /**
     * Handle a change in tile size (that is, zoom level).
     *
     * @param oldSize The previous tile size/zoom level
     * @param newSize The new tile size/zoom level
     */
    void tileSizeChanged(int oldSize, int newSize);
}
