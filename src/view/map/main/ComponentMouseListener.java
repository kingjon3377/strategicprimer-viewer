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
		pcs.addPropertyChangeListener(list);
	}
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
	}
}
