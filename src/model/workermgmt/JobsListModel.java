package model.workermgmt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.DefaultListModel;

import org.eclipse.jdt.annotation.Nullable;

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
	 * A non-null "null" worker. Adjusted to prevent modification.
	 */
	private static final Worker NULL_WORKER = new Worker("null", "null", -1) {
		@Override
		public boolean addJob(final Job job) {
			return false;
		}
	};
	/**
	 * The current worker.
	 */
	private Worker worker = NULL_WORKER;
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
	public void propertyChange(@Nullable final PropertyChangeEvent evt) {
		if (evt == null) {
			return;
		} else if ("member".equalsIgnoreCase(evt.getPropertyName())) {
			handleMemberChange(evt.getNewValue());
		} else if (("add".equalsIgnoreCase(evt.getPropertyName()) || "add_job".equalsIgnoreCase(evt.getPropertyName()))
				&& worker != null && !NULL_WORKER.equals(worker)) {
			final Job job = new Job(evt.getNewValue().toString(), 0);
			worker.addJob(job);
			addElement(job);
			pcs.firePropertyChange("finished", null, job);
		}
	}
	/**
	 * Handle a "worker" property-change.
	 * @param newValue the "new value" from the PropertyChangeEvent
	 */
	private void handleMemberChange(final Object newValue) {
		if (!worker.equals(newValue)) {
			clear();
			if (newValue instanceof Worker) {
				worker = (Worker) newValue;
				for (Job job : worker) {
					addElement(job);
				}
				pcs.firePropertyChange("finished", null, isEmpty() ? Integer.valueOf(-1) : Integer.valueOf(0));
			}
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
