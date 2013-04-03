package view.map.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

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
//		add(new DetailPanel(map.getMainMap().getVersion(), map, mapPanel),
//				BorderLayout.SOUTH);
		add(new DetailPanelNG(map.getMapDimensions().version, map, mapPanel),
				BorderLayout.SOUTH);
		final JPanel mapSuperPanel = new JPanel(new BorderLayout());
		mapSuperPanel.add((JComponent) mapPanel, BorderLayout.CENTER);
		new ScrollListener(map, mapSuperPanel).setUpListeners();
		add(mapSuperPanel, BorderLayout.CENTER);
		initializeDimensions(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		zoomListener = new ZoomListener(map);
		pack();
		((MapComponent) mapPanel).requestFocusInWindow();
	}
	/**
	 * A class to listen for zoom menu events.
	 */
	private static final class ZoomListener implements ActionListener, Serializable {
		/**
		 * Version UID for serialization.
		 */
		private static final long serialVersionUID = 1L;
		/**
		 * Constructor.
		 * @param vmodel the viewer model, which now handles the zoom level
		 */
		ZoomListener(final IViewerModel vmodel) {
			model = vmodel;
		}
		/**
		 * The map model.
		 */
		private final IViewerModel model;
		/**
		 * @param evt the event to handle
		 */
		@Override
		public void actionPerformed(final ActionEvent evt) {
			if ("zoom in".equalsIgnoreCase(evt.getActionCommand())) {
				model.zoomIn();
			} else if ("zoom out".equalsIgnoreCase(evt.getActionCommand())) {
				model.zoomOut();
			}
		}
	}
	/**
	 * A listener to handle menu- or keypress-based zooming.
	 */
	private final ActionListener zoomListener;
	/**
	 * @return the listener to handle menu-based zooming
	 */
	public ActionListener getZoomListener() {
		return zoomListener;
	}
	/**
	 * @return this frame
	 */
	public Frame getFrame() {
		return this;
	}
}
