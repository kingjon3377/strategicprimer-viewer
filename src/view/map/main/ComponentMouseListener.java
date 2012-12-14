package view.map.main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;

import model.map.PointFactory;
import model.viewer.MapModel;
import model.viewer.TileViewSize;
import model.viewer.VisibleDimensions;

/**
 * A mouse listener for the MapComponent, to show the terrain-changing menu as
 * needed.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ComponentMouseListener extends MouseAdapter {
	/**
	 * The map model we refer to.
	 */
	private final MapModel model;
	/**
	 * @param mapModel the map model we'll refer to
	 * @param list a listener to send encounter events to
	 */
	public ComponentMouseListener(final MapModel mapModel,
			final PropertyChangeListener list) {
		super();
		model = mapModel;
		menu = new TerrainChangingMenu(model.getMapDimensions().version,
				model.getSelectedTile(), list, model);
	}

	/**
	 * The terrain-changing menu.
	 */
	private final TerrainChangingMenu menu;
	/**
	 * The helper to tell us the size of a tile.
	 */
	private final TileViewSize tsize = new TileViewSize();

	/**
	 * Handle mouse clicks.
	 *
	 * @param event the event to handle
	 */
	@Override
	public void mouseClicked(final MouseEvent event) {
		final java.awt.Point eventPoint = event.getPoint();
		final VisibleDimensions dimensions = model.getDimensions();
		final int tileSize = tsize.getSize(model.getMapDimensions().version);
		model.setSelection(PointFactory.point(eventPoint.y / tileSize
				+ dimensions.getMinimumRow(), eventPoint.x / tileSize
				+ dimensions.getMinimumCol()));
		event.getComponent().requestFocusInWindow();
		if (event.getClickCount() == 2) {
			model.copyTile(model.getSelectedTile());
		}
		if (event.isPopupTrigger()) {
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	/**
	 * Handle mouse presses.
	 *
	 * @param event the event to handle
	 */
	@Override
	public void mousePressed(final MouseEvent event) {
		if (event.isPopupTrigger()) {
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	/**
	 * Handle mouse releases.
	 *
	 * @param event the event to handle
	 */
	@Override
	public void mouseReleased(final MouseEvent event) {
		if (event.isPopupTrigger()) {
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	/**
	 *
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "ComponentMouseListener";
	}
}
