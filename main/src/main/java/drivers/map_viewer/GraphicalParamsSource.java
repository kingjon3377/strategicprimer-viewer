package drivers.map_viewer;

/**
 * An interface for objects that tell listeners when the visible dimensions or
 * the tile size/zoom level changed.
 */
public interface GraphicalParamsSource {
    /**
     * Add a listener.
     */
    void addGraphicalParamsListener(GraphicalParamsListener listener);

    /**
     * Remove a listener.
     */
    void removeGraphicalParamsListener(GraphicalParamsListener listener);
}
