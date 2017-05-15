import javax.swing {
    JComponent
}
import lovelace.util.jvm {
    BorderedPanel
}
"Encapsulate the map component in a panel with scroll-bars and set up the connection
 between the scroll-bars and the map's scrolling."
BorderedPanel mapScrollPanel(IViewerModel model, JComponent component) {
    BorderedPanel retval = BorderedPanel(component, null, null, null, null);
    ScrollListener scrollListener = ScrollListener.createScrollBars(model, retval);
    model.addGraphicalParamsListener(scrollListener);
    model.addMapChangeListener(scrollListener);
    return retval;
}
