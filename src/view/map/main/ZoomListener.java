package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import model.viewer.IViewerModel;

/**
 * A class to listen for zoom menu events.
 * @author Jonathan Lovelace
 */
public final class ZoomListener implements ActionListener, Serializable {
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