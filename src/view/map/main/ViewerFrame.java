package view.map.main;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

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
	 * Initialize size to the specified dimensions. Not that this actually works
	 * ...
	 *
	 * @param width the specified width
	 * @param height the specified height
	 */
	private void initializeDimensions(final int width, final int height) {
		setPreferredSize(new Dimension(width, height));
		setSize(width, height);
		setMaximumSize(new Dimension(width, height));
		setMinimumSize(new Dimension(width, height));
	}

	/**
	 * Constructor.
	 *
	 * @param map The map model.
	 */
	public ViewerFrame(final IViewerModel map) {
		super("Strategic Primer Map Viewer");
		setLayout(new BorderLayout());
		final MapGUI mapPanel = new MapComponent(map);
		final JPanel mapSuperPanel = new JPanel(new BorderLayout());
		mapSuperPanel.add((JComponent) mapPanel, BorderLayout.CENTER);
		new ScrollListener(map, mapSuperPanel).setUpListeners();
		add(mapSuperPanel, BorderLayout.CENTER);
		add(new DetailPanelNG(map.getMapDimensions().version, map),
				BorderLayout.SOUTH);
		initializeDimensions(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		pack();
		((MapComponent) mapPanel).requestFocusInWindow();
	}
}
