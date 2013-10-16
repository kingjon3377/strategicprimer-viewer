package model.listeners;

import java.util.EventListener;

/**
 * An interface for things that want to be called when a new map is loaded.
 *
 * @author Jonathan Lovelace
 *
 */
public interface MapChangeListener extends EventListener {
	/**
	 * Called when a new map is loaded. Since everything that listens for the
	 * "map" property (which this replaces) goes to its reference to a map model
	 * rather than taking the map from the event, this has no parameters.
	 */
	void mapChanged();
}
