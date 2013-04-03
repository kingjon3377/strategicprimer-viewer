package view.map.main;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import model.viewer.IViewerModel;
import view.map.details.DetailPanelNG;

/**
 * The main driver class for the viewer app.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ViewerFrame extends JFrame {
	/**
	 * Default width of the Frame.
	 */
	private static final int DEFAULT_WIDTH = 800;
	/**
	 * Default height of the Frame.
	 */
	private static final int DEFAULT_HEIGHT = 600;
	/**
	 * The default proportion between map and detail panels.
	 */
	private static final double MAP_PROPORTION = 0.9;
	/**
	 * Initialize size to the specified dimensions. Not that this actually works
	 * ...
	 *
	 * @param width the specified width
	 * @param height the specified height
	 */
	private void initializeDimensions(final int width, final int height) {
		setPreferredSize(new Dimension(width, height));
		setSize(width, height);
		setMinimumSize(new Dimension(width, height));
	}

	/**
	 * Constructor.
	 *
	 * @param map The map model.
	 */
	public ViewerFrame(final IViewerModel map) {
		super("Strategic Primer Map Viewer");
		final MapGUI mapPanel = new MapComponent(map);
		final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				new MapScrollPanel(map, (MapComponent) mapPanel),
						new DetailPanelNG(map.getMapDimensions().version, map));
		split.setDividerLocation(MAP_PROPORTION);
		split.setResizeWeight(MAP_PROPORTION);
		setContentPane(split);
		initializeDimensions(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		pack();
		((MapComponent) mapPanel).requestFocusInWindow();
		final MapWindowSizeListener mwsl = new MapWindowSizeListener((MapComponent) mapPanel);
		addWindowListener(mwsl);
		addWindowStateListener(mwsl);
	}
}
