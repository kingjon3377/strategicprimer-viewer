package model.workermgmt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.DefaultListModel;

import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import util.PropertyChangeSource;
/**
 * A list model for a list of a worker's jobs.
 * @author Jonathan Lovelace
 */
public class JobsListModel extends DefaultListModel<Job> implements
		PropertyChangeListener, PropertyChangeSource {
	/**
	 * The current worker.
	 */
	private Worker worker = null;
	/**
	 * Constructor.
	 * @param sources property-change sources to listen to.
	 */
	public JobsListModel(final PropertyChangeSource... sources) {
		if (sources.length == 0) {
			throw new IllegalStateException("No sources given");
		}
		for (final PropertyChangeSource source : sources) {
			source.addPropertyChangeListener(this);
		}
	}
	/**
	 * Handle a property change.
	 * @param evt the event to handle.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("member".equalsIgnoreCase(evt.getPropertyName())) {
			if (worker == null || !worker.equals(evt.getNewValue())) {
				clear();
				if (evt.getNewValue() instanceof Worker) {
					worker = (Worker) evt.getNewValue();
					for (Job job : worker) {
						addElement(job);
					}
					pcs.firePropertyChange("finished", null, isEmpty() ? Integer.valueOf(-1) : Integer.valueOf(0));
				}
			} else if (evt.getNewValue() == null) {
				worker = (Worker) evt.getNewValue();
				clear();
			}
		} else if ("add".equalsIgnoreCase(evt.getPropertyName())
				&& worker != null) {
			final Job job = new Job(evt.getNewValue().toString(), 0);
			worker.addJob(job);
			addElement(job);
			pcs.firePropertyChange("finished", null, job);
		}
	}
	/**
	 * Our delegate for property-change handling.
	 */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	/**
	 * @param list a listener to listen to us
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener list) {
		pcs.addPropertyChangeListener(list);
	}
	/**
	 * @param list a listener to stop listenng to us
	 */
	@Override
	public void removePropertyChangeListener(final PropertyChangeListener list) {
		pcs.removePropertyChangeListener(list);
	}
}
