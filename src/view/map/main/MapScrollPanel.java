package view.map.main;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import model.viewer.IViewerModel;
/**
 * A panel to contain the map GUI and add scrollbars to it.
 * @author Jonathan Lovelace
 *
 */
public class MapScrollPanel extends JPanel {
	/**
	 * Constructor.
	 * @param map the viewer model
	 * @param component the map component
	 */
	public MapScrollPanel(final IViewerModel map, final JComponent component) {
		super(new BorderLayout());
		add(component, BorderLayout.CENTER);
		new ScrollListener(map, this).setUpListeners();
	}
}
