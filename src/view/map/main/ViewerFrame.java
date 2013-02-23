package view.map.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import model.viewer.MapModel;
import model.viewer.TileViewSize;
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
	 * File-choosing dialog. Used often, but immutable, so we don't want to have
	 * to construct it every time.
	 */
	private final JFileChooser chooser = new JFileChooser(".");

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
	public ViewerFrame(final MapModel map) {
		super("Strategic Primer Map Viewer");
		setLayout(new BorderLayout());
		chooser.setFileFilter(new MapFileFilter());
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
		zoomListener = new ZoomListener(((MapComponent) mapPanel).getTileSize());
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
		 * @param tsize the tile-size-encapsulating object
		 */
		ZoomListener(final TileViewSize tsize) {
			tvs = tsize;
		}
		/**
		 * The tile-size-encapsulating object.
		 */
		private final TileViewSize tvs;
		/**
		 * @param evt the event to handle
		 */
		@Override
		public void actionPerformed(final ActionEvent evt) {
			if ("zoom in".equalsIgnoreCase(evt.getActionCommand())) {
				tvs.increase(2);
			} else if ("zoom out".equalsIgnoreCase(evt.getActionCommand())) {
				tvs.decrease(2);
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
