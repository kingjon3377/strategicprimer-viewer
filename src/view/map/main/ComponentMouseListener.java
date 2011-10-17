package view.map.main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import model.viewer.MapModel;
/**
 * A mouse listener for the MapComponent.
 * @author Jonathan Lovelace
 *
 */
public final class ComponentMouseListener extends MouseAdapter {
	/**
	 * The map model we refer to.
	 */
	private final MapModel model;
	/**
	 * A helper to handle event sending.
	 */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	/**
	 * @param mapModel the map model we'll refer to
	 * @param list a listener to send encounter events to
	 */
	public ComponentMouseListener(final MapModel mapModel, final PropertyChangeListener list) {
		super();
		model = mapModel;
		menu.setTile(model.getSelectedTile());
		model.addPropertyChangeListener(menu);
		pcs.addPropertyChangeListener(list);
		menu.addPropertyChangeListener(list);
		
	}
	/**
	 * The terrain-changing menu.
	 */
	private final TerrainChangingMenu menu = new TerrainChangingMenu();
	/**
	 * Handle mouse clicks.
	 * 
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void mouseClicked(final MouseEvent event) {
		model.setSelection(event.getPoint().y / MapComponent.TILE_SIZE, event.getPoint().x / MapComponent.TILE_SIZE);
		if (event.getClickCount() == 2) {
			pcs.firePropertyChange("encounter", "old", "new");
			model.copyTile(model.getSelectedTile());
		}
		if (event.isPopupTrigger()) {
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}
	/**
	 * Handle mouse presses.
	 * 
	 * @param event
	 *            the event to handle
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
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void mouseReleased(final MouseEvent event) {
		if (event.isPopupTrigger()) {
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}
}
