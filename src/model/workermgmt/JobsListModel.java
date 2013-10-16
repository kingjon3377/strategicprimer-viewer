package model.workermgmt;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

import model.listeners.AddRemoveListener;
import model.listeners.CompletionListener;
import model.listeners.CompletionSource;
import model.listeners.UnitMemberListener;
import model.listeners.UnitMemberSelectionSource;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;

import org.eclipse.jdt.annotation.Nullable;

import view.util.AddRemovePanel;

/**
 * A list model for a list of a worker's jobs.
 *
 * @author Jonathan Lovelace
 */
public class JobsListModel extends DefaultListModel<Job> implements
		UnitMemberListener, CompletionSource, AddRemoveListener {
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
	 *
	 * @param umSources sources to listen to for changes in which unit member is
	 *        selected
	 * @param arps panels to listen to for new jobs from the user
	 */
	public JobsListModel(final UnitMemberSelectionSource[] umSources,
			final AddRemovePanel[] arps) {
		for (final UnitMemberSelectionSource source : umSources) {
			source.addUnitMemberListener(this);
		}
		for (final AddRemovePanel panel : arps) {
			panel.addAddRemoveListener(this);
		}
	}

	/**
	 * @param category what kind of thing is being added; if not a Job we ignore
	 *        it
	 * @param addendum a description of what to add
	 */
	@Override
	public void add(final String category, final String addendum) {
		if ("job".equals(category) && !NULL_WORKER.equals(worker)) {
			final Job job = new Job(addendum, 0);
			worker.addJob(job);
			addElement(job);
			for (final CompletionListener list : cListeners) {
				list.stopWaitingOn(job);
			}
		}
	}

	/**
	 * @param old the previously selected member
	 * @param selected the newly selected unit member
	 */
	@Override
	public void memberSelected(@Nullable final UnitMember old,
			@Nullable final UnitMember selected) {
		if (!worker.equals(selected)) {
			clear();
			if (selected instanceof Worker) {
				worker = (Worker) selected;
				for (Job job : worker) {
					addElement(job);
				}
				final Object retval = isEmpty() ? Integer.valueOf(-1) : Integer
						.valueOf(0);
				assert retval != null;
				for (final CompletionListener list : cListeners) {
					list.stopWaitingOn(retval);
				}
			} else {
				worker = NULL_WORKER;
			}
		}
	}

	/**
	 * The list of completion listeners listening to us.
	 */
	private final List<CompletionListener> cListeners = new ArrayList<>();

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addCompletionListener(final CompletionListener list) {
		cListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removeCompletionListener(final CompletionListener list) {
		cListeners.remove(list);
	}

	/**
	 * @param category ignored
	 */
	@Override
	public void remove(final String category) {
		// Not implemented.
	}
}
