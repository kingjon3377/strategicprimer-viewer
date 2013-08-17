package view.map.main;

import javax.swing.JComponent;

import model.viewer.IViewerModel;
import view.util.BorderedPanel;
/**
 * A panel to contain the map GUI and add scrollbars to it.
 * @author Jonathan Lovelace
 *
 */
public class MapScrollPanel extends BorderedPanel {
	/**
	 * Constructor.
	 * @param map the viewer model
	 * @param component the map component
	 */
	public MapScrollPanel(final IViewerModel map, final JComponent component) {
		super();
		setCenter(component);
		new ScrollListener(map, this).setUpListeners();
	}
}
