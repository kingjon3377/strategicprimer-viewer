package model.listeners;
/**
 * An interface for objects that handle the user's selection of Jobs.
 * @author Jonathan Lovelace
 *
 */
public interface JobSelectionSource {
	/**
	 * @param list a listener to add
	 */
	void addJobSelectionListener(final JobSelectionListener list);
	/**
	 * @param list a listener to remove
	 */
	void removeJobSelectionListener(final JobSelectionListener list);
}
