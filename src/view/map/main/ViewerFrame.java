package view.map.main;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

import model.viewer.IViewerModel;
import view.map.details.DetailPanelNG;
import controller.map.misc.IOHandler;

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
	 * @param ioHandler the I/O handler, so we can handle 'open' and 'save' menu
	 *        items.
	 */
	public ViewerFrame(final IViewerModel map, final IOHandler ioHandler) {
		super("Strategic Primer Map Viewer");
		final FixtureFilterMenu ffmenu = new FixtureFilterMenu();
		final MapComponent mapPanel = new MapComponent(map, ffmenu);
		map.addGraphicalParamsListener(mapPanel);
		map.addMapChangeListener(mapPanel);
		map.addSelectionChangeListener(mapPanel);
		final DetailPanelNG detailPanel = new DetailPanelNG(
				map.getMapDimensions().version, map.getMap().getPlayers());
		map.addVersionChangeListener(detailPanel);
		map.addSelectionChangeListener(detailPanel);
		final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				true, new MapScrollPanel(map, mapPanel), detailPanel);
		split.setDividerLocation(MAP_PROPORTION);
		split.setResizeWeight(MAP_PROPORTION);
		setContentPane(split);
		initializeDimensions(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		pack();
		mapPanel.requestFocusInWindow();
		final MapWindowSizeListener mwsl = new MapWindowSizeListener(
				mapPanel);
		addWindowListener(mwsl);
		addWindowStateListener(mwsl);

		setJMenuBar(new SPMenu(ioHandler, this, map));
		getJMenuBar().add(ffmenu);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
}
