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
	public MapScrollPanel(IViewerModel model, JComponent component) {
		super(component, null, null, null, null);
		ScrollListener scrollListener = ScrollListener.createScrollBars(model, this);
		model.addGraphicalParamsListener(scrollListener);
		model.addMapChangeListener(scrollListener);
	}
}
