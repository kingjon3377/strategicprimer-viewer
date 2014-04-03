package view.map.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import model.viewer.IViewerModel;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A class to listen for zoom menu events.
 *
 * @author Jonathan Lovelace
 */
public final class ZoomListener implements ActionListener {
	/**
	 * The map model.
	 */
	private final IViewerModel model;

	/**
	 * Constructor.
	 *
	 * @param vmodel the viewer model, which now handles the zoom level
	 */
	public ZoomListener(final IViewerModel vmodel) {
		model = vmodel;
	}

	/**
	 * @param evt the event to handle
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt == null) {
			return;
		} else if ("zoom in".equalsIgnoreCase(evt.getActionCommand())) {
			model.zoomIn();
		} else if ("zoom out".equalsIgnoreCase(evt.getActionCommand())) {
			model.zoomOut();
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ZoomListener";
	}
}
