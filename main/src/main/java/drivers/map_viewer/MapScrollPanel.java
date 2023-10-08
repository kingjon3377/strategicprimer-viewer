package drivers.map_viewer;

import javax.swing.JComponent;

import lovelace.util.BorderedPanel;

/**
 * Encapsulate the map component in a panel with scroll-bars and set up the
 * connection between the scroll-bars and the map's scrolling.
 *
 * TODO: convert (back) to static method in MapComponent or some such?
 */
/* package */ class MapScrollPanel extends BorderedPanel {
    private static final long serialVersionUID = 1L;

    public MapScrollPanel(final IViewerModel model, final JComponent component) {
        super(component, null, null, null, null);
        final ScrollListener scrollListener = ScrollListener.createScrollBars(model, this);
        model.addGraphicalParamsListener(scrollListener);
        model.addMapChangeListener(scrollListener);
    }
}
