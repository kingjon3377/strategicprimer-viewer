package model.listeners;

import model.map.fixtures.mobile.worker.IJob;

import org.eclipse.jdt.annotation.Nullable;

/**
 * An interface for objects that want to know when the user selects a Job from a
 * list or tree.
 *
 * @author Jonathan Lovelace
 *
 */
public interface JobSelectionListener {
	/**
	 * @param job the newly selected Job. May be null if no selection.
	 */
	void selectJob(@Nullable final IJob job);
}
