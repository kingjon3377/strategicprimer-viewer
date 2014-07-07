package model.listeners;

import java.util.EventListener;

/**
 * An interface for objects listening to AddRemovePanels.
 *
 * @author Jonathan Lovelace
 */
public interface AddRemoveListener extends EventListener {
	/**
	 * @param category what kind of thing is being added
	 * @param addendum A String description of the thing to be added
	 */
	void add(String category, String addendum);

	/**
	 * This method need not actually be implemented.
	 *
	 * @param category what kind of thing is being removed
	 */
	void remove(String category);
}
