package model.listeners;

import java.util.EventListener;

import model.map.fixtures.mobile.Worker;

/**
 * An interface for things that want to accept a new user-created worker.
 *
 * @author Jonathan Lovelace
 */
public interface NewWorkerListener extends EventListener {
	/**
	 * Add the new unit.
	 *
	 * @param unit the unit to add
	 */
	void addNewWorker(final Worker unit);
}
