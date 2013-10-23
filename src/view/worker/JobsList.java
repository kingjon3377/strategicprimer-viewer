package view.worker;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.listeners.CompletionListener;
import model.listeners.JobSelectionListener;
import model.listeners.JobSelectionSource;
import model.listeners.UnitMemberSelectionSource;
import model.map.fixtures.mobile.worker.Job;
import model.workermgmt.JobsListModel;

import org.eclipse.jdt.annotation.Nullable;

import view.util.AddRemovePanel;

/**
 * A visual list of a worker's Jobs. We also handle listening for selection
 * changes.
 *
 * @author Jonathan Lovelace
 *
 */
public class JobsList extends JList<Job> implements ListSelectionListener,
		JobSelectionSource {
	/**
	 * Constructor.
	 *
	 * @param listener something to listen to us
	 * @param umSources sources to listen to for changes in which unit member is
	 *        selected
	 * @param arps panels to listen to for new jobs from the user
	 */
	public JobsList(final JobSelectionListener listener,
			final UnitMemberSelectionSource[] umSources,
			final AddRemovePanel[] arps) {
		final JobsListModel lmodel = new JobsListModel(umSources);
		setModel(lmodel);
		for (final AddRemovePanel arp : arps) {
			arp.addAddRemoveListener(lmodel);
		}
		lmodel.addCompletionListener(new CompletionListener() {
			@Override
			public void stopWaitingOn(final boolean end) {
				if (!end) {
					setSelectedIndex(0);
				} else {
					setSelectedIndex(lmodel.size() - 1);
				}
			}
		});
		addJobSelectionListener(listener);
		addListSelectionListener(this);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * @param evt an event indicating the selection changed.
	 */
	@Override
	public void valueChanged(@Nullable final ListSelectionEvent evt) {
		@Nullable
		final Job temp = getSelectedValue();
		for (final JobSelectionListener list : jsListeners) {
			list.selectJob(temp);
		}
	}

	/**
	 * The list of completion listeners listening to us.
	 */
	private final List<JobSelectionListener> jsListeners = new ArrayList<>();

	/**
	 * @param list a listener to add
	 */
	@Override
	public final void addJobSelectionListener(final JobSelectionListener list) {
		jsListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public final void removeJobSelectionListener(final JobSelectionListener list) {
		jsListeners.remove(list);
	}
}
